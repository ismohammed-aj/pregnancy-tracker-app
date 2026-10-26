package com.pfe.mamacareplus.ui.main

import android.view.View
import androidx.appcompat.app.AppCompatActivity

fun AppCompatActivity.setMainContentView(
    content: View,
    activeTab: MainTab?,
    enableBackgroundAnimation: Boolean = MainLayoutSettings.isBackgroundAnimationEnabled(this)
) {
    setContentView(
        MainLayout(this).apply {
            setContent(content, activeTab, enableBackgroundAnimation)
        }
    )
}
