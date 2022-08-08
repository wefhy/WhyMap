// Copyright (c) 2022 wefhy

package dev.wefhy.whymap.tiles.thumbnails

import java.awt.image.BufferedImage

object EmptyThumbnailProvider : RenderedThumbnailProvider {
    override val wasUpdated = false
    override suspend fun getThumbnail(): BufferedImage? = null
}