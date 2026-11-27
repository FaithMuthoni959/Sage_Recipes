package com.example.sagerecipes.ml

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.ln
import kotlin.math.roundToInt
import kotlin.math.sqrt

data class EngineRecipe(
    val id: Int,
    val title: String,
    val description: String = "",
    val category: String,
    val course: String,
    val ingredients: List<String>,
    val instructions: List<String>,
    val prepTime: String = "",
    val cookTime: String = ""
)

data class ScoredRecipe(
    val recipe: EngineRecipe,
    val score: Double,
    val matchPercent: Int,
    val matchedIngredients: List<String>,
    val missingIngredients: List<String>
)

object RecipeRecommenderEngine {

    private var isInitialized = false
    private val recipes = mutableListOf<EngineRecipe>()
    private val idfMap = mutableMapOf<String, Double>()

    private var rerankerWeights = DoubleArray(9)
    private var rerankerBias = 0.0
    private var featureMeans = DoubleArray(9)
    private var featureStds = DoubleArray(9) { 1.0 }
    private var alpha = 0.6
    private var candidatePoolK = 200

    private val ingredientEmbeddings = mutableMapOf<String, FloatArray>()
    private var recipeEmbeddings: Array<FloatArray> = emptyArray()

    suspend fun initialize(context: Context) = withContext(Dispatchers.IO) {
        if (isInitialized) return@withContext
        val appContext = context.applicationContext

        loadIdf(appContext)
        loadReranker(appContext)
        loadRecipes(appContext)
        loadTwoTower(appContext)

        isInitialized = true
    }

    private fun readAssetText(context: Context, fileName: String): String {
        return context.assets.open(fileName).bufferedReader().use { it.readText() }
    }

    private fun normalizeIngredient(raw: String): String {
        val s = raw.lowercase().trim()
        return when {
            s == "potatoes" -> "potato"
            s == "tomatoes" -> "tomato"
            s == "chillies" || s == "chilies" -> "chilli"
            s == "leaves" -> "leaf"
            s.endsWith("ies") && s.length > 4 -> s.dropLast(3) + "y"
            s.endsWith("oes") && s.length > 4 -> s.dropLast(2)
            s.endsWith("s") && !s.endsWith("ss") && s.length > 3 -> s.dropLast(1)
            else -> s
        }
    }

    private fun parentIngredient(raw: String): String {
        val norm = normalizeIngredient(raw)
        val modifiers = setOf(
            "powder", "paste", "chopped", "sliced", "minced", "fresh",
            "dry", "dried", "ground", "whole", "breast", "thigh", "fillet",
            "leaf", "leaves", "seed", "seeds", "oil", "sauce", "extract", "essence"
        )
        val tokens = norm.split(" ").filter { it.isNotBlank() && it !in modifiers }
        return if (tokens.isNotEmpty()) tokens.joinToString(" ") else norm
    }

    private fun loadIdf(context: Context) {
        val json = JSONObject(readAssetText(context, "ingredient_idf.json"))
        val keys = json.keys()
        while (keys.hasNext()) {
            val k = keys.next()
            val v = json.getDouble(k)
            val clean = k.lowercase().trim()
            idfMap[clean] = v
            idfMap[normalizeIngredient(clean)] = v
        }
    }

    private fun loadReranker(context: Context) {
        val json = JSONObject(readAssetText(context, "reranker_final.json"))
        rerankerBias = json.optDouble("bias", 0.0)
        alpha = json.optDouble("alpha", 0.6)
        candidatePoolK = json.optInt("K", 200)

        val wArr = json.optJSONArray("weights")
        if (wArr != null) {
            rerankerWeights = DoubleArray(wArr.length()) { i -> wArr.getDouble(i) }
        }

        val meanArr = json.optJSONArray("mean")
        if (meanArr != null) {
            featureMeans = DoubleArray(meanArr.length()) { i -> meanArr.getDouble(i) }
        }

        val stdArr = json.optJSONArray("std")
        if (stdArr != null) {
            featureStds = DoubleArray(stdArr.length()) { i ->
                val v = stdArr.getDouble(i)
                if (v == 0.0) 1.0 else v
            }
        }
    }

    private fun parseInstructions(rawValue: Any?): List<String> {
        if (rawValue == null || rawValue == JSONObject.NULL) return emptyList()

        if (rawValue is JSONArray) {
            return List(rawValue.length()) { idx -> rawValue.getString(idx).trim() }
                .filter { it.isNotEmpty() }
        }

        val rawText = rawValue.toString().trim()
        if (rawText.isEmpty()) return emptyList()

        val rawLines = rawText
            .replace("\r\n", "\n")
            .split("\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val mergedSteps = mutableListOf<String>()
        for (line in rawLines) {
            if (mergedSteps.isNotEmpty() && line.first().isLowerCase()) {
                val prev = mergedSteps.removeAt(mergedSteps.lastIndex)
                mergedSteps.add("$prev $line")
            } else {
                mergedSteps.add(line)
            }
        }
        return mergedSteps
    }

    private fun loadRecipes(context: Context) {
        val rootText = readAssetText(context, "recipes_app.json").trim()
        val arr = if (rootText.startsWith("[")) {
            JSONArray(rootText)
        } else {
            JSONObject(rootText).getJSONArray("recipes")
        }

        recipes.clear()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)

            val ingArr = obj.optJSONArray("ingredients") ?: obj.optJSONArray("ing") ?: JSONArray()
            val ingList = List(ingArr.length()) { idx -> ingArr.getString(idx).lowercase().trim() }
                .filter { it.isNotEmpty() }

            val rawInst = when {
                obj.has("instructions") -> obj.get("instructions")
                obj.has("directions") -> obj.get("directions")
                obj.has("steps") -> obj.get("steps")
                obj.has("method") -> obj.get("method")
                else -> null
            }
            val instList = parseInstructions(rawInst)

            recipes.add(
                EngineRecipe(
                    id = obj.optInt("id", i),
                    title = obj.optString("title", "Untitled Recipe"),
                    description = obj.optString("description", ""),
                    category = obj.optString("category", ""),
                    course = obj.optString("course", ""),
                    ingredients = ingList,
                    instructions = instList,
                    prepTime = obj.optString("prep_time", ""),
                    cookTime = obj.optString("cook_time", "")
                )
            )
        }
    }

    private fun loadTwoTower(context: Context) {
        val json = JSONObject(readAssetText(context, "two_tower_export.json"))
        val recArr = json.optJSONArray("recipe_emb")
            ?: json.optJSONArray("item_emb")
            ?: json.optJSONArray("recipe_embeddings")
            ?: json.optJSONArray("V")
        if (recArr != null) {
            recipeEmbeddings = Array(recArr.length()) { i ->
                val row = recArr.getJSONArray(i)
                FloatArray(row.length()) { j -> row.getDouble(j).toFloat() }
            }
        }

        val vocabArr = json.optJSONArray("vocab") ?: json.optJSONArray("ingredients")
        val ingArr = json.optJSONArray("ing_emb")
            ?: json.optJSONArray("user_ing_emb")
            ?: json.optJSONArray("ingredient_embeddings")
            ?: json.optJSONArray("U")
        if (vocabArr != null && ingArr != null) {
            for (i in 0 until vocabArr.length()) {
                val word = vocabArr.getString(i).lowercase().trim()
                val row = ingArr.getJSONArray(i)
                val vec = FloatArray(row.length()) { j -> row.getDouble(j).toFloat() }
                ingredientEmbeddings[word] = vec
                ingredientEmbeddings[normalizeIngredient(word)] = vec
            }
        }
    }

    private fun ingredientMatches(recipeIng: String, normalizedQueries: Set<String>): Boolean {
        val normIng = normalizeIngredient(recipeIng)
        if (recipeIng in normalizedQueries || normIng in normalizedQueries) return true
        for (q in normalizedQueries) {
            if (q.length >= 4 && (normIng.contains(q) || q.contains(normIng))) {
                return true
            }
        }
        return false
    }

    suspend fun recommend(
        context: Context,
        selectedIngredients: List<String>,
        topK: Int = 15
    ): List<ScoredRecipe> = withContext(Dispatchers.Default) {
        if (!isInitialized) initialize(context)

        val cleanQueryList = selectedIngredients
            .map { normalizeIngredient(it) }
            .filter { it.isNotEmpty() }
            .distinct()

        if (cleanQueryList.isEmpty()) return@withContext emptyList()

        val normalizedQueries = selectedIngredients
            .flatMap {
                val clean = it.lowercase().trim()
                listOf(clean, normalizeIngredient(clean))
            }
            .filter { it.isNotEmpty() }
            .toSet()

        val queryParents = cleanQueryList.map { parentIngredient(it) }.toSet()
        val querySize = cleanQueryList.size.toDouble()
        val queryParentSize = queryParents.size.coerceAtLeast(1).toDouble()

        // Stage 1: Baseline candidate retrieval (alpha * user_recall + (1 - alpha) * coverage) -> Top K=200
        val candidates = recipes.mapNotNull { recipe ->
            if (recipe.ingredients.isEmpty()) return@mapNotNull null
            val matched = recipe.ingredients.filter { ingredientMatches(it, normalizedQueries) }
            if (matched.isEmpty()) return@mapNotNull null

            val matchedQueryCount = cleanQueryList.count { q ->
                recipe.ingredients.any { rIng -> ingredientMatches(rIng, setOf(q)) }
            }
            val userRecall = matchedQueryCount.toDouble() / querySize
            val coverage = matched.size.toDouble() / recipe.ingredients.size.toDouble()
            val baselineScore = alpha * userRecall + (1.0 - alpha) * coverage

            Triple(recipe, baselineScore, matched)
        }
            .sortedByDescending { it.second }
            .take(candidatePoolK)

        val queryVec = computeMeanQueryEmbedding(normalizedQueries)

        // Stage 2: Exact 9-feature linear reranker matching reranker_final.json
        return@withContext candidates.map { (recipe, baselineScore, matched) ->
            val missing = recipe.ingredients.filter { !ingredientMatches(it, normalizedQueries) }

            val matchedQueryCount = cleanQueryList.count { q ->
                recipe.ingredients.any { rIng -> ingredientMatches(rIng, setOf(q)) }
            }
            val recipeParents = recipe.ingredients.map { parentIngredient(it) }.toSet()
            val matchedParentCount = queryParents.count { it in recipeParents }

            val userRecall = matchedQueryCount.toDouble() / querySize
            val userRecallParent = matchedParentCount.toDouble() / queryParentSize
            val coverage = matched.size.toDouble() / recipe.ingredients.size.toDouble()
            val logLen = ln(recipe.ingredients.size.toDouble() + 1.0)
            val logMissing = ln(missing.size.toDouble() + 1.0)
            val unusedQueryCount = (cleanQueryList.size - matchedQueryCount).coerceAtLeast(0)
            val logUnusedQuery = ln(unusedQueryCount.toDouble() + 1.0)
            val twoTowerCos = computeCosineSimilarity(queryVec, recipe.id)
            val mainIng = recipe.ingredients.firstOrNull() ?: ""
            val mainInQuery = if (mainIng.isNotEmpty() && ingredientMatches(mainIng, normalizedQueries)) 1.0 else 0.0

            val rawFeatures = doubleArrayOf(
                userRecall,
                userRecallParent,
                coverage,
                logLen,
                logMissing,
                logUnusedQuery,
                twoTowerCos,
                baselineScore,
                mainInQuery
            )

            var finalScore = rerankerBias
            val count = minOf(rawFeatures.size, rerankerWeights.size)
            for (i in 0 until count) {
                val standardized = (rawFeatures[i] - featureMeans[i]) / featureStds[i]
                finalScore += standardized * rerankerWeights[i]
            }

            val pct = (coverage * 100.0).roundToInt().coerceIn(1, 100)

            ScoredRecipe(
                recipe = recipe,
                score = finalScore,
                matchPercent = pct,
                matchedIngredients = matched,
                missingIngredients = missing
            )
        }
            .sortedWith(
                compareByDescending<ScoredRecipe> { it.matchPercent }
                    .thenByDescending { it.score }
            )
            .take(topK)
    }

    private fun computeMeanQueryEmbedding(querySet: Set<String>): FloatArray? {
        val vectors = querySet.mapNotNull { ingredientEmbeddings[it] }
        if (vectors.isEmpty()) return null
        val dim = vectors[0].size
        val mean = FloatArray(dim)
        for (vec in vectors) {
            for (i in 0 until dim) mean[i] += vec[i]
        }
        for (i in 0 until dim) mean[i] /= vectors.size.toFloat()
        return mean
    }

    private fun computeCosineSimilarity(queryVec: FloatArray?, recipeIndex: Int): Double {
        if (queryVec == null || recipeIndex !in recipeEmbeddings.indices) return 0.0
        val recVec = recipeEmbeddings[recipeIndex]
        if (recVec.size != queryVec.size) return 0.0

        var dot = 0.0
        var normA = 0.0
        var normB = 0.0
        for (i in queryVec.indices) {
            dot += queryVec[i] * recVec[i]
            normA += queryVec[i] * queryVec[i]
            normB += recVec[i] * recVec[i]
        }
        val denom = sqrt(normA) * sqrt(normB)
        return if (denom > 0.0) dot / denom else 0.0
    }

    fun getAllRecipeIngredients(): List<String> {
        return recipes.flatMap { it.ingredients }
            .map { it.lowercase().trim().replaceFirstChar { c -> c.uppercase() } }
            .distinct()
            .sorted()
    }

    fun getRecipeById(id: String): EngineRecipe? {
        val intId = id.toIntOrNull() ?: return null
        return recipes.find { it.id == intId }
    }
}