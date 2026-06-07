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

        KoinInitializerKt.doInitKoin()

        let rootVC = MainViewControllerKt.MainViewController()
        rootVC.overrideUserInterfaceStyle = .dark

        let window = UIWindow(windowScene: windowScene)
        window.rootViewController = rootVC
        window.overrideUserInterfaceStyle = .dark
        window.makeKeyAndVisible()
        self.window = window
    }
}
