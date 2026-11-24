package com.example.sagerecipes.ui.fragments

import androidx.fragment.app.Fragment
import com.example.sagerecipes.R

fun Fragment.handleBottomNav(index: Int) {
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
