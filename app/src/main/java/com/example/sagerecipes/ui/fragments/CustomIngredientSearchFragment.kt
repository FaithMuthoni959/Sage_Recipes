package com.example.sagerecipes.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.example.sagerecipes.ui.screens.customingredient.CustomIngredientSearchScreen
import com.example.sagerecipes.ui.theme.SageRecipesTheme

class CustomIngredientSearchFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                SageRecipesTheme {
                    CustomIngredientSearchScreen(
                        onBackClick = { parentFragmentManager.popBackStack() },
                        onIngredientAdded = { ingredientName ->
                            // TODO: Save ingredient to partner's backend / pantry state
                            parentFragmentManager.popBackStack()
                        },
                        onBottomNavSelected = { index -> handleBottomNav(index) }
                    )
                }
            }
        }
    }
}
