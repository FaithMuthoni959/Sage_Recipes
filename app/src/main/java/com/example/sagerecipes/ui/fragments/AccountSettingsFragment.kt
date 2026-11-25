package com.example.sagerecipes.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.sagerecipes.R
import com.example.sagerecipes.ui.screens.profile.AccountSettingsScreen
import com.example.sagerecipes.ui.theme.SageRecipesTheme
import com.example.sagerecipes.ui.theme.screens.auth.AuthViewModel

class AccountSettingsFragment : Fragment() {
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val state by authViewModel.state.collectAsState()

                LaunchedEffect(state.errorMessage) {
                    state.errorMessage?.let {
                        Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                        authViewModel.clearMessages()
                    }
                }

                SageRecipesTheme {
                    AccountSettingsScreen(
                        initialName = state.userName,
                        initialEmail = state.userEmail,
                        onBackClick = { parentFragmentManager.popBackStack() },
                        onSaveClick = { updatedName ->
                            authViewModel.updateDisplayName(updatedName) {
                                Toast.makeText(requireContext(), "Saved!", Toast.LENGTH_SHORT).show()
                                parentFragmentManager.popBackStack()
                            }
                        },
                        onDeleteAccountClick = {
                            authViewModel.logout()
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