import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import io.papermc.hangarpublishplugin.model.Platforms
import java.io.ByteArrayOutputStream
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.Copy

plugins {
    `java-library`
    `maven-publish`
    id("io.papermc.hangar-publish-plugin") version "0.1.2"
    id("com.gradleup.shadow") version "8.3.6"
    id("com.modrinth.minotaur") version "2.+"
}

repositories {
    mavenLocal()
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
    maven { url = uri("https://oss.sonatype.org/content/groups/public/") }
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://repo.maven.apache.org/maven2/") }
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/Lyttle-Development/LyttleUtils")
        credentials {
            username = project.findProperty("GPR_USER") as String? ?: System.getenv("GPR_USER")
            password = project.findProperty("GPR_API_KEY") as String? ?: System.getenv("GPR_API_KEY")
        }
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:" + (property("paperVersion") as String) + "-R0.1-SNAPSHOT")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7") { exclude(group = "org.bukkit", module = "bukkit") }
    compileOnly("net.luckperms:api:5.4")
    implementation("com.lyttledev:lyttleutils:1.1.7")
}

group = "com.lyttledev"
version = (property("pluginVersion") as String)
description = "LyttleEssentials"
java.sourceCompatibility = JavaVersion.VERSION_21

// --- Shadow JAR configuration ---
tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier.set("")
    configurations = listOf(project.configurations.runtimeClasspath.get())
    dependencies {
        include(dependency("com.lyttledev:lyttleutils"))
    }
}

// Disable regular jar to prevent accidental use
tasks.named<Jar>("jar") {
    enabled = false
}

// Ensure build depends on shadowJar and copyContents
tasks.named("build") {
    dependsOn("shadowJar", "copyContents")
}

// --- Encoding setup for Java and Javadoc ---
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
}

// --- Resources folder handling ---
val folderToDelete = project.file("src/main/resources/#defaults")
val sourceFolder = project.file("src/main/resources")
val destinationFolder = project.file("src/main/resources/#defaults")

val deleteFolder by tasks.registering(Delete::class) {
    delete(folderToDelete)
    doLast {
        println("Deleted folder: $folderToDelete")
    }
}

val copyContents by tasks.registering(Copy::class) {
    dependsOn(deleteFolder)
    doFirst {
        println("Creating destination folder: $destinationFolder")
        destinationFolder.mkdirs()
    }
    from(sourceFolder) {
        exclude("#defaults/**")
        exclude("plugin.yml")
    }
    into(destinationFolder)
    doLast {
        println("Copied contents from $sourceFolder to $destinationFolder")
    }
}

// Ensure processResources depends on copyContents
tasks.named("processResources") {
    dependsOn(copyContents)
}

// --- Helper methods for Git integration ---
fun executeGitCommand(vararg command: String): String {
    val byteOut = ByteArrayOutputStream()
    exec {
        commandLine = listOf("git", *command)
        standardOutput = byteOut
    }
    return byteOut.toString(Charsets.UTF_8.name()).trim()
}

fun latestCommitMessage(): String {
    return executeGitCommand("log", "-1", "--pretty=%B")
}

// --- Versioning logic based on CHANNEL environment variable ---
val envChannel: String = System.getenv("CHANNEL") ?: "Alpha"
val runNumber: String? = System.getenv("GITHUB_RUN_NUMBER")

val versionString: String = when (envChannel) {
    "Release" -> version.toString()
    "Beta" -> if (runNumber != null) "${version}-SNAPSHOT.$runNumber" else "${version}-SNAPSHOT"
    else -> if (runNumber != null) "${version}-${envChannel.uppercase()}.$runNumber" else "$version-${envChannel.uppercase()}"
}

// --- Version expansion in plugin.yml ---
tasks.named<ProcessResources>("processResources") {
    filesMatching("plugin.yml") {
        expand("projectVersion" to versionString)
    }
}

// --- Publishing configuration for Maven (GitHub Packages with ShadowJar) ---
publishing {
    publications {
        create<MavenPublication>("maven") {
            artifact(tasks.named<ShadowJar>("shadowJar").get()) {
                classifier = null // main artifact, no classifier
            }
            groupId = project.group.toString()
            artifactId = "lyttleessentials"
            version = versionString
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Lyttle-Development/LyttleEssentials")
            credentials {
                username = System.getenv("GPR_USER") ?: project.findProperty("gpr.user") as String?
                password = System.getenv("GPR_API_KEY") ?: project.findProperty("gpr.key") as String?
            }
        }
    }
}

// --- Hangar Publish Configuration ---
val changelogContent: String = latestCommitMessage()

println("Version: $versionString")
println("Channel: $envChannel")

hangarPublish {
    publications.register("plugin") {
        version.set(versionString)
        channel.set(envChannel)
        changelog.set(changelogContent)
        id.set("LyttleEssentials")
        apiKey.set(System.getenv("HANGAR_API_TOKEN"))
        platforms {
            register(Platforms.PAPER) {
                jar.set(tasks.named<ShadowJar>("shadowJar").flatMap { it.archiveFile })
                val versions: List<String> = (property("paperVersion") as String)
                    .split(",")
                    .map { it.trim() }
                platformVersions.set(versions)
            }
        }
    }
}

// --- Modrinth Publish Configuration ---
modrinth {
    token.set(System.getenv("MODRINTH_API_TOKEN")) // Token from workflow secrets
    projectId.set("lyttleessentials") // Replace with your Modrinth project slug or ID
    versionNumber.set(versionString)
    changelog.set(changelogContent)
    uploadFile.set(tasks.named<ShadowJar>("shadowJar").flatMap { it.archiveFile })
    gameVersions.set((property("paperVersion") as String).split(",").map { it.trim() })
    versionType.set(
        when (envChannel.lowercase()) {
            "release" -> "release"
            "beta" -> "beta"
            "alpha" -> "alpha"
            else -> "alpha"
        }
    )
    loaders.set(listOf("paper")) // Or add "spigot", "bukkit" etc as appropriate
}