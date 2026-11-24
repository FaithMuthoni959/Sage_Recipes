package com.example.sagerecipes.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.example.sagerecipes.R
import com.example.sagerecipes.ui.screens.category.DessertsScreen
import com.example.sagerecipes.ui.theme.SageRecipesTheme

class DessertsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                SageRecipesTheme {
                    DessertsScreen(
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
}
