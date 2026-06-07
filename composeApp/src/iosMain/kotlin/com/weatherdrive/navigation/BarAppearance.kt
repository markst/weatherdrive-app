package com.weatherdrive.navigation

import platform.UIKit.UIColor
import platform.UIKit.UINavigationBar
import platform.UIKit.UINavigationBarAppearance
import platform.UIKit.UITabBar
import platform.UIKit.UITabBarAppearance
import platform.UIKit.UIBlurEffect
import platform.UIKit.UIBlurEffectStyle

// Purple80 equivalent: #BB86FC
private val accentTint = UIColor.colorWithRed(0.733, green = 0.525, blue = 0.988, alpha = 1.0)

/**
 * Applies the shared dark bar appearance to the given tab bar and to each navigation bar.
 * Navigation bars are transparent with a blur effect; tab bar is dark opaque.
 * Use the real UINavigationBar instances (e.g. from each UINavigationController) to avoid
 * the appearance proxy type cast issue.
 */
fun applyBarAppearance(tabBar: UITabBar, navigationBars: List<UINavigationBar>) {
    // Tab bar — transparent (iOS 26 glass tab bar)
    val tabBarAppearance = UITabBarAppearance().apply {
        configureWithTransparentBackground()
        backgroundColor = UIColor.clearColor
        shadowColor = UIColor.clearColor
    }
    tabBar.standardAppearance = tabBarAppearance
    tabBar.scrollEdgeAppearance = tabBarAppearance
    tabBar.tintColor = accentTint

    // Navigation bars — blurred system material
    val blurEffect = UIBlurEffect.effectWithStyle(UIBlurEffectStyle.UIBlurEffectStyleSystemUltraThinMaterial)
    val navBarAppearance = UINavigationBarAppearance().apply {
        configureWithTransparentBackground()
        backgroundEffect = blurEffect
        shadowColor = UIColor.clearColor
    }
    for (navBar in navigationBars) {
        navBar.tintColor = accentTint
        navBar.translucent = true
        navBar.barTintColor = null
        navBar.backgroundColor = UIColor.clearColor
        navBar.standardAppearance = navBarAppearance
        navBar.compactAppearance = navBarAppearance
        navBar.scrollEdgeAppearance = navBarAppearance
    }
}
