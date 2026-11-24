package com.example.sagerecipes

import com.example.sagerecipes.data.remote.parseRecommendResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class ServiceParsingTest {

    @Test
    fun parsesTextAndNumberIds() {
        val parsed = parseRecommendResponse(
            """{"results":[{"recipe_id":"4521","score":0.87},{"recipe_id":130,"score":0.5}]}""",
        )
        assertEquals(listOf("4521", "130"), parsed.map { it.recipeId })
        assertEquals(0.87, parsed[0].score!!, 0.0001)
    }

    @Test
    fun missingScore_isNull() {
        val parsed = parseRecommendResponse("""{"results":[{"recipe_id":"a"}]}""")
        assertEquals(1, parsed.size)
        assertNull(parsed[0].score)
    }

    @Test
    fun noResultsKey_isEmpty() = assertTrue(parseRecommendResponse("""{"other":1}""").isEmpty())

    @Test
    fun emptyResults_isEmpty() = assertTrue(parseRecommendResponse("""{"results":[]}""").isEmpty())

    @Test
    fun entriesWithoutId_areSkipped() {
        val parsed = parseRecommendResponse(
            """{"results":[{"score":0.9},{"recipe_id":null},{"recipe_id":"ok","score":0.1}]}""",
        )
        assertEquals(listOf("ok"), parsed.map { it.recipeId })
    }

    @Test
    fun malformedJson_throws() {
        try {
            parseRecommendResponse("this is not json")
            fail("Malformed replies must throw so the app can fall back")
        } catch (e: Exception) {
            // expected
        }
    }
}