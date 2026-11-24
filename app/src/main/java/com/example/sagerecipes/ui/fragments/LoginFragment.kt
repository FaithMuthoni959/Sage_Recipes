package com.example.sagerecipes.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.compose.ui.platform.ComposeView
import com.example.sagerecipes.R
import com.example.sagerecipes.ui.screens.auth.LoginScreen
import com.example.sagerecipes.ui.theme.SageRecipesTheme

class LoginFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                SageRecipesTheme {
                    LoginScreen(
                        onLoginClick = { _, _ ->
                            // Navigate to HomeFragment upon successful login
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, HomeFragment())
                                .commit()
                        },
                        onRegisterLinkClick = {
                            // Navigate to RegisterFragment
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, RegisterFragment())
                                .addToBackStack(null)
                                .commit()
                        }
                    )
                }
            }
        }
    }
}
