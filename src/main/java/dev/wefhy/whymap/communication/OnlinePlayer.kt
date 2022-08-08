// Copyright (c) 2022 wefhy

package dev.wefhy.whymap.communication

import dev.wefhy.whymap.utils.CoordinateConversion
import kotlinx.serialization.Serializable

@Serializable
data class OnlinePlayer(
    val name: String,
    val position: PlayerPosition,
    val angle: Double
) {
    @Serializable
    class PlayerPosition(
        val x: Double,
        val y: Double,
        val z: Double
    ) {
        val lat: Double
        val lng: Double

        init {
            val t = CoordinateConversion.coord2deg(x, z)
            lat = t.first
            lng = t.second
        }
    }


}