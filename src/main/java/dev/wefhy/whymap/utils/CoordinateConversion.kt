// Copyright (c) 2022 wefhy

package dev.wefhy.whymap.utils

import dev.wefhy.whymap.config.WhyMapConfig
import kotlin.math.*

object CoordinateConversion {
    private const val _rad = PI / 180
    private const val _deg = 180 / PI

    val Double.rad
        get() = this * _rad

    val Double.deg
        get() = this * _deg

    fun deg2tile(lat: Double, lng: Double, zoom: Double): Pair<Int, Int> {
        val n = 2.0.pow(zoom) // TODO 1 shl zoom
        val xTile = ((lng + 180) * n * (1 / 360.0)).toInt()
        val yTile = ((1 - asinh(tan(lat.rad)) / PI) / 2 * n).toInt()
        return Pair(xTile, yTile)
    }

    fun tile2deg(xTile: Int, yTile: Int, zoom: Double): Pair<Double, Double> {
//        val n = 2.0.pow(zoom) // TODO 1 shl zoom
        val n_inv = 0.5.pow(zoom)
        val lng = xTile * n_inv * 360 - 180
        val lat = atan(sinh(PI * (1 - 2 * yTile * n_inv))).deg
        return Pair(lat, lng)
    }

    fun deg2normalized(lat: Double, lng: Double): Pair<Double, Double> {
        val xTile = ((lng + 180) * (1 / 360.0))
        val yTile = ((1 - asinh(tan(lat.rad)) / PI) / 2)
        return Pair(xTile, yTile)
    }

    fun normalized2deg(x: Double, y: Double): Pair<Double, Double> {
        val lng = x * 360 - 180
        val lat = atan(sinh(PI * (1 - 2 * y))).deg
        return Pair(lat, lng)
    }

    fun deg2coord(lat: Double, lng: Double): Pair<Int, Int> {
        val normalized = deg2normalized(lat, lng)
        val scale = 2.0.pow(WhyMapConfig.blockZoom)
        return Pair(
            ((normalized.first - 0.5) * scale).roundToInt(),
            ((normalized.second - 0.5) * scale).roundToInt()
        )
    }

    fun coord2deg(x: Double, y: Double): Pair<Double, Double> {
        val scale = 0.5.pow(WhyMapConfig.blockZoom)
        return normalized2deg(
            (x * scale + 0.5),
            (y * scale + 0.5)
        )
    }
}