// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.utils

import dev.wefhy.whymap.quickaccess.BlockQuickAccess
import dev.wefhy.whymap.tiles.details.ExperimentalTextureProvider
import net.minecraft.block.Block
import java.awt.image.BufferedImage
import kotlin.math.ceil
import kotlin.math.sqrt

object TextureAtlas {

    val textureAtlas by lazy { createTextureAtlas() }

    private fun createTextureAtlas(): BufferedImage {
        val atlasSize = ceil(sqrt(BlockQuickAccess.minecraftBlocks.size.toDouble())).toInt()
        val atlasResolution = atlasSize * 16

        val bufferedImage = BufferedImage(atlasResolution, atlasResolution, BufferedImage.TYPE_INT_RGB)
        val g2d = bufferedImage.createGraphics()

        Block.STATE_IDS.forEachIndexed { i, blockState ->
            val x = i / atlasSize
            val y = i % atlasSize
            val xPos = x * 16
            val yPos = y * 16
            val source = ExperimentalTextureProvider.getBitmap(blockState.block)
            if (source != null) {
                g2d.drawImage(source, xPos, yPos, null)
            } else {
                g2d.color = java.awt.Color(blockState.material.color.color)
                g2d.fillRect(xPos, yPos, 16, 16)
            }
        }
        return bufferedImage
    }
}