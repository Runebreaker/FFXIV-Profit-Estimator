pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "FFXIV_Profit_Estimator"
include(":FFXIV_Profit_Estimator")
include(":shared")