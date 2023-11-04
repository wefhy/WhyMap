// Copyright (c) 2022 wefhy

package dev.wefhy.whymap.waypoints

import dev.wefhy.whymap.CurrentWorldProvider
import dev.wefhy.whymap.WhyMapMod.Companion.LOGGER
import dev.wefhy.whymap.WhyWorld
import dev.wefhy.whymap.utils.mkDirsIfNecessary
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.minecraft.util.math.GlobalPos

context(CurrentWorldProvider<WhyWorld>)
class Waypoints {
    private val file = currentWorld.worldPath.resolve("waypoints.txt")
    private val waypoints: MutableList<LocalWaypoint> = try {
        if (file.exists())
            Json.decodeFromString(file.readText())
        else mutableListOf()
    } catch (e: Throwable) {
        e.printStackTrace()
        mutableListOf()
    }
    val onlineWaypoints: List<OnlineWaypoint>
        get() = waypoints.map { it.asOnlineWaypointWithOffset() }

    val onlineWaypointsWithoutDeaths: List<OnlineWaypoint>
        get() = waypoints.filter { it.isDeath == null || !it.isDeath }.map { it.asOnlineWaypointWithOffset() }

    fun getOnlineWaypoints(deaths: Boolean, beds: Boolean): List<OnlineWaypoint> {
        var wp: List<LocalWaypoint> = waypoints
        if (!deaths) wp = wp.filter { it.isDeath == null || !it.isDeath }
        if (!beds) wp = wp.filter { it.isBed == null || !it.isBed }
        return wp.map { it.asOnlineWaypointWithOffset() }
    }

    fun save() {
        file.mkDirsIfNecessary()
        file.writeText("[\n${waypoints.joinToString(",\n") { Json.encodeToString(it) }}\n]")
        LOGGER.debug("Saved waypoints: ${waypoints.joinToString { it.name }}")
    }

    fun import(xaeroWaypoints: String) {
        xaeroWaypoints.lines().asReversed().asSequence()
            .filterNot { it.startsWith("#") }
            .map { it.split(":") }
            .filter { it.size > 10 }
            .mapNotNull {
                try {
                    LocalWaypoint(
                        name = it[1],
                        location = CoordXYZ(it[3].toInt(), it[4].toIntOrNull() ?: 500, it[5].toInt()),
                        initials = it[2],
                        color = xaeroColors.getOrNull(it[6].toInt()) ?: "red"
                    )
                } catch (_: Throwable) {
                    null
                }
            }.forEach { newWaypoint ->
                val old = waypoints.find { it.location == newWaypoint.location }
                    ?: return@forEach run { waypoints += newWaypoint }
                old.name = newWaypoint.name
                if (newWaypoint.initials != null) {
                    old.initials = newWaypoint.initials
                }
                if (newWaypoint.color != null) {
                    old.color = newWaypoint.color
                }
            }
    }

    fun load() {
//        val xaeroExampleFile = File(".").resolve("XaeroWaypoints").resolve("example.txt")
//        import(xaeroExampleFile.readText())
    }

    fun add(waypoint: OnlineWaypoint) {
        waypoints += waypoint.asLocalWaypoint()
    }

    fun add(waypoint: LocalWaypoint) {
        waypoints += waypoint
    }

    fun remove(waypoint: OnlineWaypoint) {
        waypoints.removeIf { it.name == waypoint.name && it.location == waypoint.pos }
    }

    fun addDeathPoint(globalPos: GlobalPos) {
//        println("adding death pos hehe; before: ${waypoints.joinToString { it.name }}")
        //TODO check dimension!
        val pos = CoordXYZ(globalPos.pos.x, globalPos.pos.y, globalPos.pos.z)
        if (waypoints.any { it.isDeath == true && it.location == pos }) return
        waypoints += LocalWaypoint("Death", pos, isDeath = true, color = "black")
//        println("death pos added; after: ${waypoints.joinToString { it.name }}")
    }

    companion object {
        val xaeroColors = arrayOf(
            0x000000,
            0x000077,
            0x007700,
            0x770000,
            0x770077,
            0xff7700,
            0x999999,
            0x555555,
            0x3333cc,
            0x00ff00,
            0x00ffff,
            0xff0000,
            0xff7700,
            0xffff00,
            0xffffff
        ).map { "#%06x".format(it) }
    }
}