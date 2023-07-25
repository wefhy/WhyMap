// Copyright (c) 2022 wefhy

package dev.wefhy.whymap.tiles.thumbnails

import dev.wefhy.whymap.CurrentWorldProvider
import dev.wefhy.whymap.WhyWorld
import dev.wefhy.whymap.utils.LocalTileThumbnail
import java.io.ByteArrayOutputStream

context(CurrentWorldProvider<WhyWorld>)
class ThumbnailsManager {
    private val thumbnailMergers = mutableMapOf<LocalTileThumbnail, ThumbnailMerger>()
    //todo use optional as well, if none of underlying regions are available just don't try to render them at all!

    suspend fun getThumbnail(position: LocalTileThumbnail): ByteArrayOutputStream? =
        thumbnailMergers.getOrPut(position) {
            ThumbnailMerger(position) //TODO use optionals?
        }.render()
}