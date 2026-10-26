package com.pfe.mamacareplus.ui.main

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.pfe.mamacareplus.R
import com.pfe.mamacareplus.notification.AppNotificationCenter
import com.pfe.mamacareplus.ui.appointment.AppointmentActivity
import com.pfe.mamacareplus.ui.article.ArticlesActivity
import com.pfe.mamacareplus.ui.home.HomeActivity
import com.pfe.mamacareplus.ui.pregnancy.PregnancyActivity
import com.pfe.mamacareplus.ui.profile.ProfileActivity
import com.pfe.mamacareplus.ui.weight.WeightActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MainLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val backgroundView = AnimatedBackgroundView(context)
    private val contentHost = FrameLayout(context)
    private val bottomNav = LinearLayout(context)
    private val notificationCenter = AppNotificationCenter(context)
    private val notificationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val notificationBadge = TextView(context)
    private val notificationButton = FrameLayout(context)
    private var lastNotificationCount = 0
    private var activeTab: MainTab? = MainTab.HOME

    init {
        setBackgroundColor(context.getColor(R.color.background))

        addView(
            backgroundView,
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        )

        contentHost.setPadding(0, 0, 0, NAV_RESERVED_BOTTOM.dp)
        addView(
            contentHost,
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        )

        bottomNav.apply {
            setBackgroundResource(R.drawable.bg_bottom_nav)
            elevation = 8.dp.toFloat()
            gravity = Gravity.CENTER
            orientation = LinearLayout.HORIZONTAL
            setPadding(5.dp, 5.dp, 5.dp, 5.dp)
            importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
        }

        addView(
            bottomNav,
            LayoutParams(LayoutParams.MATCH_PARENT, NAV_HEIGHT.dp, Gravity.BOTTOM).apply {
                marginStart = 14.dp
                marginEnd = 14.dp
                bottomMargin = 12.dp
            }
        )

        addView(createNotificationButton(), LayoutParams(48.dp, 48.dp, Gravity.TOP or Gravity.END).apply {
            topMargin = 18.dp
            marginEnd = 18.dp
        })
    }

    fun setContent(
        content: View,
        activeTab: MainTab?,
        enableBackgroundAnimation: Boolean = MainLayoutSettings.isBackgroundAnimationEnabled(context)
    ) {
        this.activeTab = activeTab
        backgroundView.setAnimationEnabled(enableBackgroundAnimation)
        prepareContentRoot(content)

        (content.parent as? ViewGroup)?.removeView(content)
        contentHost.removeAllViews()
        contentHost.addView(
            content,
            FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        )

        renderBottomNavigation()
        requestNotificationPermissionIfNeeded()
        refreshNotificationBadge()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        refreshNotificationBadge()
    }

    override fun onDetachedFromWindow() {
        notificationScope.cancel()
        super.onDetachedFromWindow()
    }

    private fun prepareContentRoot(content: View) {
        content.setBackgroundColor(Color.TRANSPARENT)
    }

    private fun renderBottomNavigation() {
        bottomNav.removeAllViews()
        navItems.forEachIndexed { index, item ->
            bottomNav.addView(createNavItem(item, index != 0))
        }
    }

    private fun createNavItem(item: NavItem, hasStartMargin: Boolean): View {
        val selected = item.tab == activeTab
        val itemColor = if (selected) SELECTED_COLOR else INACTIVE_COLOR

        return LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1f).apply {
                if (hasStartMargin) marginStart = 2.dp
            }
            gravity = Gravity.CENTER
            orientation = LinearLayout.VERTICAL
            setPadding(2.dp, 3.dp, 2.dp, 3.dp)
            isClickable = true
            isFocusable = true
            foreground = selectableItemBackground()
            contentDescription = item.contentDescription

            if (selected) {
                setBackgroundResource(R.drawable.bg_nav_selected)
            }

            setOnClickListener {
                openTab(item)
            }

            addView(ImageView(context).apply {
                setImageResource(item.iconRes)
                setColorFilter(itemColor)
                layoutParams = LinearLayout.LayoutParams(19.dp, 19.dp)
            })

            addView(TextView(context).apply {
                text = item.label
                setTextColor(itemColor)
                textSize = if (item.label.length > 7) 8.2f else 9.2f
                typeface = Typeface.DEFAULT_BOLD.takeIf { selected } ?: Typeface.DEFAULT
                gravity = Gravity.CENTER
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
                includeFontPadding = false
                layoutParams = LinearLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = 3.dp
                }
            })
        }
    }

    private fun openTab(item: NavItem) {
        val currentActivity = context as? Activity
        if (item.tab == activeTab && currentActivity?.javaClass == item.destination) return

        context.startActivity(
            Intent(context, item.destination).apply {
                addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            }
        )
    }

    private fun createNotificationButton(): View {
        notificationButton.apply {
            background = notificationButtonBackground()
            isClickable = true
            isFocusable = true
            elevation = 7.dp.toFloat()
            foreground = selectableItemBackground()
            contentDescription = "Notifications"
            setOnClickListener {
                val activity = context as? Activity ?: return@setOnClickListener
                animateNotificationButtonPress()
                notificationScope.launch {
                    notificationCenter.showNotificationDialog(activity) {
                        refreshNotificationBadge()
                    }
                    refreshNotificationBadge()
                }
            }

            addView(ImageView(context).apply {
                setImageResource(R.drawable.ic_notifications_24)
                setColorFilter(NOTIFICATION_ACCENT)
                layoutParams = LayoutParams(24.dp, 24.dp, Gravity.CENTER)
            })

            notificationBadge.apply {
                visibility = View.GONE
                gravity = Gravity.CENTER
                setTextColor(Color.WHITE)
                textSize = 10f
                setTypeface(null, Typeface.BOLD)
                setBackgroundResource(R.drawable.bg_notification_badge)
                includeFontPadding = false
                elevation = 8.dp.toFloat()
                layoutParams = LayoutParams(22.dp, 22.dp, Gravity.TOP or Gravity.END).apply {
                    topMargin = (-1).dp
                    marginEnd = (-1).dp
                }
            }
            addView(notificationBadge)
        }

        return notificationButton
    }

    private fun refreshNotificationBadge() {
        notificationScope.launch {
            val count = runCatching { notificationCenter.unreadCount() }.getOrDefault(0)
            notificationBadge.visibility = if (count > 0) View.VISIBLE else View.GONE
            notificationBadge.text = if (count > 9) "9+" else count.toString()
            if (count != lastNotificationCount && count > 0) {
                animateNotificationBadge()
            }
            lastNotificationCount = count
        }
    }

    private fun animateNotificationButtonPress() {
        AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(notificationButton, View.SCALE_X, 1f, 0.92f, 1f),
                ObjectAnimator.ofFloat(notificationButton, View.SCALE_Y, 1f, 0.92f, 1f)
            )
            duration = 210L
            interpolator = OvershootInterpolator(1.8f)
            start()
        }
    }

    private fun animateNotificationBadge() {
        notificationBadge.scaleX = 0.65f
        notificationBadge.scaleY = 0.65f
        notificationBadge.alpha = 0.55f
        AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(notificationBadge, View.SCALE_X, 0.65f, 1.12f, 1f),
                ObjectAnimator.ofFloat(notificationBadge, View.SCALE_Y, 0.65f, 1.12f, 1f),
                ObjectAnimator.ofFloat(notificationBadge, View.ALPHA, 0.55f, 1f)
            )
            duration = 260L
            interpolator = OvershootInterpolator(1.5f)
            start()
        }
    }

    private fun notificationButtonBackground(): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.WHITE)
            setStroke(1.dp, Color.parseColor("#D9F1F3"))
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val activity = context as? Activity ?: return
        val granted = ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (!granted) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION_PERMISSION_REQUEST
            )
        }
    }

    private fun selectableItemBackground(): android.graphics.drawable.Drawable? {
        val attrs = context.obtainStyledAttributes(intArrayOf(android.R.attr.selectableItemBackgroundBorderless))
        return attrs.getDrawable(0).also { attrs.recycle() }
    }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()

    private data class NavItem(
        val tab: MainTab,
        val label: String,
        val contentDescription: String,
        val iconRes: Int,
        val destination: Class<out Activity>
    )

    companion object {
        private const val NAV_HEIGHT = 58
        private const val NAV_RESERVED_BOTTOM = 88
        private const val NOTIFICATION_PERMISSION_REQUEST = 3401
        private val NOTIFICATION_ACCENT = Color.parseColor("#16B8C7")
        private val SELECTED_COLOR = Color.parseColor("#D6336C")
        private val INACTIVE_COLOR = Color.parseColor("#8A7180")

        private val navItems = listOf(
            NavItem(MainTab.HOME, "Accueil", "Accueil", R.drawable.ic_home_24, HomeActivity::class.java),
            NavItem(MainTab.HEALTH, "Santé", "Santé", R.drawable.ic_heart_pulse_24, WeightActivity::class.java),
            NavItem(MainTab.APPOINTMENTS, "RDV", "Rendez-vous", R.drawable.ic_calendar_24, AppointmentActivity::class.java),
            NavItem(MainTab.ARTICLES, "Conseils", "Conseils", R.drawable.ic_article_24, ArticlesActivity::class.java),
            NavItem(MainTab.BABY, "Bébé", "Développement de bébé", R.drawable.ic_baby_24, PregnancyActivity::class.java),
            NavItem(MainTab.PROFILE, "Profil", "Profil", R.drawable.ic_profile_24, ProfileActivity::class.java)
        )
    }
}
