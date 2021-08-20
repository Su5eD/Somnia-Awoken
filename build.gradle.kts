import com.matthewprenger.cursegradle.CurseArtifact
import com.matthewprenger.cursegradle.CurseProject
import com.matthewprenger.cursegradle.CurseRelation
import com.modrinth.minotaur.TaskModrinthUpload
import net.minecraftforge.gradle.common.util.RunConfig
import net.minecraftforge.gradle.userdev.UserDevExtension
import wtf.gofancy.fancygradle.script.extensions.deobf
import java.time.LocalDateTime

plugins {
    java
    eclipse
    `maven-publish`
    id("net.minecraftforge.gradle") version "5.+"
    id("com.matthewprenger.cursegradle") version "1.4.0"
    id("com.modrinth.minotaur") version "1.1.0"
    id("wtf.gofancy.fancygradle") version "1.0.1"
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(8))

val versionMc: String by project
val versionMajor: String by project
val versionMinor: String by project
val versionPatch: String by project
val versionClassifier: String by project
val versionType: String = versionClassifier.split(".")[0]

val versionJEI: String by project
val versionDarkUtils: String by project
val versionCurios: String by project
val versionBookshelf: String by project
val versionRunelic: String by project
val curseForgeId: String by project

version = versionMc + "-" + versionMajor + "." + versionMinor + (if (versionPatch != "0") ".$versionPatch" else "") + if (versionClassifier.isNotEmpty()) "-$versionClassifier" else ""
group = "mods.su5ed"

val versionRaw: String = version.toString().split("-")[1]
val releaseClassifier: String = versionType.ifEmpty { "release" }

configure<UserDevExtension> {
    mappings("official", "1.16.5")

    accessTransformer(file("src/main/resources/META-INF/accesstransformer.cfg"))

    runs {
        val config = Action<RunConfig> {
            properties(mapOf(
                "forge.logging.markers" to "SCAN,REGISTRIES,REGISTRYDUMP,COREMODLOG",
                "forge.logging.console.level" to "debug"
            ))
            workingDirectory = project.file("run").canonicalPath
            source(sourceSets["main"])
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
}

dependencies {
    minecraft("net.minecraftforge:forge:1.16.5-36.1.2")

    implementation(fg.deobf(group = "mezz.jei", name = "jei-1.16.5", version = versionJEI))
    compileOnly(fg.deobf(group = "net.darkhax.darkutilities", name = "DarkUtilities-1.16.5", version = versionDarkUtils))
    compileOnly(fg.deobf(group = "top.theillusivec4.curios", name = "curios-forge", version = versionCurios))
    compileOnly(fg.deobf(group = "net.darkhax.bookshelf", name = "Bookshelf-1.16.5", version = versionBookshelf))
    compileOnly(fg.deobf(group = "net.darkhax.runelic", name = "Runelic-1.16.5", version = versionRunelic))
}

tasks {
    jar {
        finalizedBy("reobfJar")
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
    
    processResources {
        filesMatching("mods.toml") {
            expand("version" to project.version)
        }
    }
    
    register<TaskModrinthUpload>("publishModrinth") {
        token = System.getenv("MODRINTH_TOKEN") ?: project.findProperty("MODRINTH_TOKEN") as String? ?: "DUMMY"
        projectId = "BiSrUr8O"
        versionName = getVersionDisplayName()
        versionNumber = versionRaw
        uploadFile = jar
        addLoader("forge")
        releaseType = releaseClassifier
        changelog = System.getenv("CHANGELOG")
    }
}

curseforge {
    apiKey = System.getenv("CURSEFORGE_TOKEN") ?: project.findProperty("CURSEFORGE_TOKEN") as String? ?: "DUMMY"
    project(closureOf<CurseProject> {
        id = curseForgeId
        changelogType = "markdown"
        changelog = System.getenv("CHANGELOG") ?: ""
        releaseType = releaseClassifier
        mainArtifact(tasks.getByName("jar"), closureOf<CurseArtifact> {
            displayName = getVersionDisplayName()
            relations(closureOf<CurseRelation> {
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

fun getVersionDisplayName(): String {
    val name = "Somnia Awoken"
    val classifier: String
    val parts: List<String> = versionClassifier.split(".")
    val classifierName = parts[0]
    if (classifierName.isNotEmpty()) {
        var firstLetter = classifierName.substring(0, 1)
        val remainingLetters = classifierName.substring(1, classifierName.length)
        firstLetter = firstLetter.toUpperCase()
        classifier = firstLetter + remainingLetters + if (parts.size > 1) " ${parts[1]}" else ""
    } else classifier = ""

    return "$name $versionRaw $classifier"
}
