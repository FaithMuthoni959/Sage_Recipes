package com.example.sagerecipes

import com.example.sagerecipes.data.error.BackendException
import com.example.sagerecipes.data.error.ErrorKind
import com.example.sagerecipes.data.error.backendCall
import com.example.sagerecipes.data.error.errorKind
import com.example.sagerecipes.data.error.friendlyMessage
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class ErrorMappingTest {

    private class HttpRequestTimeoutException : Exception("Request timeout has expired")

    @Test
    fun unknownHost_isNoInternet() =
        assertEquals(ErrorKind.NO_INTERNET, UnknownHostException("api").errorKind)

    @Test
    fun wrappedCause_isFound() =
        assertEquals(ErrorKind.NO_INTERNET, RuntimeException("Request failed", UnknownHostException("x")).errorKind)

    @Test
    fun socketTimeout_isTimeout() =
        assertEquals(ErrorKind.TIMEOUT, SocketTimeoutException("slow").errorKind)

    @Test
    fun timeoutByClassName_isTimeout() =
        assertEquals(ErrorKind.TIMEOUT, HttpRequestTimeoutException().errorKind)

    @Test
    fun expiredToken_isNotLoggedIn() =
        assertEquals(ErrorKind.NOT_LOGGED_IN, RuntimeException("JWT expired").errorKind)

    @Test
    fun rlsViolation_isNotAllowed() =
        assertEquals(
            ErrorKind.NOT_ALLOWED,
            RuntimeException("new row violates row-level security policy").errorKind,
        )

    @Test
    fun noRows_isNotFound() =
        assertEquals(
            ErrorKind.NOT_FOUND,
            RuntimeException("JSON object requested, multiple (or no) rows returned").errorKind,
        )

    @Test
    fun serverError_isServer() =
        assertEquals(ErrorKind.SERVER, RuntimeException("502 Bad Gateway").errorKind)

    @Test
    fun unrecognised_isUnknown() =
        assertEquals(ErrorKind.UNKNOWN, IllegalStateException("boom").errorKind)

    @Test
    fun backendException_passesThroughUnchanged() {
        val original = BackendException(ErrorKind.NOT_FOUND, "custom text")
        assertEquals("custom text", original.friendlyMessage)
        assertEquals(ErrorKind.NOT_FOUND, original.errorKind)
    }

    @Test
    fun friendlyMessage_isReadable() =
        assertEquals(
            "No internet connection. Check your connection and try again.",
            UnknownHostException().friendlyMessage,
        )

    @Test
    fun backendCall_returnsSuccess() = runBlocking<Unit> {
        assertEquals(42, backendCall { 42 }.getOrNull())
    }

    @Test
    fun backendCall_wrapsFailures() = runBlocking<Unit> {
        val result = backendCall<Int> { throw UnknownHostException("x") }
        assertTrue(result.isFailure)
        assertEquals(ErrorKind.NO_INTERNET, result.exceptionOrNull()!!.errorKind)
        assertTrue(result.exceptionOrNull() is BackendException)
    }

    @Test
    fun backendCall_doesNotSwallowCancellation() = runBlocking<Unit> {
        try {
            backendCall<Unit> { throw CancellationException("stop") }
            fail("Cancellation must be rethrown")
        } catch (e: CancellationException) {
            // expected
        }
    }
}