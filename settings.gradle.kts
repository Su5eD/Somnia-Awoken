pluginManagement {
    repositories {
        gradlePluginPortal()
        maven {
            name = "MinecraftForge"
            url = uri("https://maven.minecraftforge.net/")
        }
        maven { 
            name = "Parchment"
            url = uri("https://maven.parchmentmc.org")
        }
        maven {
            name = "Su5eD"
            url = uri("https://maven.su5ed.dev/releases")
        }
        maven {
            name = "Garden of Fancy"
            url = uri("https://maven.gofancy.wtf/releases")
        }
    }
    plugins {
        id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
    }
}

rootProject.name = "Somnia-Awoken"
