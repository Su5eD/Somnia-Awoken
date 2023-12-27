import me.modmuss50.mpp.ReleaseType
import java.time.LocalDateTime

plugins {
    java
    `maven-publish`
    id("net.neoforged.gradle.userdev") version "7.0.+"
    id("wtf.gofancy.koremods.gradle") version "2.0.0"
    id("me.modmuss50.mod-publish-plugin") version "0.3.+"
    id("wtf.gofancy.git-changelog") version "1.1.+"
    id("me.qoomon.git-versioning") version "6.3.+"
}

val versionMc: String by project
val neoVersion: String by project

val curseForgeId: String by project
val modrinthId: String by project

val versionDarkUtils: String by project
val versionCurios: String by project
val versionBookshelf: String by project
val versionRunelic: String by project

group = "dev.su5ed"
version = "0.0.0-SNAPSHOT"

gitVersioning.apply {
    rev {
        version = "$versionMc-\${describe.tag.version.major}.\${describe.tag.version.minor}.\${describe.tag.version.patch.plus.describe.distance}"
    }
}

java {
    withSourcesJar()

    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

println("Configured version: $version, Java: ${System.getProperty("java.version")}, JVM: ${System.getProperty("java.vm.version")} (${System.getProperty("java.vendor")}), Arch: ${System.getProperty("os.arch")}")
minecraft {
    accessTransformers.file("src/main/resources/META-INF/accesstransformer.cfg")
}

runs {
    configureEach {
        systemProperty("forge.logging.markers", "REGISTRIES")
        systemProperty("forge.logging.console.level", "debug")

        modSource(project.sourceSets.main.get())
    }

    create("client")

    create("server") {
        programArgument("--nogui")
    }
}

repositories {
    maven {
        name = "BlameJared"
        url = uri("https://maven.blamejared.com")
    }
    maven {
        name = "Curios"
        url = uri("https://maven.theillusivec4.top")
    }
    maven {
        name = "Progwml6 maven"
        url = uri("https://dvs1.progwml6.com/files/maven/")
    }
    maven {
        name = "Garden of Fancy Releases"
        url = uri("https://maven.gofancy.wtf/releases")
    }
    mavenLocal()
}

dependencies {
    implementation(group = "net.neoforged", name = "neoforge", version = neoVersion)

    koremods(group = "wtf.gofancy.koremods", name = "koremods-modlauncher", version = "2.0.0")

    compileOnly(group = "top.theillusivec4.curios", name = "curios-neoforge", version = versionCurios)
}

tasks {
    jar {
        manifest {
            attributes(
                "Specification-Title" to "Somnia Awoken",
                "Specification-Vendor" to "Su5eD",
                "Specification-Version" to 1,
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version,
                "Implementation-Vendor" to "Su5eD",
                "Implementation-Timestamp" to LocalDateTime.now()
            )
        }
    }
}

publishMods {
    file.set(tasks.jar.flatMap { it.archiveFile })
    changelog.set(provider { project.changelog.generateChangelog(1, true) })
    type.set(providers.environmentVariable("PUBLISH_RELEASE_TYPE").map(ReleaseType::of).orElse(ReleaseType.STABLE))
    modLoaders.add("forge")
    dryRun.set(!providers.environmentVariable("CI").isPresent)
    displayName.set("Somnia Awoken ${project.version}")

    curseforge {
        accessToken.set(providers.environmentVariable("CURSEFORGE_TOKEN"))
        projectId.set(curseForgeId)
        minecraftVersions.add(versionMc)
        requires { slug.set("koremods") }
        optional { slug.set("cyclic") }
        optional { slug.set("comforts") }
        optional { slug.set("coffee-mod") }
        optional { slug.set("coffee-spawner") }
        optional { slug.set("dark-utilities") }
        optional { slug.set("sleeping-bags") }
    }
    modrinth {
        accessToken.set(providers.environmentVariable("MODRINTH_TOKEN"))
        projectId.set(modrinthId)
        minecraftVersions.add(versionMc)
        requires {
            slug.set("koremods")
        }
    }
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
