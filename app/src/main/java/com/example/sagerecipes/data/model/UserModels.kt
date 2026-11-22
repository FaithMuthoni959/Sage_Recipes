package com.example.sagerecipes.data.model

import java.time.Instant

/** One row of the user's pantry. */
data class PantryItem(
    val ingredient: Ingredient,
    val quantity: String?,
    val addedAt: Instant,
)

/** A favorite, with when it was last cooked (null if never). */
data class FavoriteRecipe(
    val recipe: RecipeSummary,
    val favoritedAt: Instant,
    val lastMadeAt: Instant?,
)

/** An entry for the "Recently made" list. */
data class RecentlyMadeRecipe(
    val recipe: RecipeSummary,
    val madeAt: Instant,
)