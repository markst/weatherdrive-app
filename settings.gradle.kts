pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/swift-export-experimental")
    }
}

rootProject.name = "weatherdrive-app"
include(":composeApp")

includeBuild("radioplayer-kt")
