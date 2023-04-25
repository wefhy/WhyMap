// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.config

import kotlinx.serialization.Serializable

@Serializable
data class UserSettings(
    //TODO use data object in kotlin 1.9
    var exposeHttpApi: ExposeHttpApi = ExposeHttpApi.LOCALHOST_ONLY,
//    var httpApiPort: Int = defaultPort,
//    var updateInterval: Int = 20, //ticks
//    var ignoredOverlays: List<String> = listOf(
//        "string",
//        "tripwire",
//        "vine",
//    ),
    var mapScale: Double = 1.0,
    var displayHud: Boolean = true,
    var minimapPosition: MinimapPosition = MinimapPosition.TOP_LEFT,
    var minimapMode: MapMode = MapMode.NORTH_LOCKED,
    val forceExperimentalMinmap: Boolean = false,
//    var minimapSize: Int = 128,
//    var minimapScale : Int = 1,
//    var minimapOpacity: Int = 100,
//    var minimapDynamicScale: Boolean = false,
) {
    enum class MinimapPosition {
        TOP_LEFT,
        TOP_CENTER,
        TOP_RIGHT,
    }

    enum class ExposeHttpApi {
        DISABLED,
        LOCALHOST_ONLY,
        EVERYWHERE,
        DEBUG
    }

    enum class MapMode(val visible: Boolean) {
        DISABLED(false),
        NORTH_LOCKED(true),
        ROTATED(true);

        fun next() = values()[(ordinal + 1) % values().size]
    }

    companion object {
        fun help(): String = UserSettings::class.java.declaredFields
            .filter { it.type.isEnum }
            .map { it.name to it.type.enumConstants.joinToString(", ") }
            .joinToString("\n") { (enum, values) -> "# $enum: $values" }

    }
}