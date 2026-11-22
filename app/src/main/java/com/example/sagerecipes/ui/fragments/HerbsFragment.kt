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
import com.example.sagerecipes.ui.screens.herbs.HerbsScreen
import com.example.sagerecipes.ui.theme.SageRecipesTheme

class HerbsFragment : Fragment() {

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
                    HerbsScreen(
                        selectedIngredients = selectionViewModel.selectedIngredients,
                        onIngredientToggle = { item ->
                            selectionViewModel.toggleIngredient(item)
                        },
                        onDisplayMealsClick = {
                            val selectedItems =
                                ArrayList(selectionViewModel.selectedIngredients)

                            parentFragmentManager.beginTransaction()
                                .replace(
                                    R.id.fragment_container,
                                    RecommendationsFragment.newInstance(selectedItems)
                                )
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