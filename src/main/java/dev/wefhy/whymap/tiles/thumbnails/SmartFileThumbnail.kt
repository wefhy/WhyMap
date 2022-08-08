// Copyright (c) 2022 wefhy

package dev.wefhy.whymap.tiles.thumbnails

import dev.wefhy.whymap.CurrentWorldProvider
import dev.wefhy.whymap.WhyWorld
import dev.wefhy.whymap.utils.LocalTileRegion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

context(CurrentWorldProvider<WhyWorld>)
class SmartFileThumbnail(position: LocalTileRegion) : RenderedThumbnailProvider {

    var file = currentWorld.mapRegionManager.getThumbnailFile(position)
    var fileExists = file.exists()


    override suspend fun getThumbnail(): BufferedImage? {
        return if (file.exists()) {
            fileExists = true
            withContext(Dispatchers.IO) {
                ImageIO.read(file)
            }
        } else {
            fileExists = false
            null
        }

    }

    override val wasUpdated = (fileExists != file.exists())
}