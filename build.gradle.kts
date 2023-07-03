import com.matthewprenger.cursegradle.CurseArtifact
import com.matthewprenger.cursegradle.CurseProject
import com.matthewprenger.cursegradle.CurseRelation
import net.minecraftforge.gradle.common.util.RunConfig
import wtf.gofancy.fancygradle.script.extensions.deobf
import java.time.LocalDateTime

plugins {
    java
    `maven-publish`
    id("net.minecraftforge.gradle") version "[6.0,6.2)"
    id("org.parchmentmc.librarian.forgegradle") version "1.+"
    id("wtf.gofancy.fancygradle") version "1.1.+"
    id("wtf.gofancy.koremods.gradle") version "0.2.0"
    id("wtf.gofancy.git-changelog") version "1.1.+"
    id("com.matthewprenger.cursegradle") version "1.4.+"
    id("com.modrinth.minotaur") version "2.+"
    id("me.qoomon.git-versioning") version "6.3.+"
}

val versionMc: String by project
val versionForge: String by project

val curseForgeId: String by project
val modrinthId: String by project

val versionDarkUtils: String by project
val versionCurios: String by project
val versionBookshelf: String by project
val versionRunelic: String by project

group = "dev.su5ed"
version = "0.0.0-SNAPSHOT"

val publishReleaseType = System.getenv("PUBLISH_RELEASE_TYPE") ?: "release"
val changelogText = changelog.generateChangelog(1, true)

gitVersioning.apply {
    rev {
        version = "$versionMc-\${describe.tag.version.major}.\${describe.tag.version.minor}.\${describe.tag.version.patch.plus.describe.distance}"
    }
}

java {
    withSourcesJar()
    
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

minecraft {
    mappings("parchment", "2023.06.26-1.20.1")

    accessTransformer(file("src/main/resources/META-INF/accesstransformer.cfg"))

    runs {
        val config = Action<RunConfig> {
            properties(mapOf(
                "forge.logging.console.level" to "debug"
            ))
            workingDirectory = project.file("run").canonicalPath
            source(sourceSets.main.get())
        }

        create("client", config)
        create("server", config)
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
    minecraft("net.minecraftforge:forge:$versionMc-$versionForge")
    
    koremods(group = "wtf.gofancy.koremods", name = "koremods-modlauncher", version = "0.7.0")

    compileOnly(fg.deobf(group = "top.theillusivec4.curios", name = "curios-forge", version = versionCurios))
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

modrinth {
    token.set(System.getenv("MODRINTH_TOKEN"))
    projectId.set(modrinthId)
    versionName.set("Somnia Awoken ${project.version}")
    versionType.set(publishReleaseType)
    uploadFile.set(tasks.jar.get())
    gameVersions.addAll(versionMc)
    dependencies { 
        required.project("koremods")
    }
    changelog.set(changelogText)
}

curseforge {
    apiKey = System.getenv("CURSEFORGE_TOKEN") ?: "UNKNOWN"
    project(closureOf<CurseProject> {
        id = curseForgeId
        changelogType = "markdown"
        changelog = changelogText
        releaseType = publishReleaseType
        mainArtifact(tasks.jar.get(), closureOf<CurseArtifact> {
            displayName = "Somnia Awoken ${project.version}"
            relations(closureOf<CurseRelation> {
                requiredDependency("koremods")
                
                optionalDependency("cyclic")
                optionalDependency("comforts")
                optionalDependency("coffee-mod")
                optionalDependency("coffee-spawner")
                optionalDependency("dark-utilities")
                optionalDependency("sleeping-bags")
            })
        })
        addGameVersion("Forge")
        addGameVersion(versionMc)
    })
}

publishing {
    publications { 
        register<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
