// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.utils

import kotlin.math.max
import kotlin.math.min

class RectArea<T : TileZoom>(
    _start: LocalTile<T>,
    _end: LocalTile<T>,
) {
    val zoom = _start.zoom
    val start = LocalTile(
        min(_start.x, _end.x),
        min(_start.z, _end.z),
        zoom
    )
    val end = LocalTile(
        max(_start.x, _end.x),
        max(_start.z, _end.z),
        zoom
    )
    val sizeX = end.x - start.x + 1
    val sizeZ = end.z - start.z + 1
    val size = sizeX * sizeZ

    fun <X : TileZoom> parent(zoom: X): RectArea<X> {
        return RectArea(
            start.parent(zoom),
            end.parent(zoom)
        )
    }

    fun blockArea(): RectArea<TileZoom.BlockZoom> {
        return RectArea(
            start.getStart(),
            end.getEnd()
        )
    }

    fun list(): List<LocalTile<T>> {
        require(size < 1_000_000) { "Too many tiles in rect area" }
        val list = ArrayList<LocalTile<T>>(size)
        for (x in start.x..end.x) {
            for (z in start.z..end.z) {
                list.add(LocalTile(x, z, start.zoom))
            }
        }
        return list
    }

    fun array(): Array<LocalTile<T>> {
        require(size < 1_000_000) { "Too many tiles in rect area" }
        var x = start.x
        var z = start.z

        return Array(size) {
            LocalTile(x++, z, start.zoom).also {
                if (x > end.x) {
                    x = start.x
                    z++
                }
            }
        }
    }

    operator fun contains(tile: LocalTile<T>): Boolean {
        return tile.x in start.x..end.x && tile.z in start.z..end.z
    }

    operator fun contains(area: RectArea<T>): Boolean {
        return area.start in this && area.end in this
    }

    infix fun intersects(area: RectArea<T>): Boolean {
        return area.start.x <= end.x &&
                area.end.x >= start.x &&
                area.start.z <= end.z &&
                area.end.z >= start.z
    }

    infix fun<R: TileZoom> intersect(tile: LocalTile<R>): RectArea<T>? {
        require(tile.zoom.zoom <= zoom.zoom) { "Tile zoom must be less or equal to area zoom" }
        val tileArea = tile.areaAt(zoom)
        return intersect(tileArea)
    }

    infix fun intersect(area: RectArea<T>): RectArea<T>? {
        if (area.start.x > end.x || area.end.x < start.x) return null
        if (area.start.z > end.z || area.end.z < start.z) return null

        val startX = max(start.x, area.start.x)
        val startZ = max(start.z, area.start.z)
        val endX = min(end.x, area.end.x)
        val endZ = min(end.z, area.end.z)
        return RectArea(
            LocalTile(startX, startZ, start.zoom),
            LocalTile(endX, endZ, start.zoom)
        )
    }

    infix fun union(area: RectArea<T>): RectArea<T> {
        val startX = min(start.x, area.start.x)
        val startZ = min(start.z, area.start.z)
        val endX = max(end.x, area.end.x)
        val endZ = max(end.z, area.end.z)
        return RectArea(
            LocalTile(startX, startZ, start.zoom),
            LocalTile(endX, endZ, start.zoom)
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RectArea<*>) return false

        if (start != other.start) return false
        if (end != other.end) return false

        return true
    }

    override fun hashCode(): Int {
        var result = start.hashCode()
        result = 31 * result + end.hashCode()
        return result
    }

    override fun toString(): String {
        return "RectArea${start.zoom}({${start.x}, ${start.z}} to {${end.x}, ${end.z}})"
    }
}

fun<T: TileZoom, R: TileZoom> LocalTile<R>.areaAt(zoom: T): RectArea<T> {
    return RectArea(this.getStart(zoom), this.getEnd(zoom))
}

infix fun <T : TileZoom> LocalTile<T>.to(end: LocalTile<T>): RectArea<T> {
    return RectArea(this, end)
}

