package com.example.sagerecipes.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class IngredientCategory {
    @SerialName("vegetable") VEGETABLE,
    @SerialName("protein") PROTEIN,
    @SerialName("dairy") DAIRY,
    @SerialName("grain") GRAIN,
    @SerialName("legume") LEGUME,
    @SerialName("spice") SPICE,
    @SerialName("aromatic") AROMATIC,
    @SerialName("blend") BLEND,
    @SerialName("herb") HERB,
    @SerialName("staple") STAPLE,
    @SerialName("other") OTHER,
}

/** The value stored in the database */
val IngredientCategory.dbValue: String get() = name.lowercase()

@Serializable
enum class Difficulty { Easy, Medium, Hard}

@Serializable
data class Ingredient(
    val id: Long,
    val name: String,
    val category: IngredientCategory,
    @SerialName("image_url") val imageUrl: String? = null,
)

/** One card in a recipe list (home categories, search, favorites). */
@Serializable
data class RecipeSummary(
    val id: Long,
    val title: String,
    val description: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("cook_time_minutes") val cookTimeMinutes: Int? = null,
    val difficulty: Difficulty? = null,
    @SerialName("avg_rating") val avgRating: Double? = null,
)
/** One card on the "Food recommendations" screen. */
@Serializable
data class RecommendedRecipe(
    val id: Long,
    val title: String,
    val description: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("cook_time_minutes") val cookTimeMinutes: Int? = null,
    val difficulty: Difficulty? = null,
    @SerialName("match_percent") val matchPercent: Int,
)
data class RecipeIngredientLine(
    val ingredientId: Long,
    val name: String,
    val quantity: String?,
    val prepNote: String?,
)

data class RecipeStep(val number: Int, val title : String, val body: String)
data class RecipeDetail(
    val id: Long,
    val title: String,
    val description: String?,
    val imageUrl: String?,
    val cookTimeMinutes: Int?,
    val difficulty: Difficulty?,
    val heatLevel: String?,
    val servings: Int?,
    val avgRating: Double?,
    val tags: List<String>,
    val ingredients: List<RecipeIngredientLine>,
    val steps: List<RecipeStep>,
)