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
    }
}

rootProject.name = "Somnia-Awoken"
