//
//  WeatherdriveApp.swift
//  Weatherdrive
//
//  Created by Mark Turner on 2/3/2026.
//

import ComposeApp
import SwiftUI

@main
struct WeatherdriveApp: App {
    init() {
        MainViewControllerKt.doInitKoin()
    }

    var body: some Scene {
        WindowGroup {
            MainApp()
        }
    }
}

struct MainApp: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UITabBarController {
        let tabBarController = UITabBarController()
        let coordinator = AppCoordinator(tabBarController: tabBarController)
        return coordinator.getTabBarController()
    }

    func updateUIViewController(
        _ uiViewController: UITabBarController,
        context: Context
    ) {

    }
}
