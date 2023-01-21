// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.utils

import dev.wefhy.whymap.tiles.details.ExperimentalTextureProvider
import dev.wefhy.whymap.tiles.mesh.Uv
import dev.wefhy.whymap.tiles.mesh.UvCoordinate
import net.minecraft.block.Block
import java.awt.image.BufferedImage

/**
 * Trim sheet implementation
 */
object TextureAtlas {


    val textureAtlas by lazy { createTextureAtlas() }

    private val blocks by lazy { Block.STATE_IDS.map { it.block }.toSet().sortedBy { it.translationKey } }
    private val atlasSize by lazy { blocks.size }

    fun getBlockUV(block: Block): Uv {
        val i = blocks.indexOf(block)
        val aS = (1.0 / atlasSize)
        return Uv(
            UvCoordinate((i + 0) * aS, 0.0),
            UvCoordinate((i + 0) * aS, 1.0),
            UvCoordinate((i + 1) * aS, 1.0),
            UvCoordinate((i + 1) * aS, 0.0),
        )
    }

    fun getBlockSideUv(block: Block, height: Short): Uv {
        val i = blocks.indexOf(block)
        val aS = (1.0 / atlasSize)
        return Uv(
            UvCoordinate((i + 0) * aS, 0.0),
            UvCoordinate((i + 0) * aS, height.toDouble()),
            UvCoordinate((i + 1) * aS, height.toDouble()),
            UvCoordinate((i + 1) * aS, 0.0),
        )
    }

    private fun createTextureAtlas(): BufferedImage {

        val atlasResolution = atlasSize * 16

        val bufferedImage = BufferedImage(atlasResolution, 16, BufferedImage.TYPE_INT_RGB)
        val g2d = bufferedImage.createGraphics()

        blocks.forEachIndexed { i, block ->
            val pos = i * 16
            val source = ExperimentalTextureProvider.getBitmap(block)
            if (source != null) {
                g2d.drawImage(source, pos, 0, null)
            } else {
                g2d.color = java.awt.Color(block.defaultState.material.color.color)
                g2d.fillRect(pos, 0, 16, 16)
            }
        }
        return bufferedImage
    }
}