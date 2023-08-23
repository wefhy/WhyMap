
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly
import java.io.ByteArrayOutputStream
import java.security.MessageDigest

// Copyright (c) 2022 wefhy

val mod_id: String by project
val maven_group: String by project
val minecraft_version: String by project
val yarn_mappings: String by project
val loader_version: String by project
val fabric_version: String by project

plugins {
	id ("fabric-loom") version "1.3.8"
	id ("maven-publish")
	kotlin("jvm") version "1.9.0"
	id ("org.jetbrains.kotlin.plugin.serialization") version "1.9.0"
	id("com.github.johnrengelman.shadow") version "8.1.1"
}

loom {
	runs {
		getByName("client") {
			vmArgs("-Xmx2G") // limit the heap memory to 512MB
		}
	}
}

java {
	sourceCompatibility = JavaVersion.VERSION_17
	targetCompatibility = JavaVersion.VERSION_17
//	withSourcesJar()
}
base {
	archivesName.set(mod_id.toLowerCaseAsciiOnly())
}
version = getCurrentVersion()
group = maven_group

repositories {
	mavenCentral()
	maven(url = "https://repo.maven.apache.org/maven2/")
	maven(url = "https://maven.shedaniel.me/") //TODO use maven local or multi project build to build from sources
	maven(url = "https://maven.terraformersmc.com") //TODO use maven local or multi project build to build from sources
}

//configurations {
//	// configuration that holds jars to include in the jar
//	extraLibs
//}

val extraLibs: Configuration by configurations.creating
//configurations.implementation.get().extendsFrom(extraLibs)

val isReleaseBuild = false
val experimentalOptimizations = false

dependencies {
	minecraft("com.mojang", "minecraft", minecraft_version)
	mappings("net.fabricmc", "yarn", yarn_mappings, classifier = "v2")
	mappings("net.fabricmc:yarn:$yarn_mappings:v2")
	modImplementation("net.fabricmc", "fabric-loader", loader_version)
	modImplementation("net.fabricmc.fabric-api", "fabric-api", fabric_version)
	modImplementation("net.fabricmc", "fabric-language-kotlin", "1.10.8+kotlin.1.9.0")

	modCompileOnly ("me.shedaniel.cloth", "cloth-config-fabric","10.0.96") {
		exclude (group = "net.fabricmc.fabric-api")
	}
	modCompileOnlyApi("com.terraformersmc", "modmenu", "6.2.0")

	val ktorVersion = "2.3.2"
	extraLibs(implementation("io.ktor", "ktor-server-core-jvm", ktorVersion))
	extraLibs(implementation("io.ktor", "ktor-server-cio-jvm", ktorVersion))
	extraLibs(implementation("io.ktor", "ktor-server-content-negotiation", ktorVersion))
	extraLibs(implementation("io.ktor", "ktor-serialization-kotlinx-json", ktorVersion))
	extraLibs(implementation("io.ktor", "ktor-server-html-builder", ktorVersion))
	extraLibs(implementation("io.ktor", "ktor-server-cors", ktorVersion))

	extraLibs(implementation("org.tukaani", "xz", "1.9"))
	extraLibs(implementation("com.akuleshov7", "ktoml-core", "0.5.0"))
//	extraLibs(implementation("org.ojalgo", "ojalgo", "53.0.0"))
//	extraLibs(implementation("ai.hypergraph", "kotlingrad", "0.4.7"))
//	extraLibs(implementation("ar.com.hjg", "pngj", "2.1.0"))

	testImplementation(platform("org.junit:junit-bom:5.9.3"))
	testImplementation("org.junit.jupiter:junit-jupiter")
	testImplementation("com.github.doyaaaaaken", "kotlin-csv-jvm", "1.9.1")
	testImplementation("ar.com.hjg", "pngj", "2.1.0")
    implementation(kotlin("stdlib-jdk8"))
}

@Suppress("UnstableApiUsage")
tasks.getByName<ProcessResources>("processResources") {
	filesMatching("fabric.mod.json") {
		expand(
			mutableMapOf("version" to project.version)
		)
	}
}

tasks.withType<KotlinCompile>().all {
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
}

tasks.shadowJar {
//	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
//	from("LICENSE") {
//		rename { "${it}_${mod_id}"}
//	}
//	minimize {
//		minimize is incompatible with kotlin? https://github.com/johnrengelman/shadow/issues/688
//	}
	archiveClassifier.set("slim")
//	This saves 0.1MB
//	exclude("**/*.kotlin_metadata")
//	exclude ("**/*.kotlin_module")
//	exclude ("META-INF/maven/**")

	dependencies {
		exclude(dependency("org.jetbrains.kotlin:.*"))
		exclude(dependency("org.jetbrains.kotlinx:kotlinx-coroutines-.*"))
		exclude(dependency("org.slf4j:.*"))
	}
	configurations = listOf(extraLibs)
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

val yarnBuild = task<Exec>("yarnBuild") {
	inputs.dir("src-vue/src")
	inputs.dir("src-vue/public")
	inputs.files("src-vue/index.html", "src-vue/vite.config.js", "src-vue/vue.config.js", "src-vue/jsconfig.json")
	outputs.dir("src-vue/dist")
	workingDir = file("src-vue")
	commandLine("yarn", "build")
}

val threeBuild = task<Exec>("threeBuild") {
	inputs.dir("src-threejs/src")
	inputs.dir("src-threejs/public")
	inputs.files("src-threejs/index.html", "src-threejs/vite.config.js", "src-threejs/vue.config.js", "src-threejs/jsconfig.json")
	outputs.dir("src-threejs/dist")
	workingDir = file("src-threejs")
	commandLine("npm", "run", "build")
}

val copyDistFolder = tasks.register<Copy>("copyDistFolder") {
	from(file("src-vue/dist"))
	into(file("src/main/resources/web"))
	exclude("*.js.map")
}

val copyThreeJsFolder = tasks.register<Copy>("copyThreeJsFolder") {
	from(file("src-threejs/dist"))
	into(file("src/main/resources/three"))
	exclude("*.js.map")
}

val deleteOldWeb = tasks.register<Delete>("deleteOldWeb") {
//	delete(files("src/main/resources/web/css"))
//	delete(files("src/main/resources/web/js"))
	delete(files("src/main/resources/web/assets"))
}

val deleteOldThree = tasks.register<Delete>("deleteOldThree") {
//	delete(files("src/main/resources/three/css"))
//	delete(files("src/main/resources/three/js"))
	delete(files("src/main/resources/three/assets"))
}

val yarnInstall = task<Exec>("yarnInstall") {
	inputs.file("src-vue/package.json")
	outputs.dir("src-vue/node_modules")
	workingDir = file("src-vue")
	commandLine("yarn", "install")
}

val npmInstall = task<Exec>("npmInstall") {
	inputs.file("src-threejs/package.json")
	outputs.dir("src-threejs/node_modules")
	workingDir = file("src-threejs")
	commandLine("npm", "install")
}

val md = MessageDigest.getInstance("MD5")!!

fun listFilesAndMd5(inDir: File, extension: String): String {
	return inDir
		.listFiles()!!
		.filter { it.extension == extension }
		.sorted()
		.joinToString("\n") {
			val md5 = md.digest(it.readBytes())
			"${it.nameWithoutExtension}=${md5.toHex()}"
		}
}

val blockMappingsList = task("blockMappingsList") {
	val inDir = File("src/main/resources/blockmappings")
	val outFile = File("src/main/resources/blockmappings.txt")
	inputs.dir(inDir.path)
	outputs.file(outFile.path)
	doLast {
		if (!inDir.exists()) return@doLast outFile.writeText("")
		outFile.writeText(listFilesAndMd5(inDir, "blockmap"))
	}
}

val biomeMappingsList = task("biomeMappingsList") {
	val inDir = File("src/main/resources/biomemappings")
	val outFile = File("src/main/resources/biomemappings.txt")
	inputs.dir(inDir.path)
	outputs.file(outFile.path)
	doLast {
		if (!inDir.exists()) return@doLast outFile.writeText("")
		outFile.writeText(listFilesAndMd5(inDir, "biomemap"))
	}
}

val newMappings = task("newMappings") {
	val inDir = File("run/WhyMap/mappings-custom")
	val outBlockDir = File("src/main/resources/blockmappings")
	val outBiomeDir = File("src/main/resources/biomemappings")
//	inputs.dir(inDir.path)
//	outputs.dir(outBiomeDir.path)
//	outputs.dir(outBlockDir.path)
	doLast {
		inDir.resolve("current-biome").takeIf { it.exists() }?.let {
			val biomeMap = inDir.resolve("${it.readText().trim()}.biomemap")
			val fileNumber = outBiomeDir.listFiles()?.mapNotNull { it.nameWithoutExtension.toIntOrNull() }?.maxOrNull()?.plus(1) ?: 0
			biomeMap.copyTo(outBiomeDir.resolve("$fileNumber.biomemap"))
			inDir.resolve("current-biome").delete()
		}
		inDir.resolve("current-block").takeIf { it.exists() }?.let {
			val blockMap = inDir.resolve("${it.readText().trim()}.blockmap")
			val fileNumber = outBlockDir.listFiles()?.mapNotNull { it.nameWithoutExtension.toIntOrNull() }?.maxOrNull()?.plus(1) ?: 0
			blockMap.copyTo(outBlockDir.resolve("$fileNumber.blockmap"))
			inDir.resolve("current-block").delete()
		}
	}
}

val fillChangelogLinks = task("fillChangelogLinks") {
	val projectUrl = "https://github.com/wefhy/WhyMap"
	val changelog = File("CHANGELOG.md")
	val versionRegex = Regex("## ?\\[(?<version>.+)].*")
	doLast {
		val readLines = changelog.readLines()
		val versions = readLines.mapNotNull { versionRegex.matchEntire(it)?.groups?.get("version")?.value }
		println(versions)
		val versionLinks = versions.map { "[$it]: $projectUrl/releases/tag/$it" }
		val diffLinks = versions.mapIndexed { index, version ->
			if (index == 0) return@mapIndexed ""
			val from = versions[index - 1]
			val to = version
			"[$from]: $projectUrl/compare/$from..$to"
		}

		//Delete old links
		val oldLinksRegex = Regex("\\[.+]: $projectUrl/(releases/tag|compare)/.+")
		val output = readLines.filter { !oldLinksRegex.matches(it) }.dropLastWhile { it.isBlank() } + "\n" + diffLinks + versionLinks.last()

		changelog.writeText(output.joinToString("\n"))
	}
}

val createDiscordMessage = task("createDiscordMessage") {
	val githubUrl = "https://github.com/wefhy/WhyMap/releases/tag/"
	val modrinthUrl = "https://modrinth.com/mod/whymap/version/"
	val curseforgeUrl = "https://www.curseforge.com/minecraft/mc-mods/whymap/files/"
	val changelog = File("CHANGELOG.md")
	val versionRegex = Regex("## ?\\[(?<version>.+)].*")
	doLast {
		val readLines = changelog.readLines()
		val versions = readLines.mapNotNull { versionRegex.matchEntire(it)?.groups?.get("version")?.value }
		val latestVersion = versions.first()
		val latestChangelog = readLines.dropWhile { !it.startsWith("## [$latestVersion]") }.drop(1).takeWhile { !it.startsWith("## [") }.joinToString("\n")
		val message = """
			New version: $latestVersion
			${latestChangelog.trimIndent()}
			:github: $githubUrl$latestVersion
			:modrinth: $modrinthUrl$latestVersion
			:curseforge: $curseforgeUrl
			""".trimIndent()
		File("discordMessage.txt").writeText(message)
		println(message)
	}
}

fun ByteArray.toHex(): String {
	val HEX_ARRAY = "0123456789ABCDEF".toCharArray()
	val hexChars = CharArray(size * 2)
	for (j in indices) {
		val v = get(j).toInt() and 0xFF
		hexChars[j * 2] = HEX_ARRAY[v ushr 4]
		hexChars[j * 2 + 1] = HEX_ARRAY[v and 0x0F]
	}
	return String(hexChars)
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
		dependsOn(copyThreeJsFolder)
	}
	"build" {
		dependsOn(copyDistFolder)
		dependsOn(copyThreeJsFolder)
		dependsOn(createDiscordMessage)
		dependsOn(newMappings)
	}
	"processResources" {
		dependsOn(copyDistFolder)
		dependsOn(copyThreeJsFolder)
		dependsOn(blockMappingsList)
		dependsOn(biomeMappingsList)
	}
	"copyDistFolder" {
		dependsOn(deleteOldWeb)
		dependsOn(yarnBuild)
	}
	"copyThreeJsFolder" {
		dependsOn(deleteOldThree)
		dependsOn(threeBuild)
	}
	"yarnBuild" {
		dependsOn(yarnInstall)
	}
	"threeBuild" {
		dependsOn(npmInstall)
	}
	"createDiscordMessage" {
		dependsOn(fillChangelogLinks)
	}
	"newMappings" {
		dependsOn(blockMappingsList)
		dependsOn(biomeMappingsList)
	}
	remapJar {
		dependsOn("shadowJar")
		inputFile.set(named<ShadowJar>("shadowJar").get().archiveFile)
	}
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "17"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "17"
}