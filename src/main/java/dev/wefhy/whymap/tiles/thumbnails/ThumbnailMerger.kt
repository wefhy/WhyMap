// Copyright (c) 2022 wefhy

package dev.wefhy.whymap.tiles.thumbnails

import dev.wefhy.whymap.CurrentWorldProvider
import dev.wefhy.whymap.WhyMapMod.Companion.LOGGER
import dev.wefhy.whymap.WhyWorld
import dev.wefhy.whymap.config.WhyMapConfig.regionThumbnailResolution
import dev.wefhy.whymap.config.WhyMapConfig.tileResolution
import dev.wefhy.whymap.utils.LocalTileThumbnail
import dev.wefhy.whymap.utils.TileZoom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

context(CurrentWorldProvider<WhyWorld>)
class ThumbnailMerger(val position: LocalTileThumbnail) {
    val children = position.toList(TileZoom.RegionZoom)

    lateinit var encoded: ByteArrayOutputStream

    suspend fun render(): ByteArrayOutputStream? {
        val thumbnailers = children.map { row -> row.map { currentWorld.regionThumbnailer.getRegion(it) } }
        if (thumbnailers.flatten().all { it is EmptyThumbnailProvider }) {
            return null
        }
        if (::encoded.isInitialized && thumbnailers.flatten().none { it.wasUpdated }) {
            LOGGER.debug("EXCELLENT! THUMBNAIL WAS REUSED (size: ${encoded.size()}!!!")
            return encoded
        }

        LOGGER.debug("Composing thumbnail out of: ${children.joinToString(" | ") { it.joinToString(", ") { "{x: ${it.x}, z: ${it.z}}" } }}")
        val bufferedImage = BufferedImage(tileResolution, tileResolution, BufferedImage.TYPE_INT_RGB)
        withContext(Dispatchers.Default) {
            val g2d = bufferedImage.createGraphics()
            for ((z, row) in children.withIndex()) {
                for ((x, regionPosition) in row.withIndex()) {
//                    print("attempting render $x, $z ($regionPosition)")
                    val thumbnailProvider = currentWorld.regionThumbnailer.getRegion(regionPosition)
                    thumbnailProvider.wasUpdated = false
//                    LOGGER.debug(" type: ${thumbnailProvider::class.simpleName}")
                    val tile = thumbnailProvider.getThumbnail() ?: continue
//                    LOGGER.debug("something to draw!")
                    g2d.drawImage(tile, x * regionThumbnailResolution, z * regionThumbnailResolution, null)
//                    LOGGER.debug("drawn!")
                }
            }
        }
        LOGGER.debug("finished composing!")

        encoded = ByteArrayOutputStream()
        withContext(Dispatchers.IO) {
            ImageIO.write(bufferedImage, "jpg", encoded)
        }
        LOGGER.debug("rendered!")
        return encoded
    }


}