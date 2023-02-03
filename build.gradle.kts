import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.currentBuildId
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly
import java.io.ByteArrayOutputStream

// Copyright (c) 2022 wefhy

val mod_id: String by project
val maven_group: String by project
val minecraft_version: String by project
val yarn_mappings: String by project
val loader_version: String by project
val fabric_version: String by project

plugins {
	id ("fabric-loom") version "1.1.7"
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
version = getCurrentVersion()
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

val isReleaseBuild = false
val experimentalOptimizations = false

dependencies {
	minecraft("com.mojang", "minecraft", minecraft_version)
	mappings("net.fabricmc", "yarn", yarn_mappings, classifier = "v2")
	mappings("net.fabricmc:yarn:$yarn_mappings:v2")
	modImplementation("net.fabricmc", "fabric-loader", loader_version)
	modImplementation("net.fabricmc.fabric-api", "fabric-api", fabric_version)
	modImplementation("net.fabricmc", "fabric-language-kotlin", "1.9.0+kotlin.1.8.0")


	extraLibs(implementation("io.ktor", "ktor-server-core-jvm", "2.2.2"))
	extraLibs(implementation("io.ktor", "ktor-server-cio-jvm", "2.2.2"))
	extraLibs(implementation("io.ktor", "ktor-server-content-negotiation", "2.2.2"))
	extraLibs(implementation("io.ktor", "ktor-serialization-kotlinx-json", "2.2.2"))
	extraLibs(implementation("io.ktor", "ktor-server-html-builder", "2.2.2"))
	extraLibs(implementation("io.ktor", "ktor-server-cors", "2.2.2"))

	extraLibs(implementation("org.tukaani", "xz", "1.9"))
//	extraLibs(implementation("ar.com.hjg", "pngj", "2.1.0"))

	testImplementation("org.junit.jupiter", "junit-jupiter-api", "5.8.1")
	testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", "5.8.1")
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
	if (isReleaseBuild) {
		kotlinOptions.freeCompilerArgs += "-Xno-call-assertions"
		kotlinOptions.freeCompilerArgs += "-Xno-receiver-assertions"
		kotlinOptions.freeCompilerArgs += "-Xno-param-assertions"
	}
	if (experimentalOptimizations) {
		kotlinOptions.freeCompilerArgs += "-Xlambdas=indy"
		kotlinOptions.freeCompilerArgs += "-Xsam-conversions=indy"
	}
//	kotlinOptions.freeCompilerArgs += "-Xtype-enhancement-improvements-strict-mode"
//	kotlinOptions.freeCompilerArgs += "-Xenhance-type-parameter-types-to-def-not-null"

//	kotlinOptions.freeCompilerArgs += "-no-jdk"
//	kotlinOptions.freeCompilerArgs += "-no-stdlib"
}

tasks.test {
	useJUnitPlatform()
}

tasks.withType<Jar> {
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
	from("LICENSE") {
		rename { "${it}_${mod_id}"}
	}
	from(extraLibs.resolve().map { if (it.isDirectory) it else zipTree(it) })
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
	delete(files("src/main/resources/web/assets"))
}

val yarnInstall = task<Exec>("yarnInstall") {
	inputs.file("src-vue/package.json")
	outputs.dir("src-vue/node_modules")
	workingDir = file("src-vue")
	commandLine("yarn", "install")
}

fun getCurrentVersion(): String {
	val stdout = ByteArrayOutputStream()
	exec {
		executable("/bin/sh")
		args("-c", "echo `git describe --tags`")
//		commandLine("git", "describe", "--tags")
		standardOutput = stdout
	}
	return stdout.toString().trim()
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
