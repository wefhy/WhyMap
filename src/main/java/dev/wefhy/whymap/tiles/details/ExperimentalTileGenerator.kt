// Copyright (c) 2022 wefhy

package dev.wefhy.whymap.tiles.details

import dev.wefhy.whymap.CurrentWorldProvider
import dev.wefhy.whymap.WhyWorld
import dev.wefhy.whymap.communication.quickaccess.BlockQuickAccess.foliageBlocksSet
import dev.wefhy.whymap.communication.quickaccess.BlockQuickAccess.ignoreDepthTint
import dev.wefhy.whymap.communication.quickaccess.BlockQuickAccess.waterBlocks
import dev.wefhy.whymap.communication.quickaccess.BlockQuickAccess.waterLoggedBlocks
import dev.wefhy.whymap.tiles.details.ExperimentalTextureProvider.waterTexture
import dev.wefhy.whymap.tiles.region.MapArea
import dev.wefhy.whymap.utils.LocalTile
import dev.wefhy.whymap.utils.RectArea
import dev.wefhy.whymap.utils.TileZoom
import dev.wefhy.whymap.utils.chunkPos
import dev.wefhy.whymap.whygraphics.*
import kotlinx.coroutines.withContext
import net.minecraft.util.math.ChunkPos
import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.awt.image.RescaleOp
import java.util.*
import kotlin.jvm.optionals.getOrNull

context(CurrentWorldProvider<WhyWorld>)
class ExperimentalTileGenerator {

    val renderedTiles = mutableMapOf<ChunkPos, Optional<BufferedImage>>()
    //TODO this map is never freed?!

    @OptIn(ExperimentalStdlibApi::class)
    suspend fun getTile(position: ChunkPos): BufferedImage? = renderedTiles.getOrPut(position) {
        //TODO getOrPut basically makes this whole stuff synchronous
        Optional.ofNullable(renderTile(position))
    }.getOrNull()

    suspend fun MapArea.renderIntersection(g2d: Graphics2D, area: RectArea<TileZoom.ChunkZoom>, offsetX: Int, offsetY: Int) {
        val chunks = ((area intersect location) ?: return).list()
        println("rendering ${chunks.size} chunks at $location, offset: $offsetX, $offsetY")
        chunks.map { chunk ->
            val chunkOffset = chunk relativeTo location
//            mapAreaScope.launch {
                println("rendering chunk ${chunk.chunkPos} at ${chunkOffset.x}, ${chunkOffset.z}, final offset: ${offsetX + chunkOffset.x * 16 * 16}, ${offsetY + chunkOffset.z * 16 * 16}")
                renderAt(
                    g2d,
                    chunk.chunkPos,
                    offsetX + chunkOffset.x * 16 * 16,
                    offsetY + chunkOffset.z * 16 * 16
                )
//            }
        }//.joinAll()
    }

    private fun MapArea.renderAt(g2d: Graphics2D, position: ChunkPos, offsetX: Int = 0, offsetY: Int = 0) {
        val chunk = getChunk(position) ?: return
        val chunkOverlay = getChunkOverlay(position) ?: return
        val biomeFoliage = getChunkBiomeFoliageAndWater(position) ?: return
        val depthmap = getChunkDepthmap(position) ?: return
        val normalmap = getChunkNormals(position) ?: return
        //                    val originalComposite = g2d.composite
        //                    val alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f)

        for (y in 0 until 16) {
            for (x in 0 until 16) {
                val drawPosX = offsetX + x * 16
                val drawPosY = offsetY + y * 16
                val block = chunk[y][x]
                val (foliageColor, oceanColor) = biomeFoliage[y][x]
                val blockOverlay = chunkOverlay[y][x]
                val depth = depthmap[y][x].toUByte().toInt()
                val normalShade = normalmap[y][x].shade

                //TODO handle lava separately

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
                        drawPosX,
                        drawPosY
                    )
                } else {
                    g2d.color = Color(block.material.color.color)
                    g2d.fillRect(drawPosX, drawPosY, 16, 16)
                }
                if (depth == 0) continue
                val sourceOverlay = ExperimentalTextureProvider.getBitmap(blockOverlay.block)
                val tmp1 = (1 - depth * 0.02f).coerceAtLeast(0f)
                val alpha = (3 - tmp1 * tmp1) / 3
                val darken = -depth * 1.6f

                val c = if (waterBlocks.contains(blockOverlay)) oceanColor
                else if (foliageBlocksSet.contains(blockOverlay)) foliageColor
                else WhyColor.White

                val darkenArray = if (!ignoreDepthTint.contains(blockOverlay)) {
                    floatArrayOf(darken, darken, darken, 0f)
                } else FloatArray(4)

                val newRop = if (waterLoggedBlocks.contains(blockOverlay)) {
                    val waterRop = RescaleOp(oceanColor.floatArray.apply { this[3] = alpha * 1.6f }, darkenArray, null)
                    g2d.drawImage(waterTexture, waterRop, drawPosX, drawPosY)
                    RescaleOp(c.floatArray, FloatArray(4), null)
                } else {
                    RescaleOp(c.floatArray.apply { this[3] = alpha * 1.6f }, darkenArray, null) //TODO don't change alpha for non-water!
                }

                if (sourceOverlay != null) {
                    g2d.drawImage(sourceOverlay, newRop, drawPosX, drawPosY)
                } else {
                    g2d.color = Color(
                        (c + (-depth * 4)).intRGB or ((alpha * 255).toInt() shl 24), //TODO use proper alpha!
                        true
                    )
                    g2d.fillRect(drawPosX, drawPosY, 16, 16)
                }

                //TODO refactor this rendering finally
            }
        }
    }

    private suspend fun renderTile(position: ChunkPos): BufferedImage? {
        return try {
            currentWorld.mapRegionManager.getRegionForTilesRendering(
                LocalTile.Region(
                    position.regionX,
                    position.regionZ
                )
            ) {
                withContext(areaCoroutineContext) {
                    BufferedImage(16 * 16, 16 * 16, BufferedImage.TYPE_INT_RGB).also {
                        renderAt(it.createGraphics(), position)
                    }
                }
            }
        } catch (e: IndexOutOfBoundsException) {
            println("   Failed to render chunk (${position.x}, ${position.z}) due to out of bounds")
            null
        } catch (e: IllegalArgumentException) {
            println("Failed to render chunk (${position.x}, ${position.z}) due do indexed image (probably)")
            null
        }
    }


}