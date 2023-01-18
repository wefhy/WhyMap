import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly

// Copyright (c) 2022 wefhy

val mod_id: String by project
val maven_group: String by project
val mod_version: String by project
val minecraft_version: String by project
val yarn_mappings: String by project
val loader_version: String by project
val fabric_version: String by project

plugins {
	id ("fabric-loom") version "1.0.17"
	id ("maven-publish")
	id ("org.jetbrains.kotlin.jvm") version "1.8.0"
	id ("org.jetbrains.kotlin.plugin.serialization") version "1.8.0"
}

java {
	sourceCompatibility = JavaVersion.VERSION_17
	targetCompatibility = JavaVersion.VERSION_17
//	withSourcesJar()
}
base {
	archivesBaseName = mod_id.toLowerCaseAsciiOnly()
}
version = mod_version
group = maven_group

repositories {
	mavenCentral()
	maven(url = "https://repo.maven.apache.org/maven2/")
}

//configurations {
//	// configuration that holds jars to include in the jar
//	extraLibs
//}

val extraLibs: Configuration by configurations.creating



dependencies {
	minecraft("com.mojang:minecraft:${minecraft_version}")
	mappings("net.fabricmc:yarn:${yarn_mappings}:v2")
	modImplementation("net.fabricmc:fabric-loader:${loader_version}")
	modImplementation("net.fabricmc.fabric-api:fabric-api:${fabric_version}")
	modImplementation("net.fabricmc:fabric-language-kotlin:1.9.0+kotlin.1.8.0")

	implementation("io.ktor:ktor-server-core-jvm:2.2.2")
	implementation("io.ktor:ktor-server-cio-jvm:2.2.2")
	implementation("io.ktor:ktor-server-content-negotiation:2.2.2")
	implementation("io.ktor:ktor-serialization-kotlinx-json:2.2.2")
	implementation("io.ktor:ktor-server-html-builder:2.2.2")
	implementation("io.ktor:ktor-server-cors:2.2.2")
	implementation(group = "org.tukaani", name = "xz", version = "1.9")

	extraLibs("io.ktor:ktor-server-core-jvm:2.2.2")
	extraLibs("io.ktor:ktor-server-cio-jvm:2.2.2")
	extraLibs("io.ktor:ktor-server-content-negotiation:2.2.2")
	extraLibs("io.ktor:ktor-serialization-kotlinx-json:2.2.2")
	extraLibs("io.ktor:ktor-server-html-builder:2.2.2")
	extraLibs("io.ktor:ktor-server-cors:2.2.2")
	extraLibs(group = "org.tukaani", name = "xz", version = "1.9")
}

tasks.getByName<ProcessResources>("processResources") {
	filesMatching("fabric.mod.json") {
		expand(
			mutableMapOf("version" to project.version)
		)
	}
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
	kotlinOptions.freeCompilerArgs += "-Xcontext-receivers"
}

tasks.withType<Jar> {
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
	from("LICENSE") {
		rename { "${it}_${mod_id}"}
	}
	from(extraLibs.resolve().map { if (it.isDirectory) it else zipTree(it) })
//	from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
//	from(configurations.runtime.map({ if (it.isDirectory) it else zipTree(it) }))
//	from(configurations.implementation.map({ if (it.isDirectory()) it else zipTree(it) }))
//	from {
//		configurations.extraLibs.collect { it.isDirectory() ? it : zipTree(it) }
//	}
}

val yarnBuild = task<Exec>("yarnBuild") {
	workingDir = file("src-vue")
	commandLine("yarn", "build")
}

val copyDistFolder = tasks.register<Copy>("copyDistFolder") {
	from(file("src-vue/dist"))
	into(file("src/main/resources/web"))
	exclude("*.js.map")
}

val deleteOldWeb = tasks.register<Delete>("deleteOldWeb") {
	delete(files("src/main/resources/web/css"))
	delete(files("src/main/resources/web/js"))
}

val yarnInstall = task<Exec>("yarnInstall") {
	inputs.file("src-vue/package.json")
	outputs.dir("src-vue/node_modules")
	workingDir = file("src-vue")
	commandLine("yarn", "install")
}

//abstract class YarnServeTask : DefaultTask() {
//	@TaskAction
//	fun serve() {
//
//	}
//}
//
//tasks.register<YarnServeTask>("yarnServe") {
//
//}

tasks.register<Exec>("serve") {
	workingDir = file("src-vue")
	commandLine("yarn", "serve")
}

tasks {
	"runClient" {
		dependsOn(copyDistFolder)
	}
	"build" {
		dependsOn(copyDistFolder)
	}
	"processResources" {
		dependsOn(copyDistFolder)
	}
	"copyDistFolder" {
		dependsOn(deleteOldWeb)
		dependsOn(yarnBuild)
	}
	"yarnBuild" {
		dependsOn(yarnInstall)
	}
}
