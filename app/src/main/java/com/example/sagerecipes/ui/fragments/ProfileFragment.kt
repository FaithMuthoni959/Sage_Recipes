package com.example.sagerecipes.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.sagerecipes.R
import com.example.sagerecipes.ui.screens.profile.ProfileScreen
import com.example.sagerecipes.ui.theme.SageRecipesTheme
import com.example.sagerecipes.ui.theme.screens.auth.AuthViewModel

class ProfileFragment : Fragment() {
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val state by authViewModel.state.collectAsState()

                SageRecipesTheme {
                    ProfileScreen(
                        userName = state.userName.ifBlank { "Chef" },
                        userEmail = state.userEmail,
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
                        onDietaryPreferencesClick = {
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, DietaryPreferencesFragment())
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
                            authViewModel.logout()
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