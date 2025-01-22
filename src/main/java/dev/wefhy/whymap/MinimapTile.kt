// Copyright (c) 2025 wefhy

package dev.wefhy.whymap

import dev.wefhy.whymap.utils.LocalTile
import dev.wefhy.whymap.utils.LocalTileRegion
import net.minecraft.util.Identifier

class MinimapTile(
    textureId: Identifier,
    width: Int,
    height: Int,
): BufferedTexture(textureId, width, height) {
    var frontBufferLocation: LocalTileRegion = LocalTile.Region(0, 0)
        private set
    private var backBufferLocation: LocalTileRegion = LocalTile.Region(0, 0)

    fun setBackBufferLocation(location: LocalTileRegion) {
        backBufferLocation = location
    }

    override fun onSwapBuffers() {
        frontBufferLocation = backBufferLocation.also { backBufferLocation = frontBufferLocation }
    }
}