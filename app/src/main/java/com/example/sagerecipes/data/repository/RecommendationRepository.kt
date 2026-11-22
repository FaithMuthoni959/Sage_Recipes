package com.example.sagerecipes.data.repository

import com.example.sagerecipes.data.model.Ingredient
import com.example.sagerecipes.data.model.RecipeSummary
import com.example.sagerecipes.data.model.RecommendationResult
import com.example.sagerecipes.data.model.RecommendationSource
import com.example.sagerecipes.data.model.RecommendedRecipe
import com.example.sagerecipes.data.remote.RecommendationService
import kotlin.math.roundToInt
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeoutOrNull

/**
 * The one place the screens ask for suggestions.
 *
 * With an AI service: ask it first. If it fails, is too slow, returns nothing, or returns ids
 * that Supabase doesn't know, fall back to the SQL ranking.
 * With ai = null (until the service exists): always the SQL ranking.
 */
class RecommendationRepository(
    private val recipes: RecipeRepository = RecipeRepository(),
    private val ai: RecommendationService? = null,
    private val aiTimeoutMs: Long = 6_000,
) {
    suspend fun recommend(
        selected: List<Ingredient>,
        limit: Int = 50,
        offset: Int = 0,
    ): RecommendationResult {
        if (selected.isEmpty()) return RecommendationResult(emptyList(), RecommendationSource.SQL)

        val viaAi = tryAi(selected, limit, offset)
        if (!viaAi.isNullOrEmpty()) return RecommendationResult(viaAi, RecommendationSource.AI)

        val viaSql = recipes.getRecommendations(selected.map { it.id }, limit, offset)
        return RecommendationResult(viaSql, RecommendationSource.SQL)
    }

    private suspend fun tryAi(
        selected: List<Ingredient>,
        limit: Int,
        offset: Int,
    ): List<RecommendedRecipe>? {
        val service = ai ?: return null
        return try {
            withTimeoutOrNull(aiTimeoutMs) {
                val scored = service.recommend(selected.map { it.name }, limit, offset)
                if (scored.isEmpty()) return@withTimeoutOrNull null
                val found = recipes.getRecipesByExternalIds(scored.map { it.recipeId })
                scored.mapNotNull { s -> found[s.recipeId]?.toRecommended(s.score) }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }

    /** Assumes the model's score runs from 0 to 1. Change the factor here if it doesn't. */
    private fun RecipeSummary.toRecommended(score: Double?) = RecommendedRecipe(
        id = id,
        title = title,
        description = description,
        imageUrl = imageUrl,
        cookTimeMinutes = cookTimeMinutes,
        difficulty = difficulty,
        matchPercent = ((score ?: 0.0) * 100).roundToInt().coerceIn(0, 100),
    )
}