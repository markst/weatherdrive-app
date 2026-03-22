# Native Navigation with Compose Multiplatform — Coordinator Pattern Specification

## Overview

This specification describes a navigation architecture for **Compose Multiplatform** (KMP) apps that uses **native platform navigation** on each platform while building all screen UI with shared Compose code. The key insight is separating *what is rendered* (shared Compose screens) from *how navigation happens* (platform-native controllers and transitions).

On **iOS**, this means using `UITabBarController` and `UINavigationController` directly — giving users the familiar iOS tab bar, native push/pop animations, swipe-back gestures, and large-title navigation bars. On **Android**, it means using Jetpack Compose Navigation (`NavHost`/`NavController`) or any other Android navigation library, all within a standard `Scaffold` bottom navigation layout.

The result is an app that **feels native on both platforms** — with native navigation chrome, native animations, and native gestures — while sharing 100% of the screen-level UI code.

---

## Why Native Navigation Matters

Compose Multiplatform provides cross-platform UI rendering, but its built-in navigation solutions render tab bars and navigation bars as Compose widgets. This leads to several issues:

1. **Non-native animations** — Compose-driven transitions don't match the platform's native push/pop, swipe-back, or tab-switch animations.
2. **Unfamiliar appearance** — Custom-rendered tab bars and navigation bars look subtly different from their native counterparts (spacing, blur effects, font weight, icon alignment).
3. **Missing gestures** — iOS users expect the interactive swipe-back gesture on `UINavigationController`. Compose-only navigation cannot replicate this natively.
4. **Accessibility gaps** — Native navigation controllers integrate with platform accessibility features (VoiceOver, TalkBack) in ways that are hard to replicate.

By using native navigation controllers and wrapping each Compose screen in a platform view controller, you get all of this for free.

---

## Architecture Summary

```
┌──────────────────────────────────────────────────────┐
│                    commonMain                         │
│                                                       │
│   expect class AppCoordinator()                       │
│     fun Content()                                     │
│                                                       │
│   expect class BrowseCoordinator()                    │
│     fun navigateToShowDetail(show: Show)              │
│     fun navigateBack()                                │
│     fun Content()                                     │
│                                                       │
│   expect class DownloadsCoordinator()                 │
│     fun navigateBack()                                │
│     fun Content()                                     │
│                                                       │
│   Shared Screens (HomeScreen, ShowDetailScreen, ...)  │
│     - Accept closures/lambdas for navigation events   │
│     - Accept showTopBar: Boolean parameter            │
│                                                       │
└────────────┬──────────────────────────┬───────────────┘
             │                          │
    ┌────────▼────────┐       ┌────────▼─────────┐
    │   androidMain    │       │     iosMain       │
    │                  │       │                   │
    │  NavHost +       │       │  UITabBarController│
    │  NavController   │       │  UINavController   │
    │  Scaffold +      │       │  ComposeUIVC       │
    │  NavigationBar   │       │  BarAppearance     │
    └──────────────────┘       └───────────────────┘
```

---

## Core Concepts

### 1. The `expect`/`actual` Coordinator Pattern

Each coordinator is declared as an `expect class` in `commonMain` with a no-arg constructor and a `@Composable fun Content()` method. Platform-specific `actual` implementations provide the real navigation logic.

**Common declaration** (`commonMain`):

```kotlin
// AppCoordinator.kt
expect class AppCoordinator() {
    @Composable
    fun Content()
}

// BrowseCoordinator.kt
expect class BrowseCoordinator() {
    fun navigateToShowDetail(show: Show)
    fun navigateBack()

    @Composable
    fun Content()
}

// DownloadsCoordinator.kt
expect class DownloadsCoordinator() {
    fun navigateBack()

    @Composable
    fun Content()
}
```

The `expect` declarations define the **navigation contract** — what actions each coordinator supports — without any platform dependency.

### 2. The `fun Content()` Composable

The `@Composable fun Content()` method is the critical bridge between shared and platform code. It serves two purposes:

- **On iOS**: Wraps the native `UITabBarController` or `UINavigationController` using `UIKitViewController`, embedding the entire native navigation stack into the Compose hierarchy.
- **On Android**: Renders the `NavHost` with `NavController`, providing standard Jetpack Compose navigation within the Compose tree.

Because `Content()` is a regular `@Composable` function declared in the `expect` class, the shared `App()` composable can call it without knowing anything about the platform:

```kotlin
// App.kt (commonMain)
@Composable
fun App() {
    val coordinator = remember { AppCoordinator() }

    MaterialTheme {
        coordinator.Content()  // Platform-native navigation happens here
    }
}
```

This is what makes the pattern powerful: **the Android `actual` implementation of `Content()` can use any Compose-compatible navigation library** (Jetpack Navigation, Voyager, Decompose, etc.) because it is just a composable function. The common code does not impose any navigation framework — it only requires that `Content()` renders the screen.

### 3. Screen Design — Closures/Lambdas and `showTopBar`

Every shared screen composable accepts:

1. **Lambda/closure parameters** for navigation events (e.g., `onShowClick`, `onBack`). The coordinator binds these to platform-native navigation calls.
2. **A `showTopBar: Boolean` parameter** (defaults to `true`) that controls whether the screen renders its own Compose `TopAppBar`.

```kotlin
// HomeScreen.kt (commonMain)
@Composable
fun HomeScreen(
    onShowClick: (Show) -> Unit = {},
    showTopBar: Boolean = true
) {
    Scaffold(
        topBar = {
            if (showTopBar) {
                TopAppBar(title = { Text("Browse") })
            }
        }
    ) { paddingValues ->
        // Screen content...
    }
}
```

```kotlin
// ShowDetailScreen.kt (commonMain)
@Composable
fun ShowDetailScreen(
    showId: Long,
    onBack: () -> Unit = {},
    showTopBar: Boolean = true
) {
    Scaffold(
        topBar = {
            if (showTopBar) {
                TopAppBar(
                    title = { Text(show.title) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        // Screen content...
    }
}
```

**Why `showTopBar`?**

- **On iOS**, `UINavigationController` provides its own native navigation bar with the title, back button, and swipe-back gesture. Rendering a second Compose `TopAppBar` would be redundant. So iOS coordinators pass `showTopBar = false`.
- **On Android**, there is no external navigation bar — the screen itself must render the `TopAppBar`. So Android coordinators use the default `showTopBar = true`.

This keeps screens fully reusable across platforms while allowing each platform to decide which chrome is native.

---

## iOS Implementation

### AppCoordinator (iOS)

The iOS `AppCoordinator` owns a `UITabBarController` and creates child coordinators for each tab:

```kotlin
actual class AppCoordinator(
    private val tabBarController: UITabBarController
) {
    private val browseCoordinator = BrowseCoordinator()
    private val downloadsCoordinator = DownloadsCoordinator()

    actual constructor() : this(UITabBarController()) {
        setupTabs()
    }

    private fun setupTabs() {
        val browseNav = browseCoordinator.getNavigationController()
        val downloadsNav = downloadsCoordinator.getNavigationController()

        browseNav.setTitle("Browse")
        downloadsNav.setTitle("Downloads")

        tabBarController.setViewControllers(
            listOf(browseNav, downloadsNav),
            animated = false
        )
        applyBarAppearance(
            tabBar = tabBarController.tabBar,
            navigationBars = listOf(browseNav.navigationBar, downloadsNav.navigationBar)
        )
    }

    @OptIn(ExperimentalForeignApi::class)
    @Composable
    actual fun Content() {
        UIKitViewController(
            factory = { tabBarController },
            modifier = Modifier
        )
    }
}
```

**Key points:**

- The `UITabBarController` is created in Kotlin and hosts the native tab bar.
- Each tab is a `UINavigationController` from a child coordinator.
- `Content()` uses `UIKitViewController` to embed the native UIKit view hierarchy into Compose.
- Bar appearance (colors, tint) is applied once during setup.

### BrowseCoordinator (iOS)

The browse coordinator owns a `UINavigationController` and wraps each Compose screen in a `ComposeUIViewController`:

```kotlin
actual class BrowseCoordinator(
    private val navigationController: UINavigationController
) {
    actual constructor() : this(UINavigationController()) {
        setupNavigationController()
    }

    private fun setupNavigationController() {
        val homeVC = ComposeUIViewController {
            HomeScreen(
                onShowClick = { show -> navigateToShowDetail(show) },
                showTopBar = false  // Native nav bar provides the title
            )
        }
        navigationController.setViewControllers(listOf(homeVC), animated = false)
    }

    @OptIn(ExperimentalForeignApi::class)
    @Composable
    actual fun Content() {
        UIKitViewController(
            factory = { navigationController },
            modifier = Modifier
        )
    }

    actual fun navigateToShowDetail(show: Show) {
        val detailVC = ComposeUIViewController {
            ShowDetailScreen(
                showId = show.id,
                onBack = { navigateBack() },
                showTopBar = false  // Native nav bar provides back button
            )
        }
        navigationController.pushViewController(detailVC, animated = true)
    }

    actual fun navigateBack() {
        navigationController.popViewControllerAnimated(animated = true)
    }
}
```

**Key points:**

- `ComposeUIViewController { ... }` wraps each Compose screen in a `UIViewController` that UIKit can manage.
- Navigation lambdas (`onShowClick`, `onBack`) are bound to the coordinator's native push/pop methods.
- `showTopBar = false` is passed because `UINavigationController` renders its own navigation bar.
- `pushViewController` and `popViewControllerAnimated` provide the standard iOS slide animations and swipe-back gesture.

### Bar Appearance (iOS)

A shared utility function styles the tab bar and navigation bars consistently:

```kotlin
fun applyBarAppearance(tabBar: UITabBar, navigationBars: List<UINavigationBar>) {
    val tabBarAppearance = UITabBarAppearance().apply {
        configureWithOpaqueBackground()
        backgroundColor = barColor
    }
    tabBar.standardAppearance = tabBarAppearance
    tabBar.scrollEdgeAppearance = tabBarAppearance
    tabBar.tintColor = barTintColor

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
```

### Swift Integration (iOS)

The `SceneDelegate` creates the coordinator and sets its tab bar controller as the window's root:

```swift
class SceneDelegate: UIResponder, UIWindowSceneDelegate {
    var window: UIWindow?

    func scene(_ scene: UIScene, willConnectTo session: UISceneSession, options: UIScene.ConnectionOptions) {
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
```

This gives iOS full native ownership of the window's root view controller hierarchy — the `UITabBarController` is a real UIKit controller, not a Compose simulation.

---

## Android Implementation

### AppCoordinator (Android)

The Android `AppCoordinator` uses a `Scaffold` with a `NavigationBar` for tab switching:

```kotlin
actual class AppCoordinator actual constructor() {
    private val browseCoordinator = BrowseCoordinator()
    private val downloadsCoordinator = DownloadsCoordinator()

    @Composable
    actual fun Content() {
        var selectedTab by remember { mutableIntStateOf(0) }

        Scaffold(
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Home, "Browse") },
                        label = { Text("Browse") },
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.KeyboardArrowDown, "Downloads") },
                        label = { Text("Downloads") },
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 }
                    )
                }
            }
        ) { paddingValues ->
            when (selectedTab) {
                0 -> Box(Modifier.padding(paddingValues)) {
                    browseCoordinator.Content()
                }
                1 -> Box(Modifier.padding(paddingValues)) {
                    downloadsCoordinator.Content()
                }
            }
        }
    }
}
```

### BrowseCoordinator (Android)

The Android browse coordinator uses Jetpack Compose Navigation with type-safe routes:

```kotlin
actual class BrowseCoordinator actual constructor() {
    private var navController: NavHostController? by mutableStateOf(null)

    @Composable
    actual fun Content() {
        val controller = rememberNavController()
        navController = controller

        NavHost(
            navController = controller,
            startDestination = BrowseHomeRoute
        ) {
            composable<BrowseHomeRoute> {
                HomeScreen(
                    onShowClick = { show -> navigateToShowDetail(show) }
                    // showTopBar defaults to true — Compose renders the app bar
                )
            }
            composable<ShowDetailRoute> { backStackEntry ->
                val route: ShowDetailRoute = backStackEntry.toRoute()
                ShowDetailScreen(
                    showId = route.id,
                    onBack = { navigateBack() }
                    // showTopBar defaults to true — Compose renders the back button
                )
            }
        }
    }

    actual fun navigateToShowDetail(show: Show) {
        navController?.navigate(show.toRoute())
    }

    actual fun navigateBack() {
        navController?.popBackStack()
    }
}
```

### Navigation Routes (Android)

Routes are defined as `@Serializable` objects/data classes for type-safe navigation:

```kotlin
@Serializable
object BrowseHomeRoute

@Serializable
object DownloadsHomeRoute

@Serializable
data class ShowDetailRoute(val id: Long)

fun Show.toRoute(): ShowDetailRoute = ShowDetailRoute(id = id)
```

### Why `fun Content()` Enables Any Android Navigation Library

Because the `expect` declaration only requires a `@Composable fun Content()` method, the Android `actual` implementation is free to use **any** Compose-compatible navigation approach:

- **Jetpack Navigation** (`NavHost` + `NavController`) — as shown in this codebase.
- **Voyager** — Replace `NavHost` with Voyager's `Navigator` and `Screen` classes inside `Content()`.
- **Decompose** — Use Decompose's `ChildStack` and render components inside `Content()`.
- **Appyx** — Use Appyx's node-based navigation inside `Content()`.
- **Simple state management** — Use `mutableStateOf` to track the current screen and render it directly.

The common code never references any navigation library — it only calls `coordinator.Content()` and the navigation lambdas on each screen. This makes the Android navigation library a **swappable implementation detail**.

---

## Applying This Pattern to a New Codebase

### Step 1: Design Your Screens as Pure Composables

Each screen should be a `@Composable` function in `commonMain` that:

- Accepts **lambda parameters** for every navigation event (`onItemClick`, `onBack`, `onNavigateToSettings`, etc.).
- Accepts a **`showTopBar: Boolean = true`** parameter to control whether the screen renders its own navigation bar.
- Has **no dependency** on any navigation library.

```kotlin
// commonMain
@Composable
fun ItemListScreen(
    onItemClick: (Item) -> Unit = {},
    showTopBar: Boolean = true
) { /* ... */ }

@Composable
fun ItemDetailScreen(
    itemId: Long,
    onBack: () -> Unit = {},
    showTopBar: Boolean = true
) { /* ... */ }
```

### Step 2: Define Coordinator Expect Classes

Create `expect class` declarations in `commonMain` for each navigation scope (one per tab, or one per flow):

```kotlin
// commonMain
expect class ItemsCoordinator() {
    fun navigateToDetail(item: Item)
    fun navigateBack()

    @Composable
    fun Content()
}
```

If you have a tab bar, create an `AppCoordinator`:

```kotlin
// commonMain
expect class AppCoordinator() {
    @Composable
    fun Content()
}
```

### Step 3: Implement iOS Coordinators with Native UIKit

In `iosMain`, each coordinator owns a `UINavigationController` and uses `ComposeUIViewController` to wrap screens:

```kotlin
// iosMain
actual class ItemsCoordinator(
    private val navigationController: UINavigationController
) {
    actual constructor() : this(UINavigationController()) {
        val rootVC = ComposeUIViewController {
            ItemListScreen(
                onItemClick = { item -> navigateToDetail(item) },
                showTopBar = false
            )
        }
        navigationController.setViewControllers(listOf(rootVC), animated = false)
    }

    fun getNavigationController(): UINavigationController = navigationController

    @OptIn(ExperimentalForeignApi::class)
    @Composable
    actual fun Content() {
        UIKitViewController(
            factory = { navigationController },
            modifier = Modifier
        )
    }

    actual fun navigateToDetail(item: Item) {
        val detailVC = ComposeUIViewController {
            ItemDetailScreen(
                itemId = item.id,
                onBack = { navigateBack() },
                showTopBar = false
            )
        }
        navigationController.pushViewController(detailVC, animated = true)
    }

    actual fun navigateBack() {
        navigationController.popViewControllerAnimated(animated = true)
    }
}
```

The `AppCoordinator` owns a `UITabBarController` and assembles the child coordinators' navigation controllers as tabs.

### Step 4: Implement Android Coordinators with Your Preferred Library

In `androidMain`, implement using Jetpack Navigation, Voyager, or any other approach:

```kotlin
// androidMain
actual class ItemsCoordinator actual constructor() {
    private var navController: NavHostController? by mutableStateOf(null)

    @Composable
    actual fun Content() {
        val controller = rememberNavController()
        navController = controller

        NavHost(navController = controller, startDestination = ItemListRoute) {
            composable<ItemListRoute> {
                ItemListScreen(
                    onItemClick = { item -> navigateToDetail(item) }
                )
            }
            composable<ItemDetailRoute> { backStackEntry ->
                val route: ItemDetailRoute = backStackEntry.toRoute()
                ItemDetailScreen(
                    itemId = route.id,
                    onBack = { navigateBack() }
                )
            }
        }
    }

    actual fun navigateToDetail(item: Item) {
        navController?.navigate(ItemDetailRoute(item.id))
    }

    actual fun navigateBack() {
        navController?.popBackStack()
    }
}
```

### Step 5: Wire Up the App Entry Point

In `commonMain`, the `App()` composable creates the `AppCoordinator` and calls `Content()`:

```kotlin
// commonMain
@Composable
fun App() {
    val coordinator = remember { AppCoordinator() }
    MaterialTheme {
        coordinator.Content()
    }
}
```

On iOS, the `SceneDelegate` creates the coordinator and sets the `UITabBarController` as the window's root view controller. On Android, the `MainActivity` calls `setContent { App() }` as normal.

### Step 6: Customize Bar Appearance (iOS)

Create a utility function to style `UITabBar` and `UINavigationBar` appearances consistently:

```kotlin
// iosMain
fun applyBarAppearance(tabBar: UITabBar, navigationBars: List<UINavigationBar>) {
    val tabBarAppearance = UITabBarAppearance().apply {
        configureWithOpaqueBackground()
        backgroundColor = yourBackgroundColor
    }
    tabBar.standardAppearance = tabBarAppearance
    tabBar.scrollEdgeAppearance = tabBarAppearance

    val navBarAppearance = UINavigationBarAppearance().apply {
        configureWithOpaqueBackground()
        backgroundColor = yourBackgroundColor
    }
    navigationBars.forEach { navBar ->
        navBar.standardAppearance = navBarAppearance
        navBar.scrollEdgeAppearance = navBarAppearance
    }
}
```

Call this once during tab setup in the `AppCoordinator`.

---

## File Structure

```
composeApp/
  src/
    commonMain/kotlin/com/yourapp/
      navigation/
        AppCoordinator.kt          # expect class
        BrowseCoordinator.kt       # expect class
        DownloadsCoordinator.kt    # expect class
      ui/
        HomeScreen.kt              # Shared composable
        ShowDetailScreen.kt        # Shared composable
        DownloadsListScreen.kt     # Shared composable
      App.kt                       # Entry point composable

    androidMain/kotlin/com/yourapp/
      navigation/
        AppCoordinator.kt          # actual — Scaffold + NavigationBar
        BrowseCoordinator.kt       # actual — NavHost + NavController
        DownloadsCoordinator.kt    # actual — NavHost + NavController
        routes/
          Routes.kt                # @Serializable route definitions

    iosMain/kotlin/com/yourapp/
      navigation/
        AppCoordinator.kt          # actual — UITabBarController
        BrowseCoordinator.kt       # actual — UINavigationController
        DownloadsCoordinator.kt    # actual — UINavigationController
        BarAppearance.kt           # UIKit bar styling utility
      MainViewController.kt        # ComposeUIViewController entry point

ios/
  YourApp/
    AppDelegate.swift
    SceneDelegate.swift             # Creates coordinator, sets window root
```

---

## Summary

| Concern | Common | iOS | Android |
|---|---|---|---|
| Screen UI | Shared composables with lambdas | Same | Same |
| Tab bar | `AppCoordinator.Content()` | `UITabBarController` | `Scaffold` + `NavigationBar` |
| Navigation stack | Coordinator methods | `UINavigationController` push/pop | `NavController` navigate/popBackStack |
| Screen wrapping | — | `ComposeUIViewController { Screen() }` | Direct composable in `NavHost` |
| Navigation bar | `showTopBar` parameter | Native `UINavigationBar` (`showTopBar = false`) | Compose `TopAppBar` (`showTopBar = true`) |
| Animations | — | Native iOS push/pop + swipe-back | Compose/NavHost transitions |
| Bar styling | — | `applyBarAppearance()` utility | Material3 theming |
| Entry point | `App()` calls `coordinator.Content()` | `SceneDelegate` sets `rootViewController` | `setContent { App() }` |

This pattern delivers **native navigation UX on both platforms** while keeping all screen code shared, and the `@Composable fun Content()` bridge makes the Android navigation library completely interchangeable.
