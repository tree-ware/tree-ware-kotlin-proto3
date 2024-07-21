rootProject.name = "proto3"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io") }
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "org.tree-ware.core") {
                useModule("org.tree-ware.tree-ware-gradle-core-plugin:core-plugin:${requested.version}")
            }
        }
    }
}

