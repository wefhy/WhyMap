// Copyright (c) 2022 wefhy

package dev.wefhy.whymap.tiles.details

import dev.wefhy.whymap.CurrentWorldProvider
import dev.wefhy.whymap.WhyWorld
import dev.wefhy.whymap.quickaccess.BlockQuickAccess.foliageBlocksSet
import dev.wefhy.whymap.quickaccess.BlockQuickAccess.waterBlocks
import dev.wefhy.whymap.utils.Color
import dev.wefhy.whymap.utils.FloatColor
import dev.wefhy.whymap.utils.LocalTile
import kotlinx.coroutines.runBlocking
import net.minecraft.util.math.ChunkPos
import java.awt.AlphaComposite
import java.awt.image.BufferedImage
import java.awt.image.RescaleOp
import java.util.*
import kotlin.jvm.optionals.getOrNull

context(CurrentWorldProvider<WhyWorld>)
class ExperimentalTileGenerator {

    val renderedTiles = mutableMapOf<ChunkPos, Optional<BufferedImage>>()

    @OptIn(ExperimentalStdlibApi::class)
    fun getTile(position: ChunkPos): BufferedImage? = renderedTiles.getOrPut(position) {
        Optional.ofNullable(renderTile(position))
    }.getOrNull()

    val FloatColor.floatArray
        get() = floatArrayOf(r, g, b, 1f)


    private fun renderTile(position: ChunkPos): BufferedImage? {
        //TODO switch context
        return runBlocking {
            try {
                currentWorld.mapRegionManager.getRegionForTilesRendering(
                    LocalTile.Region(
                        position.regionX,
                        position.regionZ
                    )
                ) {
                    val chunk = getChunk(position) ?: return@runBlocking null
                    val chunkOverlay = getChunkOverlay(position) ?: return@runBlocking null
                    val biomeFoliage = getChunkBiomeFoliageAndWater(position) ?: return@runBlocking null
                    val depthmap = getChunkDepthmap(position) ?: return@runBlocking null
                    val normalmap = getChunkNormals(position) ?: return@runBlocking null
                    val bufferedImage = BufferedImage(16 * 16, 16 * 16, BufferedImage.TYPE_INT_RGB)
                    val g2d = bufferedImage.createGraphics()
                    val originalComposite = g2d.composite
                    val alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f)

                    for (y in 0 until 16) {
                        for (x in 0 until 16) {
                            val block = chunk[y][x]
                            val (foliageColor, oceanColor) = biomeFoliage[y][x]
                            val blockOverlay = chunkOverlay[y][x]
                            val depth = depthmap[y][x]
                            val normalShade = normalmap[y][x].shade

                            val blockColorFilter =
                                if (foliageBlocksSet.contains(block)) {
                                    (foliageColor * normalShade).floatArray
                                } else {
                                    normalShade.floatArray
                                }
                            val source = ExperimentalTextureProvider.getBitmap(block.block)
                            if (source != null) {
                                g2d.drawImage( //TODO java.lang.IllegalArgumentException: Rescaling cannot be performed on an indexed image
                                    source,
                                    RescaleOp(blockColorFilter, FloatArray(4), null),
                                    x * 16,
                                    y * 16
                                )
                            } else {
                                g2d.color = java.awt.Color(block.material.color.color)
                                g2d.fillRect(x * 16, y * 16, 16, 16)
                            }
                            if (depth == 0.toByte()) continue
                            val sourceOverlay = ExperimentalTextureProvider.getBitmap(blockOverlay.block)
                            val tmp1 = (1 - depth * 0.02f).coerceAtLeast(0f)
                            val alpha = (3 - tmp1 * tmp1) / 3
                            val darken = -depth / 64f
                            val blockOverlayColorFilter = if (foliageBlocksSet.contains(blockOverlay)) {
                                (foliageColor * normalShade).floatArray
                            } else {
                                normalShade.floatArray
                            }
                            val newRop = RescaleOp(blockOverlayColorFilter, floatArrayOf(darken, darken, darken, 0f), null)
                            if (sourceOverlay != null) {
                                g2d.drawImage(sourceOverlay, newRop, x * 16, y * 16)
                            } else {
                                val c = if (waterBlocks.contains(blockOverlay)) oceanColor else Color(blockOverlay.material.color.color)
                                g2d.color = java.awt.Color(
                                    (c + (-depth * 4)).toInt() or ((alpha * 255).toInt() shl 24),
                                    true
                                )
                                g2d.fillRect(x * 16, y * 16, 16, 16)
                            }
                        }
                    }
                    bufferedImage
                }

            } catch (e: IndexOutOfBoundsException) {
                println("Failed to render chunk (${position.x}, ${position.z})")
                null
            }
        }
    }
}