// Copyright (c) 2022 wefhy

package dev.wefhy.whymap.tiles.thumbnails

import dev.wefhy.whymap.CurrentWorldProvider
import dev.wefhy.whymap.WhyWorld
import dev.wefhy.whymap.utils.LocalTileRegion
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

context(CurrentWorldProvider<WhyWorld>)
class LazyThumbnail(position: LocalTileRegion) : RenderedThumbnailProvider {

    val file = currentWorld.getThumbnailFile(position)

    override suspend fun getThumbnail(): BufferedImage? {
        return try {
            ImageIO.read(file)
        } catch (e: Throwable) {
            e.printStackTrace()
            return null
        }
//        null
    }


    override var wasUpdated = false
        set(_) {}
}