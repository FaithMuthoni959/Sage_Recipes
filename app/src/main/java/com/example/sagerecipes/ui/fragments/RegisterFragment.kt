package com.example.sagerecipes.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.compose.ui.platform.ComposeView
import com.example.sagerecipes.R
import com.example.sagerecipes.ui.screens.auth.RegisterScreen
import com.example.sagerecipes.ui.theme.SageRecipesTheme

class RegisterFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                SageRecipesTheme {
                    RegisterScreen(
                        onRegisterClick = { _, _, _ ->
                            // Navigate back to LoginFragment
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, LoginFragment())
                                .commit()
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
