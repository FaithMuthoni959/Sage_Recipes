package com.example.sagerecipes.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.example.sagerecipes.R
import com.example.sagerecipes.ui.screens.category.CategoryRecipesScreen
import com.example.sagerecipes.ui.theme.SageRecipesTheme

class CategoryRecipesFragment : Fragment() {
    private var categoryName: String = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            categoryName = it.getString(ARG_CATEGORY_NAME, "All")
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
                    CategoryRecipesScreen(
                        categoryName = categoryName,
                        onRecipeClick = { recipeId ->
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, RecipeDetailFragment.newInstance(recipeId))
                                .addToBackStack(null)
                                .commit()
                        },
                        onBackClick = { parentFragmentManager.popBackStack() }
                    )
                }
            }
        }
    }

    companion object {
        private const val ARG_CATEGORY_NAME = "arg_category_name"

        fun newInstance(categoryName: String): CategoryRecipesFragment {
            return CategoryRecipesFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CATEGORY_NAME, categoryName)
                }
            }
        }
    }
}
