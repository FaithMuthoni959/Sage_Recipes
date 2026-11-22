package com.example.sagerecipes.data.remote

import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

/** One suggestion from the AI service: the dataset's recipe id and how strongly it matched. */
data class ScoredRecipeId(val recipeId: String, val score: Double?)

/** What the app needs from the AI model. Your colleague's service has to provide this. */
interface RecommendationService {
    suspend fun recommend(ingredients: List<String>, limit: Int, offset: Int): List<ScoredRecipeId>
}

/**
 * Calls  POST {baseUrl}/recommend  with
 *   { "ingredients": ["eggs","potatoes"], "limit": 50, "offset": 0 }
 * and expects
 *   { "results": [ { "recipe_id": "4521", "score": 0.87 }, ... ] }
 * Use an https address: Android blocks plain http by default.
 */
class HttpRecommendationService(
    baseUrl: String,
    private val connectTimeoutMs: Int = 4_000,
    private val readTimeoutMs: Int = 5_000,
) : RecommendationService {

    private val endpoint = baseUrl.trimEnd('/') + "/recommend"

    override suspend fun recommend(
        ingredients: List<String>,
        limit: Int,
        offset: Int,
    ): List<ScoredRecipeId> = withContext(Dispatchers.IO) {
        val body = buildJsonObject {
            putJsonArray("ingredients") { ingredients.forEach { add(it) } }
            put("limit", limit)
            put("offset", offset)
        }.toString()

        val connection = URL(endpoint).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "POST"
            connection.connectTimeout = connectTimeoutMs
            connection.readTimeout = readTimeoutMs
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }

            if (connection.responseCode !in 200..299) {
                error("AI service returned HTTP ${connection.responseCode}")
            }
            val text = connection.inputStream.bufferedReader().use { it.readText() }
            parseRecommendResponse(text)
        } finally {
            connection.disconnect()
        }
    }
}

/** Reads the service's reply. Accepts recipe ids as text or numbers and skips entries without one. */
fun parseRecommendResponse(text: String): List<ScoredRecipeId> {
    val root = Json.parseToJsonElement(text).jsonObject
    val results = root["results"]?.jsonArray ?: return emptyList()
    return results.mapNotNull { element ->
        val obj = element.jsonObject
        val id = obj["recipe_id"]?.jsonPrimitive
        if (id == null || id is JsonNull) return@mapNotNull null
        ScoredRecipeId(id.content, obj["score"]?.jsonPrimitive?.doubleOrNull)
    }
}