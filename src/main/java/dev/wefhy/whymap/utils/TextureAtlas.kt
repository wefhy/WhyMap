// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.utils

import dev.wefhy.whymap.CurrentWorldProvider
import dev.wefhy.whymap.WhyWorld
import dev.wefhy.whymap.communication.quickaccess.BlockQuickAccess
import dev.wefhy.whymap.tiles.details.ExperimentalTextureProvider
import dev.wefhy.whymap.tiles.mesh.MeshGenerator
import dev.wefhy.whymap.tiles.mesh.Uv
import dev.wefhy.whymap.tiles.mesh.UvCoordinate
import net.minecraft.block.Block
import java.awt.image.BufferedImage
import java.awt.image.RescaleOp
import java.util.*

/**
 * Trim sheet implementation
 */

object TextureAtlas {

    val _textureAtlas by lazy {
        with(context) {
            createTextureAtlas()
        }
    }

    lateinit var context: CurrentWorldProvider<WhyWorld>

    context(CurrentWorldProvider<WhyWorld>)
    val textureAtlas: BufferedImage
        get() {
            context = this@CurrentWorldProvider
            return _textureAtlas
        }

//    private val blocks by lazy { Block.STATE_IDS.map { it.block }.toSet().sortedBy { it.hashCode() } }
    private val blocks by lazy {
        IdentityHashMap(
            Block.STATE_IDS.map { it.block }.toSet().let {
                var i = 0
                it.associateWith { i++ }
            }
        )
    }
//    private val atlasSize by lazy { blocks.size }
    private val atlasSize by lazy {
//        1 shl (32 - Integer.numberOfLeadingZeros(blocks.size - 1))
        var size = 1
        while (size < blocks.size) {
            size *= 2
        }
        size
    }

    fun getBlockUV(block: Block): Uv {
        val i = getBlockIndex(block)
        val aS = (1.0 / atlasSize)
        return Uv(
            UvCoordinate((i + 0) * aS, 0.0),
            UvCoordinate((i + 0) * aS, 1.0),
            UvCoordinate((i + 1) * aS, 1.0),
            UvCoordinate((i + 1) * aS, 0.0),
        )
    }

    fun getOverlaySideUv(block: Block, hDiff: Int): Uv {
        val repeats = hDiff
        val i = getBlockIndex(block)
        val aS = (1.0 / atlasSize)
        return Uv(
            UvCoordinate((i + 0) * aS, 0.0),
            UvCoordinate((i + 0) * aS, repeats.toDouble()),
            UvCoordinate((i + 1) * aS, repeats.toDouble()),
            UvCoordinate((i + 1) * aS, 0.0),
        )
    }

    fun getBlockSideUv(block: Block, height: Short): Uv {
        val repeats = height - MeshGenerator.bottomFaceHeightI
        return getOverlaySideUv(block, repeats)
    }

    private fun getBlockIndex(block: Block) = blocks[block]!!
//    private fun getBlockIndex(block: Block) = blocks.indexOf(block)
//    private fun getBlockIndex(block: Block) = blocks.binarySearch { it.hashCode().compareTo(block.hashCode()) }
//    private inline fun getBlockIndex(block: Block) = blocks.binarySearch { it.translationKey.compareTo(block.translationKey) }
//    private inline fun getIndex(block: Block) = blocks.binarySearch(block, String::compareTo)

    context(CurrentWorldProvider<WhyWorld>)
    private fun createTextureAtlas(): BufferedImage {

        val atlasResolution = atlasSize * 16

        val bufferedImage = BufferedImage(atlasResolution, 16, BufferedImage.TYPE_INT_RGB)
        val g2d = bufferedImage.createGraphics()
        currentWorld.biomeManager
//        val foliageColor = currentWorld.biomeManager.decodeBiomeFoliage(0).floatArray.dropLast(1).toFloatArray()
        val foliageColor = floatArrayOf(0.33f, 1f, 0.07f).map { it * 0.7f }.toFloatArray()
        val waterColor = floatArrayOf(0f, 0.25f, 1f)
//        val normalShade = MapArea.Normal(0, 0).shade
        val floatArray = floatArrayOf(1f, 1f, 1f)

        blocks.forEach { (block, i) ->
//        blocks.forEachIndexed { i, block ->
            val pos = i * 16
            val source = ExperimentalTextureProvider.getBitmap(block)
            val blockColorFilter = if (BlockQuickAccess.foliageBlocksSet.contains(block.defaultState)) {
                foliageColor
            } else if (BlockQuickAccess.waterBlocks.contains(block.defaultState)) {
                waterColor
            } else {
                floatArray
            }
            if (source != null) {
                try {
                    g2d.drawImage(
                        source,
                        RescaleOp(blockColorFilter, FloatArray(3), null),
                        pos,
                        0
                    )
                } catch (e: IllegalArgumentException) {
                    println("Indexed image! ${block.translationKey}")
                }
            } else {
                g2d.color = java.awt.Color(block.defaultState.getMapColor(null, null).color)
                g2d.fillRect(pos, 0, 16, 16)
            }
        }
        return bufferedImage
    }
}