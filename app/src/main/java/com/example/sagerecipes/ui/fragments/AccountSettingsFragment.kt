package com.example.sagerecipes.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.example.sagerecipes.R
import com.example.sagerecipes.ui.screens.profile.AccountSettingsScreen
import com.example.sagerecipes.ui.theme.SageRecipesTheme

class AccountSettingsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                SageRecipesTheme {
                    AccountSettingsScreen(
                        onBackClick = { parentFragmentManager.popBackStack() },
                        onSaveClick = { parentFragmentManager.popBackStack() },
                        onDeleteAccountClick = {
                            // Delete account action -> return to Login
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
}
