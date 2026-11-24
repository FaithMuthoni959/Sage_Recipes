package com.example.sagerecipes.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.example.sagerecipes.ui.screens.recipe.RecipeDetailScreen
import com.example.sagerecipes.ui.theme.SageRecipesTheme

class RecipeDetailFragment : Fragment() {
    private var recipeId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            recipeId = it.getString(ARG_RECIPE_ID)
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
                    RecipeDetailScreen(
                        recipeId = recipeId ?: "1",
                        onBackClick = { parentFragmentManager.popBackStack() },
                        onBottomNavSelected = { index -> handleBottomNav(index) }
                    )
                }
            }
        }
    }

    companion object {
        private const val ARG_RECIPE_ID = "recipe_id"

        fun newInstance(recipeId: String): RecipeDetailFragment {
            val fragment = RecipeDetailFragment()
            val args = Bundle().apply {
                putString(ARG_RECIPE_ID, recipeId)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
