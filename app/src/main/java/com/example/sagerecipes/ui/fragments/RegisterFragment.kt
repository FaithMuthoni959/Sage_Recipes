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
import com.example.sagerecipes.ui.screens.auth.RegisterScreen
import com.example.sagerecipes.ui.theme.SageRecipesTheme
import com.example.sagerecipes.ui.theme.screens.auth.AuthViewModel

class RegisterFragment : Fragment() {
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val state by authViewModel.state.collectAsState()

                LaunchedEffect(state.isLoggedIn) {
                    if (state.isLoggedIn) {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, HomeFragment())
                            .commit()
                    }
                }

                LaunchedEffect(state.errorMessage) {
                    state.errorMessage?.let { msg ->
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
                        authViewModel.clearMessages()
                    }
                }

                LaunchedEffect(state.infoMessage) {
                    state.infoMessage?.let { msg ->
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
                        authViewModel.clearMessages()
                        parentFragmentManager.popBackStack()
                    }
                }

                SageRecipesTheme {
                    RegisterScreen(
                        onRegisterClick = { email, name, password, confirmPassword ->
                            if (!state.isLoading) {
                                // RegisterScreen currently validates passwords on its own or passes password twice
                                authViewModel.register(email, name, password, confirmPassword)
                            }
                        },
                        onLoginLinkClick = {
                            parentFragmentManager.popBackStack()
                        }
                    )
                }
            }
        }
    }
}