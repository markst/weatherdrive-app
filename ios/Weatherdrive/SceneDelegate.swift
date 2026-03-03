//
//  SceneDelegate.swift
//  Weatherdrive
//

import ComposeApp
import UIKit

class SceneDelegate: UIResponder, UIWindowSceneDelegate {

    var window: UIWindow?

    func scene(
        _ scene: UIScene,
        willConnectTo session: UISceneSession,
        options connectionOptions: UIScene.ConnectionOptions
    ) {
        guard let windowScene = scene as? UIWindowScene else { return }

        MainViewControllerKt.doInitKoin()

        let tabBarController = UITabBarController()
        let coordinator = AppCoordinator(tabBarController: tabBarController)
        let rootVC = coordinator.getTabBarController()

        let window = UIWindow(windowScene: windowScene)
        window.rootViewController = rootVC
        window.makeKeyAndVisible()
        self.window = window
    }
}
