// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.config

import com.akuleshov7.ktoml.Toml
import com.akuleshov7.ktoml.TomlInputConfig
import dev.wefhy.whymap.config.WhyMapConfig.configFile
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

object FileConfigManager {
    private val toml = Toml(
        inputConfig = TomlInputConfig(
            ignoreUnknownNames = true,
        )
    )

    // TODO can this generate type hints for enum values?
    // Can this only edit the fields and leave the rest of the file untouched? Like comments?
    // Can this hint default values?
    // Add information about exact mod version that generated this config

    var config = if (configFile.exists()) {
        try {
            toml.decodeFromString(configFile.readText())
        } catch (e: Exception) {
            println("Failed to load config: ${e.message}")
            try {
                val bak = configFile.resolveSibling("${configFile.nameWithoutExtension}.${configFile.extension}.bak")
                bak.delete()
                configFile.renameTo(bak)
            } catch (e: Exception) {
                println("Failed to backup old config: ${e.message}")
            }
            ConfigData()
        }
    } else {
        ConfigData()
    }

    fun save() = try {
        configFile.writeText("${toml.encodeToString(config)}\n\n${UserSettings.help()}", Charsets.UTF_8)
        println("Saved config to ${configFile.absolutePath}")
    } catch (e: Exception) {
        println("Failed to save config: ${e.message}")
    }


    @Serializable
    data class ConfigData(
        val configFileVersion: Int = 1,
        val userSettings: UserSettings = UserSettings(),
    )
}