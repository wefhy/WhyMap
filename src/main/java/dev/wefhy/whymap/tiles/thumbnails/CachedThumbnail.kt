// Copyright (c) 2022 wefhy

package dev.wefhy.whymap.tiles.thumbnails

import java.awt.image.BufferedImage

class CachedThumbnail : RenderedThumbnailProvider {
    override suspend fun getThumbnail(): BufferedImage {
        TODO()
    }

    override var wasUpdated: Boolean = false

}