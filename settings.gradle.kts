pluginManagement {
    repositories {
//        google()
        maven(url = "https://maven.fabricmc.net/") {
            name = "Fabric"
        }
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }

    plugins {
//        id 'fabric-loom' version loom_version
//        id "org.jetbrains.kotlin.jvm" version kotlin_version
        id("org.jetbrains.compose").version("1.6.10")
    }
}
