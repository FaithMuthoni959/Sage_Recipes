package com.example.sagerecipes.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.compose.ui.platform.ComposeView
import com.example.sagerecipes.R
import com.example.sagerecipes.ui.screens.home.HomeScreen
import com.example.sagerecipes.ui.theme.SageRecipesTheme

class HomeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                SageRecipesTheme {
                    HomeScreen(
                        onAddIngredientsClick = {
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, IngredientsFragment())
                                .addToBackStack(null)
                                .commit()
                        },
                        onRecipeClick = { recipeId ->
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, RecipeDetailFragment.newInstance(recipeId.toString()))
                                .addToBackStack(null)
                                .commit()
                        },
                        onCategoryClick = { categoryTitle ->
                            val fragment = when (categoryTitle.lowercase()) {
                                "vegan" -> VeganFragment()
                                "quick" -> QuickFragment()
                                "protein" -> ProteinFragment()
                                "desserts", "dessert" -> DessertsFragment()
                                else -> HomeFragment()
                            }
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, fragment)
                                .addToBackStack(null)
                                .commit()
                        },
                        onAccountSettingsClick = {
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, AccountSettingsFragment())
                                .addToBackStack(null)
                                .commit()
                        },
                        onAboutClick = {
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, AboutSageRecipesFragment())
                                .addToBackStack(null)
                                .commit()
                        },
                        onLogoutClick = {
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, LoginFragment())
                                .commit()
                        },
                        onBottomNavSelected = { index ->
                            val target = when (index) {
                                0 -> HomeFragment()
                                1 -> FavoritesFragment()
                                2 -> PantryFragment()
                                3 -> ProfileFragment()
                                else -> null
                            }
                            target?.let {
                                parentFragmentManager.beginTransaction()
                                    .replace(R.id.fragment_container, it)
                                    .commit()
                            }
                        }
                    )
                }
            }
        }
    }
}
