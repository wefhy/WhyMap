// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.whygraphics

import dev.wefhy.whymap.config.WhyMapConfig
import dev.wefhy.whymap.config.WhyMapConfig.chunksPerRegionLog
import dev.wefhy.whymap.utils.ExpensiveCall
import dev.wefhy.whymap.utils.ImageWriter.encodeJPEG
import dev.wefhy.whymap.utils.ImageWriter.encodePNG
import net.minecraft.client.texture.NativeImage
import java.awt.image.BufferedImage
import java.io.OutputStream

class WhyTiledImage(
    private val xTiles: Int,
    private val yTiles: Int,
    val data: Array<Array<WhyTile?>> = Array(yTiles) { Array(xTiles) { null } }
) : WhyImage(xTiles * WhyTile.chunkSize, yTiles * WhyTile.chunkSize) {

    constructor(xTiles: Int, yTiles: Int, builder: (y: Int, x: Int) -> WhyTile) : this(
        xTiles,
        yTiles,
        Array(yTiles) { y -> Array(xTiles) { x -> builder(y, x) } }
    )

    @ExpensiveCall
    override fun get(y: Int, x: Int): WhyColor? {
        val yTile = y shr WhyTile.lineShl
        val yPixel = y and WhyTile.lineMask
        val xTile = x shr WhyTile.lineShl
        val xPixel = x and WhyTile.lineMask
        return data[yTile][xTile]?.get(yPixel, xPixel)
    }

    fun toBufferedImage(): BufferedImage {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
//        val g2d = image.createGraphics()
        val raster = image.raster
        for (y in 0 until yTiles) {
            val line = data[y]
            for (x in 0 until xTiles) {
                val tile = line[x] ?: continue
                tile.writeInto(raster, x shl WhyTile.lineShl, y shl WhyTile.lineShl)
//                raster.setPixels(x shl WhyTile.lineShl, y shl WhyTile.lineShl, WhyTile.chunkSize, WhyTile.chunkSize, tile.data)
//                raster.setSample()
//                raster.setPixel()
//                raster.setSamples()
            }
        }
        return image
    }

    fun toNativeImage(): NativeImage {
        val image = NativeImage(width, height, false)
        for (y in 0 until yTiles) {
            val line = data[y]
            for (x in 0 until xTiles) {
                val tile = line[x] ?: continue
                tile.writeInto(image, x shl WhyTile.lineShl, y shl WhyTile.lineShl)
            }
        }
        return image
    }

//    fun toPng(os: OutputStream) {
//        os.encodePNG(toBufferedImage())
//    }
//
//    fun toJpeg(os: OutputStream) {
//        os.encodeJPEG(toBufferedImage())
//    }

    context(OutputStream)
    fun toPng() {
        encodePNG(toBufferedImage())
    }

    context(OutputStream)
    fun toJpeg() {
        encodeJPEG(toBufferedImage())
    }

    companion object {

        fun BuildForRegion(builder: (x: Int, y: Int) -> WhyColor): WhyTiledImage {
            return WhyTiledImage(WhyMapConfig.storageTileChunks, WhyMapConfig.storageTileChunks) { y, x ->
                WhyTile { yPixel, xPixel ->
                    builder(x shl chunksPerRegionLog + xPixel, y shl chunksPerRegionLog + yPixel)
                }
            }
        }

    }
}