package com.example.sagerecipes.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.example.sagerecipes.R
import com.example.sagerecipes.ui.screens.recommendations.RecommendationsScreen
import com.example.sagerecipes.ui.theme.SageRecipesTheme

class RecommendationsFragment : Fragment() {
    private var selectedIngredients: ArrayList<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            selectedIngredients = it.getStringArrayList(ARG_INGREDIENTS)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                SageRecipesTheme {
                    RecommendationsScreen(
                        selectedIngredients = selectedIngredients ?: listOf("Potatoes", "Onions", "Eggs"),
                        onRecipeClick = { recipeId ->
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, RecipeDetailFragment.newInstance(recipeId))
                                .addToBackStack(null)
                                .commit()
                        },
                        onBackClick = { parentFragmentManager.popBackStack() },
                        onBottomNavSelected = { index -> handleBottomNav(index) }
                    )
                }
            }
        }
    }

    companion object {
        private const val ARG_INGREDIENTS = "arg_ingredients"

        fun newInstance(ingredients: ArrayList<String>): RecommendationsFragment {
            return RecommendationsFragment().apply {
                arguments = Bundle().apply {
                    putStringArrayList(ARG_INGREDIENTS, ingredients)
                }
            }
        }
    }
}
