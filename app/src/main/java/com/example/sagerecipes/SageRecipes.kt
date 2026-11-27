package com.example.sagerecipes

import android.app.Application
import com.example.sagerecipes.ui.theme.ThemeManager

class SageRecipes : Application() {
    override fun onCreate() {
        super.onCreate()
        ThemeManager.init(this)
    }
}
