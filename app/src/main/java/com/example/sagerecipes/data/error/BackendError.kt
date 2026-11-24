package com.example.sagerecipes.data.error

import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException
import kotlinx.coroutines.CancellationException

/** What went wrong, in terms a screen can react to (offline banner, login screen, retry button). */
enum class ErrorKind { NO_INTERNET, TIMEOUT, NOT_LOGGED_IN, NOT_ALLOWED, NOT_FOUND, SERVER, UNKNOWN }

class BackendException(
    val kind: ErrorKind,
    val userMessage: String,
    cause: Throwable? = null,
) : Exception(userMessage, cause)

fun messageFor(kind: ErrorKind): String = when (kind) {
    ErrorKind.NO_INTERNET -> "No internet connection. Check your connection and try again."
    ErrorKind.TIMEOUT -> "That took too long. Please try again."
    ErrorKind.NOT_LOGGED_IN -> "Your session has expired. Please log in again."
    ErrorKind.NOT_ALLOWED -> "You don't have permission to do that."
    ErrorKind.NOT_FOUND -> "We couldn't find that."
    ErrorKind.SERVER -> "Something went wrong on our side. Please try again later."
    ErrorKind.UNKNOWN -> "Something went wrong. Please try again."
}

/** Turns any exception into a BackendException with a kind and a readable message. */
fun Throwable.toBackendException(): BackendException {
    if (this is BackendException) return this

    val chain = generateSequence(this) { it.cause }.take(8).toList()
    val names = chain.map { it::class.java.simpleName }
    val text = chain.joinToString(" ") { it.message.orEmpty() }.lowercase()
    fun has(vararg parts: String) = parts.any { it in text }

    val kind = when {
        chain.any { it is SocketTimeoutException } ||
                names.any { it.contains("timeout", ignoreCase = true) } -> ErrorKind.TIMEOUT

        chain.any {
            it is UnknownHostException || it is ConnectException ||
                    it is SocketException || it is SSLException
        } -> ErrorKind.NO_INTERNET

        has("pgrst116", "json object requested") -> ErrorKind.NOT_FOUND

        has("jwt", "pgrst301", "pgrst303", "not authenticated", "refresh token",
            "session_not_found", "session missing") -> ErrorKind.NOT_LOGGED_IN

        has("row-level security", "permission denied", "42501") -> ErrorKind.NOT_ALLOWED

        has("internal server error", "bad gateway", "service unavailable",
            "gateway timeout") -> ErrorKind.SERVER

        else -> ErrorKind.UNKNOWN
    }
    return BackendException(kind, messageFor(kind), this)
}

val Throwable.errorKind: ErrorKind get() = toBackendException().kind

/** Text that is safe to show the user. */
val Throwable.friendlyMessage: String get() = toBackendException().userMessage

/**
 * Wrap any repository call in this inside a ViewModel:
 *
 *   backendCall { recipes.getRecipe(id) }
 *       .onSuccess { ... }
 *       .onFailure { e -> show(e.friendlyMessage) }
 */
suspend fun <T> backendCall(block: suspend () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e.toBackendException())
    }