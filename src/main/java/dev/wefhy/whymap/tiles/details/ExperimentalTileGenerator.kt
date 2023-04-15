// Copyright (c) 2022 wefhy

package dev.wefhy.whymap.tiles.details

import dev.wefhy.whymap.CurrentWorldProvider
import dev.wefhy.whymap.WhyWorld
import dev.wefhy.whymap.communication.quickaccess.BlockQuickAccess.foliageBlocksSet
import dev.wefhy.whymap.communication.quickaccess.BlockQuickAccess.ignoreDepthTint
import dev.wefhy.whymap.communication.quickaccess.BlockQuickAccess.waterBlocks
import dev.wefhy.whymap.communication.quickaccess.BlockQuickAccess.waterLoggedBlocks
import dev.wefhy.whymap.tiles.details.ExperimentalTextureProvider.waterTexture
import dev.wefhy.whymap.utils.LocalTile
import dev.wefhy.whymap.whygraphics.*
import kotlinx.coroutines.withContext
import net.minecraft.util.math.ChunkPos
import java.awt.image.BufferedImage
import java.awt.image.RescaleOp
import java.util.*
import kotlin.jvm.optionals.getOrNull

context(CurrentWorldProvider<WhyWorld>)
class ExperimentalTileGenerator {

    val renderedTiles = mutableMapOf<ChunkPos, Optional<BufferedImage>>()

    @OptIn(ExperimentalStdlibApi::class)
    suspend fun getTile(position: ChunkPos): BufferedImage? = renderedTiles.getOrPut(position) {
        //TODO getOrPut basically makes this whole stuff synchronous
        Optional.ofNullable(renderTile(position))
    }.getOrNull()


    private suspend fun renderTile(position: ChunkPos): BufferedImage? {
        return try {
            currentWorld.mapRegionManager.getRegionForTilesRendering(
                LocalTile.Region(
                    position.regionX,
                    position.regionZ
                )
            ) {
                withContext(areaCoroutineContext) {
                    val chunk = getChunk(position) ?: return@withContext null
                    val chunkOverlay = getChunkOverlay(position) ?: return@withContext null
                    val biomeFoliage = getChunkBiomeFoliageAndWater(position) ?: return@withContext null
                    val depthmap = getChunkDepthmap(position) ?: return@withContext null
                    val normalmap = getChunkNormals(position) ?: return@withContext null
                    val bufferedImage = BufferedImage(16 * 16, 16 * 16, BufferedImage.TYPE_INT_RGB)
                    val g2d = bufferedImage.createGraphics()
//                    val originalComposite = g2d.composite
//                    val alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f)

                    for (y in 0 until 16) {
                        for (x in 0 until 16) {
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
                                    x * 16,
                                    y * 16
                                )
                            } else {
                                g2d.color = java.awt.Color(block.material.color.color)
                                g2d.fillRect(x * 16, y * 16, 16, 16)
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
                                g2d.drawImage(waterTexture, waterRop, x * 16, y * 16)
                                RescaleOp(c.floatArray, FloatArray(4), null)
                            } else {
                                RescaleOp(c.floatArray.apply { this[3] = alpha * 1.6f }, darkenArray, null) //TODO don't change alpha for non-water!
                            }

                            if (sourceOverlay != null) {
                                g2d.drawImage(sourceOverlay, newRop, x * 16, y * 16)
                            } else {
                                g2d.color = java.awt.Color(
                                    (c + (-depth * 4)).intRGB or ((alpha * 255).toInt() shl 24), //TODO use proper alpha!
                                    true
                                )
                                g2d.fillRect(x * 16, y * 16, 16, 16)
                            }

                            //TODO refactor this rendering finally
                        }
                    }
                    bufferedImage
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