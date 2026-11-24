package com.example.sagerecipes.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.example.sagerecipes.R
import com.example.sagerecipes.ui.screens.profile.ProfileScreen
import com.example.sagerecipes.ui.theme.SageRecipesTheme

class ProfileFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                SageRecipesTheme {
                    ProfileScreen(
                        onFavoriteRecipesClick = {
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, FavoritesFragment())
                                .addToBackStack(null)
                                .commit()
                        },
                        onPantryInventoryClick = {
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, PantryFragment())
                                .addToBackStack(null)
                                .commit()
                        },
                        onAccountSettingsClick = {
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, AccountSettingsFragment())
                                .addToBackStack(null)
                                .commit()
                        },
                        onLogoutClick = {
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, LoginFragment())
                                .commit()
                        },
                        onBottomNavSelected = { index -> handleBottomNav(index) }
                    )
                }
            }
        }
    }

    private fun handleBottomNav(index: Int) {
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
}
