package com.example.sagerecipes.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.sagerecipes.R
import com.example.sagerecipes.ui.IngredientSelectionViewModel
import com.example.sagerecipes.ui.screens.spices.SpicesScreen
import com.example.sagerecipes.ui.theme.SageRecipesTheme

class SpicesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val selectionViewModel =
            ViewModelProvider(requireActivity())[IngredientSelectionViewModel::class.java]

        return ComposeView(requireContext()).apply {
            setContent {
                SageRecipesTheme {
                    SpicesScreen(
                        selectedIngredients = selectionViewModel.selectedIngredients,
                        onIngredientToggle = { item ->
                            selectionViewModel.toggleIngredient(item)
                        },
                        onAddHerbsClick = {
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, HerbsFragment())
                                .addToBackStack(null)
                                .commit()
                        },
                        onBackClick = {
                            parentFragmentManager.popBackStack()
                        },
                        onCustomSearchClick = {
                            parentFragmentManager.beginTransaction()
                                .replace(
                                    R.id.fragment_container,
                                    CustomIngredientSearchFragment()
                                )
                                .addToBackStack(null)
                                .commit()
                        }
                    )
                }
            }
        }
    }
}