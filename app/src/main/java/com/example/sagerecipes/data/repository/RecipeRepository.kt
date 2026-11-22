package com.example.sagerecipes.data.repository

import com.example.sagerecipes.data.error.BackendException
import com.example.sagerecipes.data.error.ErrorKind
import com.example.sagerecipes.data.model.Difficulty
import com.example.sagerecipes.data.model.RecipeDetail
import com.example.sagerecipes.data.model.RecipeIngredientLine
import com.example.sagerecipes.data.model.RecipeStep
import com.example.sagerecipes.data.model.RecipeSummary
import com.example.sagerecipes.data.model.RecommendedRecipe
import com.example.sagerecipes.data.remote.SupabaseProvider
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Every list below is paged: ask for [limit] recipes starting at [offset].
 * If you get back fewer than [limit], there are no more pages. The server caps [limit] at 100.
 */
class RecipeRepository {
    private val client get() = SupabaseProvider.client

    // ---------- Recommendations (SQL ranking) ----------

    /** Recipes ranked by how many of their ingredients the user selected. */
    suspend fun getRecommendations(
        selectedIngredientIds: List<Long>,
        limit: Int = 50,
        offset: Int = 0,
    ): List<RecommendedRecipe> {
        if (selectedIngredientIds.isEmpty()) return emptyList()
        return client.postgrest
            .rpc("match_recipes", MatchParams(selectedIngredientIds, limit, offset))
            .decodeList<RecommendedRecipe>()
    }

    // ---------- Lists ----------

    /** One flexible, paged list. Any filter left as null is ignored. */
    suspend fun list(
        search: String? = null,
        tag: String? = null,
        maxMinutes: Int? = null,
        limit: Int = 20,
        offset: Int = 0,
    ): List<RecipeSummary> =
        client.postgrest
            .rpc(
                "list_recipes",
                ListParams(
                    searchText = search?.trim()?.takeIf { it.isNotEmpty() },
                    tagFilter = tag,
                    maxMinutes = maxMinutes,
                    pageLimit = limit.coerceIn(1, 100),
                    pageOffset = offset.coerceAtLeast(0),
                ),
            )
            .decodeList<RecipeSummary>()

    /** All recipes (for "View all"), best rated first. */
    suspend fun getAll(limit: Int = 20, offset: Int = 0): List<RecipeSummary> =
        list(limit = limit, offset = offset)

    /** Home categories such as "vegan", "protein", "dessert" (matches the recipes.tags column). */
    suspend fun getByTag(tag: String, limit: Int = 20, offset: Int = 0): List<RecipeSummary> =
        list(tag = tag, limit = limit, offset = offset)

    /** The "Quick" category: recipes that take at most [maxMinutes], fastest first. */
    suspend fun getQuick(maxMinutes: Int = 30, limit: Int = 20, offset: Int = 0): List<RecipeSummary> =
        list(maxMinutes = maxMinutes, limit = limit, offset = offset)

    /** Search box: matches recipe titles. */
    suspend fun search(query: String, limit: Int = 20, offset: Int = 0): List<RecipeSummary> {
        if (query.isBlank()) return emptyList()
        return list(search = query, limit = limit, offset = offset)
    }

    // ---------- One recipe ----------

    /** Full recipe: stats, ingredient lines and ordered steps. Throws a NOT_FOUND BackendException if missing. */
    suspend fun getRecipe(id: Long): RecipeDetail {
        val columns = listOf(
            "id", "title", "description", "image_url", "cook_time_minutes", "difficulty",
            "heat_level", "servings", "avg_rating", "tags",
            "recipe_ingredients(quantity, prep_note, ingredients(id, name))",
            "recipe_steps(step_number, title, body)",
        ).joinToString(", ")

        val dto = client.from("recipes").select(Columns.raw(columns)) {
            filter { eq("id", id) }
        }.decodeList<RecipeDetailDto>().firstOrNull()
            ?: throw BackendException(ErrorKind.NOT_FOUND, "We couldn't find that recipe.")

        return RecipeDetail(
            id = dto.id,
            title = dto.title,
            description = dto.description,
            imageUrl = dto.imageUrl,
            cookTimeMinutes = dto.cookTimeMinutes,
            difficulty = dto.difficulty,
            heatLevel = dto.heatLevel,
            servings = dto.servings,
            avgRating = dto.avgRating,
            tags = dto.tags,
            ingredients = dto.recipeIngredients.map {
                RecipeIngredientLine(
                    ingredientId = it.ingredients.id,
                    name = it.ingredients.name,
                    quantity = it.quantity,
                    prepNote = it.prepNote,
                )
            },
            steps = dto.recipeSteps
                .sortedBy { it.stepNumber }
                .map { RecipeStep(it.stepNumber, it.title, it.body) },
        )
    }

    // ---------- Lookup by the dataset's own id (used with the AI service) ----------

    /**
     * Finds recipes by the id the AI model uses (stored as recipes.external_id).
     * Returns a map of external id -> recipe. Ids that don't exist are simply missing from the map.
     */
    suspend fun getRecipesByExternalIds(externalIds: List<String>): Map<String, RecipeSummary> {
        if (externalIds.isEmpty()) return emptyMap()
        val ids = externalIds.distinct()
        val columns = Columns.list(
            "external_id", "id", "title", "description", "image_url",
            "cook_time_minutes", "difficulty", "avg_rating",
        )
        return client.from("recipes").select(columns) {
            filter { isIn("external_id", ids) }
        }.decodeList<ExternalRecipeDto>().associate { it.externalId to it.toSummary() }
    }
}

// ---- wire shapes used only inside this file ----

@Serializable
private data class MatchParams(
    @SerialName("selected_ids") val selectedIds: List<Long>,
    @SerialName("result_limit") val resultLimit: Int,
    @SerialName("result_offset") val resultOffset: Int,
)

@Serializable
private data class ListParams(
    @SerialName("search_text") val searchText: String?,
    @SerialName("tag_filter") val tagFilter: String?,
    @SerialName("max_minutes") val maxMinutes: Int?,
    @SerialName("page_limit") val pageLimit: Int,
    @SerialName("page_offset") val pageOffset: Int,
)

@Serializable
private data class ExternalRecipeDto(
    @SerialName("external_id") val externalId: String,
    val id: Long,
    val title: String,
    val description: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("cook_time_minutes") val cookTimeMinutes: Int? = null,
    val difficulty: Difficulty? = null,
    @SerialName("avg_rating") val avgRating: Double? = null,
) {
    fun toSummary() = RecipeSummary(id, title, description, imageUrl, cookTimeMinutes, difficulty, avgRating)
}

@Serializable
private data class RecipeDetailDto(
    val id: Long,
    val title: String,
    val description: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("cook_time_minutes") val cookTimeMinutes: Int? = null,
    val difficulty: Difficulty? = null,
    @SerialName("heat_level") val heatLevel: String? = null,
    val servings: Int? = null,
    @SerialName("avg_rating") val avgRating: Double? = null,
    val tags: List<String> = emptyList(),
    @SerialName("recipe_ingredients") val recipeIngredients: List<RecipeIngredientDto> = emptyList(),
    @SerialName("recipe_steps") val recipeSteps: List<RecipeStepDto> = emptyList(),
)

@Serializable
private data class RecipeIngredientDto(
    val quantity: String? = null,
    @SerialName("prep_note") val prepNote: String? = null,
    val ingredients: IngredientBriefDto,
)

@Serializable
private data class IngredientBriefDto(val id: Long, val name: String)

@Serializable
private data class RecipeStepDto(
    @SerialName("step_number") val stepNumber: Int,
    val title: String,
    val body: String,
)