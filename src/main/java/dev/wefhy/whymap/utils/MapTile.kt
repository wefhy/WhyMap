// Copyright (c) 2022 wefhy

package dev.wefhy.whymap.utils

import dev.wefhy.whymap.config.WhyMapConfig
import dev.wefhy.whymap.utils.TileZoom.*
import net.minecraft.util.math.ChunkPos
import java.io.File

open class MapTile<Z>(val x: Int, val z: Int, val zoom: Z) where Z : TileZoom {

    fun toLocalTile(): LocalTile<Z> {
        return LocalTile(
            x - zoom.offset,
            z - zoom.offset,
            zoom
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MapTile<*>

        if (x != other.x) return false
        if (z != other.z) return false
        if (zoom != other.zoom) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + z
        result = 31 * result + zoom.hashCode()
        return result
    }

    override fun toString() = "OnlineTile$zoom{x: $x, z: $z}"


//    fun <X> changeZoom(): MapTile<X> where X : TileZoom {
//
//    }
//
//    fun <X> toLocalTile(): LocalTile<X> where X : TileZoom {
//
//    }

}

typealias LocalTileBlock = LocalTile<BlockZoom>
fun LocalTileBlock(x: Int, z: Int) = LocalTile(x, z, BlockZoom)
typealias LocalTileChunk = LocalTile<ChunkZoom>
fun LocalTileChunk(x: Int, z: Int) = LocalTile(x, z, ChunkZoom)
typealias LocalTileRegion = LocalTile<RegionZoom>
fun LocalTileRegion(x: Int, z: Int) = LocalTile(x, z, RegionZoom)
typealias LocalTileThumbnail = LocalTile<ThumbnailZoom>
fun LocalTileThumbnail(x: Int, z: Int) = LocalTile(x, z, ThumbnailZoom)

open class LocalTile<Z : TileZoom>(val x: Int, val z: Int, val zoom: Z) {

    fun toMapTile(): MapTile<Z> {
        return MapTile(
            x + zoom.offset,
            z + zoom.offset,
            zoom
        )
    }

    fun <X : TileZoom> toList(newZoom: X): List<List<LocalTile<X>>> {
        val diff = newZoom.zoom - zoom.zoom
        assert(diff > 0)
        val nTiles = 1 shl diff
        val startX = x shl diff
        val startZ = z shl diff
        return List(nTiles) { _z ->
            List(nTiles) { _x ->
                LocalTile(
                    startX + _x,
                    startZ + _z,
                    newZoom
                )
            }
        }
    }

    fun<T: TileZoom> getStart(newZoom: T): LocalTile<T> {
        val diff = newZoom.zoom - zoom.zoom
        return LocalTile(
            x shl diff,
            z shl diff,
            newZoom
        )
    }

    fun <T: TileZoom> getEnd(newZoom: T): LocalTile<T> {
        val diff = newZoom.zoom - zoom.zoom
        return LocalTile(
            ((x+1) shl diff) - 1,
            ((z+1) shl diff) - 1,
            newZoom
        )
    }

    fun getStart(): LocalTileBlock = getStart(BlockZoom)

    fun getEnd(): LocalTileBlock = getEnd(BlockZoom)

    fun getCenter(): LocalTileBlock {
        val diff = BlockZoom.zoom - zoom.zoom
        val half = 1 shl diff shr 1 // can't be 1 shl (diff-1) because 1 shl -1
        return Block(
            (x shl diff) + half,
            (z shl diff) + half
        )
    }

    fun <X : TileZoom> parent(newZoom: X): LocalTile<X> {
        val diff = zoom.zoom - newZoom.zoom
        require(diff >= 0)
        return LocalTile(
            x shr diff,
            z shr diff,
            newZoom
        )
    }

    operator fun plus(other: LocalTile<Z>): LocalTile<Z> {
        return LocalTile(
            x + other.x,
            z + other.z,
            zoom
        )
    }

    operator fun minus(other: LocalTile<Z>): LocalTile<Z> {
        return LocalTile(
            x - other.x,
            z - other.z,
            zoom
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LocalTile<*>

        if (x != other.x) return false
        if (z != other.z) return false
        if (zoom != other.zoom) return false

        return true
    }

    infix fun<X: TileZoom> relativeTo(other: LocalTile<X>): LocalTile<Z> {
        val otherStart = other.getStart(zoom)
        return LocalTile(
            x - otherStart.x,
            z - otherStart.z,
            zoom
        )
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + z
        result = 31 * result + zoom.hashCode()
        return result
    }

    companion object {
        fun Thumbnail(x: Int, z: Int) = LocalTile(x, z, ThumbnailZoom)
        fun Region(x: Int, z: Int) = LocalTile(x, z, RegionZoom)
        fun Chunk(x: Int, z: Int) = LocalTile(x, z, ChunkZoom)
        fun Block(x: Int, z: Int) = LocalTile(x, z, BlockZoom)
    }

    override fun toString() = "LocalTile$zoom{x: $x, z: $z}"
}

val LocalTileChunk.chunkPos
    get() = ChunkPos(x, z)

sealed class TileZoom(val zoom: Int) {
    override fun toString() = "Zoom$zoom"

    object BlockZoom : TileZoom(WhyMapConfig.blockZoom)
    object ChunkZoom : TileZoom(WhyMapConfig.chunkZoom)
    object RegionZoom : TileZoom(WhyMapConfig.regionZoom)
    object ThumbnailZoom : TileZoom(WhyMapConfig.thumbnailZoom)

    val offset = 1 shl (zoom - 1)
}

fun File.resolve(tile: MapTile<out TileZoom>) = this
    .resolve(tile.zoom.toString())
    .resolve(tile.x.toString())
    .resolve(tile.z.toString())
