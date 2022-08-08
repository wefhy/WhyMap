// Copyright (c) 2022 wefhy

package dev.wefhy.whymap.tiles.thumbnails

import java.awt.image.BufferedImage

interface RenderedThumbnailProvider {
    val wasUpdated: Boolean
    suspend fun getThumbnail(): BufferedImage?
}