// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.config

import dev.wefhy.whymap.WhyMapClient
import dev.wefhy.whymap.config.WhyMapConfig.defaultPort
import kotlinx.serialization.Serializable

@Serializable
data class UserSettings( //TODO use data object in kotlin 1.9
    var exposeHttpApi: ExposeHttpApi = ExposeHttpApi.LOCALHOST_ONLY,
    var httpApiPort: Int = defaultPort,
//    var updateInterval: Int = 20, //ticks
//    var ignoredOverlays: List<String> = listOf(
//        "string",
//        "tripwire",
//        "vine",
//    ),
    var minimapPosition: MinimapPosition = MinimapPosition.TOP_LEFT,
    var minimapMode: WhyMapClient.MapMode = WhyMapClient.MapMode.Normal,
//    var minimapSize: Int = 128,
//    var minimapScale : Int = 1,
//    var minimapOpacity: Int = 100,
//    var minimapDynamicScale: Boolean = false,
) {
    enum class MinimapPosition {
        TOP_LEFT,
        TOP_RIGHT
    }

    enum class ExposeHttpApi {
        DISABLED,
        LOCALHOST_ONLY,
        EVERYWHERE
    }

    companion object {
        fun help(): String {
            val fieldTypes = UserSettings::class.java.declaredFields.map { it.name to it.type }
            val enumTypes = fieldTypes.filter { it.second.isEnum }
            val enumValues = enumTypes.map { it.first to it.second.enumConstants.joinToString(", ") }
            return enumValues.joinToString("\n") { (enum, values) -> "$enum: $values" }.prependIndent("# ")
        }
    }
}