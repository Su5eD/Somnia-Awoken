import com.matthewprenger.cursegradle.CurseArtifact
import com.matthewprenger.cursegradle.CurseProject
import com.matthewprenger.cursegradle.CurseRelation
import com.modrinth.minotaur.dependencies.DependencyType
import com.modrinth.minotaur.dependencies.ModDependency
import fr.brouillard.oss.jgitver.GitVersionCalculator
import fr.brouillard.oss.jgitver.Strategies
import net.minecraftforge.gradle.common.util.RunConfig
import wtf.gofancy.fancygradle.script.extensions.deobf
import java.time.LocalDateTime

buildscript {
    dependencies {
        classpath(group = "fr.brouillard.oss", name = "jgitver", version = "0.14.+")
    }
}

plugins {
    java
    `maven-publish`
    id("net.minecraftforge.gradle") version "5.1.+"
    id("org.parchmentmc.librarian.forgegradle") version "1.+"
    id("wtf.gofancy.fancygradle") version "1.1.+"
    id("wtf.gofancy.koremods.gradle") version "0.1.19"
    id("com.matthewprenger.cursegradle") version "1.4.+"
    id("com.modrinth.minotaur") version "2.+"
}

val versionMc: String by project
val versionForge: String by project

val curseForgeId: String by project
val modrinthId: String by project

val versionJEI: String by project
val versionDarkUtils: String by project
val versionCurios: String by project
val versionBookshelf: String by project
val versionRunelic: String by project

val baseVersion = getGitVersion()
version = "$versionMc-$baseVersion"
group = "dev.su5ed"

val publishVersionName = "Somnia Awoken ${project.version}"
val publishReleaseType = System.getenv("PUBLISH_RELEASE_TYPE") ?: "release"

java {
    withSourcesJar()
    
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

minecraft {
    mappings("parchment", "2022.06.19-$versionMc")

    accessTransformer(file("src/main/resources/META-INF/accesstransformer.cfg"))

    runs {
        val config = Action<RunConfig> {
            properties(mapOf(
                "forge.logging.console.level" to "debug"
            ))
            workingDirectory = project.file("run").canonicalPath
            source(sourceSets["main"])
            forceExit = false
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
}

dependencies {
    minecraft("net.minecraftforge:forge:$versionMc-$versionForge")
    
    koremods(group = "wtf.gofancy.koremods", name = "koremods-modlauncher", version = "0.4.7")

    implementation(fg.deobf(group = "mezz.jei", name = "jei-1.18.2-forge-api", version = versionJEI))
    compileOnly(fg.deobf(group = "net.darkhax.darkutilities", name = "DarkUtilities-Forge-1.18.2", version = versionDarkUtils))
    compileOnly(fg.deobf(group = "top.theillusivec4.curios", name = "curios-forge", version = versionCurios))
    compileOnly(fg.deobf(group = "net.darkhax.bookshelf", name = "Bookshelf-Forge-1.18.2", version = versionBookshelf))
    compileOnly(fg.deobf(group = "net.darkhax.runelic", name = "Runelic-Forge-1.18.2", version = versionRunelic))
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
    versionName.set(publishVersionName)
    versionType.set(publishReleaseType)
    uploadFile.set(tasks.jar.get())
    gameVersions.addAll(versionMc)
    dependencies.add(ModDependency("EWmBPx3X", DependencyType.REQUIRED))
    // TODO changelog
}

curseforge {
    apiKey = System.getenv("CURSEFORGE_TOKEN") ?: "UNKNOWN"
    project(closureOf<CurseProject> {
        id = curseForgeId
        changelogType = "markdown"
//        changelog = System.getenv("CHANGELOG") ?: ""
        releaseType = publishReleaseType
        mainArtifact(tasks.jar.get(), closureOf<CurseArtifact> {
            displayName = publishVersionName
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

fun getGitVersion(): String {
    val jgitver = GitVersionCalculator.location(rootDir)
        .setNonQualifierBranches("mc-1.18")
        .setStrategy(Strategies.SCRIPT)
        .setScript("print \"\${metadata.CURRENT_VERSION_MAJOR};\${metadata.CURRENT_VERSION_MINOR};\${metadata.CURRENT_VERSION_PATCH + metadata.COMMIT_DISTANCE}\"")
    return jgitver.version
}
