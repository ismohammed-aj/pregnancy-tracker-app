package com.pfe.mamacareplus.ui.main

import android.content.Context
import android.provider.Settings

object MainLayoutSettings {
    private const val PREFS_NAME = "main_layout_settings"
    private const val KEY_ANIMATED_BACKGROUND = "animated_background_enabled"

    fun isBackgroundAnimationEnabled(context: Context): Boolean {
        val enabledByApp = context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_ANIMATED_BACKGROUND, true)

        return enabledByApp && systemAnimationsEnabled(context)
    }

    fun setBackgroundAnimationEnabled(context: Context, enabled: Boolean) {
        context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_ANIMATED_BACKGROUND, enabled)
            .apply()
    }

    private fun systemAnimationsEnabled(context: Context): Boolean {
        return runCatching {
            Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) != 0f
        }.getOrDefault(true)
    }
}
