// Copyright (c) 2022 wefhy

object Constants {
	const val modid = "WhyMap"
	const val group = "dev.wefhy"
	const val name = modid
	const val version = "1.0.0"
	const val minecraftVersion="1.18.2"
	const val yarnMappings="1.18.2+build.1"
	const val loaderVersion="0.13.3"
	const val fabricVersion="0.47.8+1.18.2"
}

plugins {
	id ("fabric-loom") version "0.11-SNAPSHOT"
	id ("maven-publish")
	id ("org.jetbrains.kotlin.jvm") version "1.7.10"
	id ("org.jetbrains.kotlin.plugin.serialization") version "1.7.10"
}

java {
	sourceCompatibility = JavaVersion.VERSION_17
	targetCompatibility = JavaVersion.VERSION_17
	withSourcesJar()
}
base {
	archivesBaseName = Constants.name
}
version = Constants.version
group = Constants.group

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
	minecraft ("com.mojang:minecraft:${Constants.minecraftVersion}")
	mappings ("net.fabricmc:yarn:${Constants.yarnMappings}:v2")
	modImplementation ("net.fabricmc:fabric-loader:${Constants.loaderVersion}")
	modImplementation ("net.fabricmc.fabric-api:fabric-api:${Constants.fabricVersion}")
	modImplementation("net.fabricmc:fabric-language-kotlin:1.8.2+kotlin.1.7.10")

	implementation ("io.ktor:ktor-server-core:2.0.3")
	implementation ("io.ktor:ktor-server-cio:2.0.3")
	implementation ("io.ktor:ktor-server-content-negotiation:2.0.3")
	implementation ("io.ktor:ktor-serialization-kotlinx-json:2.0.3")
	implementation ("io.ktor:ktor-server-html-builder:2.0.3")
	implementation ("io.ktor:ktor-server-cors:2.0.3")
	implementation  (group = "org.tukaani", name = "xz", version = "1.9")

	extraLibs ("io.ktor:ktor-server-core:2.0.3")
	extraLibs ("io.ktor:ktor-server-cio:2.0.3")
	extraLibs ("io.ktor:ktor-server-content-negotiation:2.0.3")
	extraLibs ("io.ktor:ktor-serialization-kotlinx-json:2.0.3")
	extraLibs ("io.ktor:ktor-server-html-builder:2.0.3")
	extraLibs ("io.ktor:ktor-server-cors:2.0.3")
	extraLibs  (group = "org.tukaani", name = "xz", version = "1.9")


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

//kotlin {
//	targets.all {
//		compilatons.all {
//			kotlinOptions {
//				freeCompilerArgs = freeCompilerArgs + "-Xcontext-receivers"
//			}
//		}
//	}
//}

tasks.withType<Jar> {
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
	from("LICENSE") {
		rename { "${it}_${Constants.name}"}
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
	"sourcesJar" {
		dependsOn(copyDistFolder)
	}
	"copyDistFolder" {
		dependsOn(deleteOldWeb)
		dependsOn(yarnBuild)
	}
}


//val fatJar = task("fatJar", type = Jar::class) {
//	baseName = "${project.name}-fat"
//	manifest {
//		attributes["Implementation-Title"] = "Gradle Jar File Example"
//		attributes["Implementation-Version"] = version
//		attributes["Main-Class"] = "com.mkyong.DateUtils"
//	}
////	from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
//	from(extraLibs.resolve().map { if (it.isDirectory) it else zipTree(it) })
//	with(tasks.jar.get() as CopySpec)
//}
//
//tasks {
//	"build" {
//		dependsOn(fatJar)
//	}
//}