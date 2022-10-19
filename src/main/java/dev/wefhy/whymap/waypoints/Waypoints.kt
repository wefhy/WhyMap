// Copyright (c) 2022 wefhy

package dev.wefhy.whymap.waypoints

import dev.wefhy.whymap.CurrentWorldProvider
import dev.wefhy.whymap.WhyMapMod.Companion.LOGGER
import dev.wefhy.whymap.WhyWorld
import dev.wefhy.whymap.utils.mkDirsIfNecessary
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

context(CurrentWorldProvider<WhyWorld>)
class Waypoints {
    private val file = currentWorld.worldPath.resolve("waypoints.txt")
    private val waypoints: MutableList<LocalWaypoint> = try {
        Json.decodeFromString(file.readText())
    } catch (e: Throwable) {
        e.printStackTrace()
        mutableListOf()
    }
    val onlineWaypoints: List<OnlineWaypoint>
        get() = waypoints.map { it.asOnlineWaypointWithOffset() }

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