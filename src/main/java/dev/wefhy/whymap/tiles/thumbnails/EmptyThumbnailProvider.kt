// Copyright (c) 2022 wefhy

package dev.wefhy.whymap.tiles.thumbnails

import java.awt.image.BufferedImage

object EmptyThumbnailProvider : RenderedThumbnailProvider {
    override var wasUpdated = false
        set(_) {}
    override suspend fun getThumbnail(): BufferedImage? = null
}