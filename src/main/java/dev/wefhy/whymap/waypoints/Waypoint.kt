// Copyright (c) 2022 wefhy

package dev.wefhy.whymap.waypoints

import dev.wefhy.whymap.utils.CoordinateConversion
import dev.wefhy.whymap.utils.LocalTileBlock
import kotlinx.serialization.Serializable
import net.minecraft.util.math.Vec3d

@Serializable
class LocalWaypoint(var name: String, val location: CoordXYZ, var color: String? = null, var initials: String? = null, val isDeath: Boolean? = null, val isBed: Boolean? = null) {
    fun asOnlineWaypoint() = OnlineWaypoint(name, location.toLatLng(), location, color)
    fun asOnlineWaypointWithOffset() = OnlineWaypoint(name, location.toLatLngWithHalfBlockOffset(), location, color)
}

@Serializable
data class OnlineWaypoint(val name: String, var loc: LatLng? = null, val pos: CoordXYZ, val color: String? = null) {
    fun asLocalWaypoint() = LocalWaypoint(name, pos, color) //TODO calculate Y value!
}

@Serializable
class LatLng(val lat: Double, val lng: Double)

@Serializable
data class CoordXYZ(val x: Int, val y: Int, val z: Int) {
    fun toLatLng(): LatLng {
        val deg = CoordinateConversion.coord2deg(x.toDouble(), z.toDouble())
        return LatLng(deg.first, deg.second)
    }

    fun toVec3d(): Vec3d {
        return Vec3d(x.toDouble(), y.toDouble(), z.toDouble())
    }

    fun toLocalBlock(): LocalTileBlock {
        return LocalTileBlock(x, z)
    }

    fun toLatLngWithHalfBlockOffset(): LatLng {
        val deg = CoordinateConversion.coord2deg(x.toDouble() + 0.5, z.toDouble() + 0.5)
        return LatLng(deg.first, deg.second)
    }

    override fun toString(): String {
        return "(x=$x, y=$y, z=$z)"
    }

    companion object {
        val ZERO = CoordXYZ(0, 0, 0)
        fun Vec3d.toCoordXYZ(): CoordXYZ {
            return CoordXYZ(x.toInt(), y.toInt(), z.toInt())
        }
    }
}