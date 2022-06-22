pluginManagement {
    repositories {
        gradlePluginPortal()
        maven {
            name = "MinecraftForge"
            url = uri("https://maven.minecraftforge.net/")
        }
        maven {
            name = "FancyGradle"
            url = uri("https://gitlab.com/api/v4/projects/26758973/packages/maven")
        }
        maven { 
            name = "Parchment"
            url = uri("https://maven.parchmentmc.org")
        }
        maven {
            name = "Garden of Fancy"
            url = uri("https://maven.gofancy.wtf/releases")
        }
    }
}

rootProject.name = "Somnia-Awoken"
