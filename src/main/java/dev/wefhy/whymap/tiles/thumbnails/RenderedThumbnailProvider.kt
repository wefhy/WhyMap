// Copyright (c) 2022 wefhy

package dev.wefhy.whymap.tiles.thumbnails

import java.awt.image.BufferedImage

interface RenderedThumbnailProvider {
    var wasUpdated: Boolean
    suspend fun getThumbnail(): BufferedImage?
}