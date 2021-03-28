import com.matthewprenger.cursegradle.CurseArtifact
import com.matthewprenger.cursegradle.CurseProject
import com.matthewprenger.cursegradle.CurseRelation
import com.modrinth.minotaur.TaskModrinthUpload
import net.minecraftforge.gradle.common.util.RunConfig
import net.minecraftforge.gradle.userdev.DependencyManagementExtension
import net.minecraftforge.gradle.userdev.UserDevExtension
import java.time.LocalDateTime

buildscript {
    repositories {
        maven(url = "https://files.minecraftforge.net/maven")
        jcenter()
        mavenCentral()
    }
    dependencies {
        classpath(group = "net.minecraftforge.gradle", name = "ForgeGradle", version = "4.+") {
            isChanging = true
        }
    }
}
plugins {
    `java-library`
    eclipse
    `maven-publish`
    id("com.matthewprenger.cursegradle") version "1.4.0"
    id("com.modrinth.minotaur") version "1.1.0"
}
apply(plugin = "net.minecraftforge.gradle")

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

val versionMc: String by project
val versionMajor: String by project
val versionMinor: String by project
val versionPatch: String by project
val versionClassifier: String by project
val versionType: String = versionClassifier.split(".")[0]

val versionDarkUtils: String by project
val versionCurios: String by project
val versionBookshelf: String by project
val versionRunelic: String by project
val curseForgeId: String by project
val buildNumber: String = System.getenv("GITHUB_RUN_NUMBER") ?: ""
version = versionMc + "-" + versionMajor + "." + versionMinor + (if (versionPatch != "0") ".$versionPatch" else "") + (if (buildNumber.isNotEmpty()) ".$buildNumber" else "") + if (versionClassifier.isNotEmpty()) "-$versionClassifier" else ""
group = "mods.su5ed"

val versionRaw: String = version.toString().split("-")[1]
val releaseClassifier: String = if (versionType.isNotEmpty()) versionType else "release"

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
        name = "CurseMaven"
        url = uri("https://www.cursemaven.com")
    }
    maven {
        name = "BlameJared"
        url = uri("https://maven.blamejared.com")
    }
    maven {
        name = "Curios"
        url = uri("https://maven.theillusivec4.top")
    }
}

dependencies {
    "minecraft"("net.minecraftforge:forge:1.16.5-36.1.2")

    val fg: DependencyManagementExtension = project.extensions["fg"] as DependencyManagementExtension
    "compile"(fg.deobf("net.darkhax.darkutilities:DarkUtilities-1.16.5:$versionDarkUtils"))
    "compile"(fg.deobf("top.theillusivec4.curios:curios-forge:$versionCurios"))
    "compile"(fg.deobf("net.darkhax.bookshelf:Bookshelf-1.16.5:$versionBookshelf"))
    "compile"(fg.deobf("net.darkhax.runelic:Runelic-1.16.5:$versionRunelic"))
    //"compile"(fg.deobf("curse.maven:sleeping-bags-384485:3105540"))
    //"compile"(fg.deobf("curse.maven:comforts-276951:3156807"))
    //"compile"(fg.deobf("curse.maven:cyclic-239286:3168180"))
}

tasks.named<Copy>("processResources") {
    from(sourceSets["main"].resources) {
        include("META-INF/mods.toml")

        expand("version" to project.version)
    }
}

tasks.named<Jar>("jar") {
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

fun getVersionDisplayName(): String {
    val name = "Somnia Awoken"
    val classifier: String
    val parts: List<String> = versionClassifier.split(".")
    val classifierName = parts[0]
    var firstLetter = classifierName.substring(0, 1)
    if (classifierName.isNotEmpty()) {
        val remainingLetters = classifierName.substring(1, classifierName.length)
        firstLetter = firstLetter.toUpperCase()
        classifier = firstLetter + remainingLetters + if (parts.size > 1) " ${parts[1]}" else ""
    } else classifier = ""

    return "$name $versionRaw $classifier"
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

tasks.register<TaskModrinthUpload>("publishModrinth") {
    token = System.getenv("MODRINTH_TOKEN") ?: project.findProperty("MODRINTH_TOKEN") as String? ?: "DUMMY"
    projectId = "BiSrUr8O"
    versionName = getVersionDisplayName()
    versionNumber = versionRaw
    uploadFile = tasks.getByName("jar")
    addLoader("forge")
    releaseType = releaseClassifier
    changelog = System.getenv("CHANGELOG")
}