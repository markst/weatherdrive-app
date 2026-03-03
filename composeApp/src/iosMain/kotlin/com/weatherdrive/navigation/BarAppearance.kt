package com.weatherdrive.navigation

import platform.UIKit.UIColor
import platform.UIKit.UINavigationBar
import platform.UIKit.UINavigationBarAppearance
import platform.UIKit.UITabBar
import platform.UIKit.UITabBarAppearance

private val barColor = UIColor.colorWithWhite(0.11, 1.0)
private val barTintColor = UIColor.whiteColor

/**
 * Applies the shared dark bar appearance to the given tab bar and to each navigation bar.
 * Use the real UINavigationBar instances (e.g. from each UINavigationController) to avoid
 * the appearance proxy type cast issue.
 */
fun applyBarAppearance(tabBar: UITabBar, navigationBars: List<UINavigationBar>) {
    tabBar.tintColor = barTintColor
    val tabBarAppearance = UITabBarAppearance().apply {
        configureWithOpaqueBackground()
        backgroundColor = barColor
    }
    tabBar.standardAppearance = tabBarAppearance
    tabBar.scrollEdgeAppearance = tabBarAppearance

    val navBarAppearance = UINavigationBarAppearance().apply {
        configureWithOpaqueBackground()
        backgroundColor = barColor
    }
    for (navBar in navigationBars) {
        navBar.tintColor = barTintColor
        navBar.standardAppearance = navBarAppearance
        navBar.scrollEdgeAppearance = navBarAppearance
        navBar.compactAppearance = navBarAppearance
    }
}
