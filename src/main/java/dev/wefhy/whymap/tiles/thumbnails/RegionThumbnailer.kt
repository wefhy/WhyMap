// Copyright (c) 2022 wefhy

package dev.wefhy.whymap.tiles.thumbnails

import dev.wefhy.whymap.CurrentWorldProvider
import dev.wefhy.whymap.WhyWorld
import dev.wefhy.whymap.utils.LocalTileRegion

context(CurrentWorldProvider<WhyWorld>)
class RegionThumbnailer {

    fun getRegion(position: LocalTileRegion): RenderedThumbnailProvider {

        //Weakly get region if already loaded and render it then maybe save it
        //Look for cached region
        //Read region from file
        //Strongly get region even if not already loaded, render it, save thumbnail and instantly unload it
        //when region that was updated is unloaded, it should write thumbnail file before being unloaded


//        val region = MapRegionManager.getLoadedRegionForRead(position)?.getAndSaveThumbnail()
//            ?: getThumbnailFromFile(position)
//            ?: MapRegionManager.getRegionForReadAndLoad(position)?.getAndSaveThumbnail()

        return currentWorld.mapRegionManager.getLoadedRegionForRead(position)
            ?: getFileThumbnail(position)
            ?: currentWorld.mapRegionManager.peekRegion(position)
            ?: EmptyThumbnailProvider


//        MinecraftClient.getInstance().world!!.getChunk(0,0, ChunkStatus.EMPTY, false)
    }

    private fun getFileThumbnail(position: LocalTileRegion): RenderedThumbnailProvider? {
        return if (currentWorld.mapRegionManager.getThumbnailFile(position).exists())
            LazyThumbnail(position)
        else null

    }


    fun reRenderRegion() {

    }

}