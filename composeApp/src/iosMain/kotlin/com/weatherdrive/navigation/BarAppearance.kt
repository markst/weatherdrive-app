package com.weatherdrive.navigation

import platform.UIKit.UIColor
import platform.UIKit.UINavigationBar
import platform.UIKit.UINavigationBarAppearance
import platform.UIKit.UITabBar
import platform.UIKit.UITabBarAppearance

// Purple80 equivalent: #BB86FC
private val accentTint = UIColor.colorWithRed(0.733, green = 0.525, blue = 0.988, alpha = 1.0)

/** Fully transparent nav bar; list screens use Compose haze for gradient blur instead. */
val transparentNavBarAppearance = UINavigationBarAppearance().apply {
    configureWithTransparentBackground()
    backgroundEffect = null
    shadowColor = UIColor.clearColor
}

/**
 * Applies the shared bar appearance to the given tab bar and to each navigation bar.
 * Navigation bars and tab bar are fully transparent.
 */
fun applyBarAppearance(tabBar: UITabBar, navigationBars: List<UINavigationBar>) {
    val tabBarAppearance = UITabBarAppearance().apply {
        configureWithTransparentBackground()
        backgroundColor = UIColor.clearColor
        shadowColor = UIColor.clearColor
    }
    tabBar.standardAppearance = tabBarAppearance
    tabBar.scrollEdgeAppearance = tabBarAppearance
    tabBar.tintColor = accentTint

    for (navBar in navigationBars) {
        navBar.tintColor = accentTint
        navBar.translucent = true
        navBar.barTintColor = null
        navBar.backgroundColor = UIColor.clearColor
        applyNavBarAppearance(navBar, transparentNavBarAppearance)
    }
}

fun applyNavBarAppearance(navBar: UINavigationBar, appearance: UINavigationBarAppearance) {
    navBar.standardAppearance = appearance
    navBar.compactAppearance = appearance
    navBar.scrollEdgeAppearance = appearance
}
