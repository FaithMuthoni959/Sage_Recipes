package com.example.sagerecipes.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.compose.ui.platform.ComposeView
import com.example.sagerecipes.R
import com.example.sagerecipes.ui.screens.ingredients.IngredientsScreen
import com.example.sagerecipes.ui.theme.SageRecipesTheme

class IngredientsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                SageRecipesTheme {
                    IngredientsScreen(
                        onAddSpicesClick = {
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, SpicesFragment())
                                .addToBackStack(null)
                                .commit()
                        },
                        onBackClick = { parentFragmentManager.popBackStack() },
                        onCustomSearchClick = {
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, CustomIngredientSearchFragment())
                                .addToBackStack(null)
                                .commit()
                        }
                    )
                }
            }
        }
    }
}
