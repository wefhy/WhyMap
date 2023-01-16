package dev.wefhy.whymap.tiles.region

import dev.wefhy.whymap.WhyMapMod
import dev.wefhy.whymap.config.RenderConfig
import dev.wefhy.whymap.config.RenderConfig.isFoliageBlock
import dev.wefhy.whymap.config.RenderConfig.isWaterBlock
import dev.wefhy.whymap.config.RenderConfig.shouldIgnoreAlpha
import dev.wefhy.whymap.tiles.details.ExperimentalTextureProvider
import dev.wefhy.whymap.utils.getAverageColor
import dev.wefhy.whymap.utils.getAverageLeavesColor
import net.minecraft.block.Block
import net.minecraft.block.BlockState

object MinecraftHelper {
    internal val minecraftBlocks = Block.STATE_IDS.map { it.block.translationKey }.toSet().toTypedArray().sortedArray()
    private val blockNameMap = Block.STATE_IDS.map { it.block.defaultState }.associateBy { it.block.translationKey }
    private val forceOverlayLookup = Block.STATE_IDS.filter { RenderConfig.isOverlayForced(it.block.translationKey) }.toSet()
    private val forceSolidLookup = Block.STATE_IDS.filter { RenderConfig.isSolidForced(it.block.translationKey) }.toSet()
    val fastIgnoreLookup = minecraftBlocks.map { RenderConfig.shouldBlockOverlayBeIgnored(it) }.toTypedArray()
    val foliageBlocksSet = minecraftBlocks.filter { isFoliageBlock(it) }.map { blockNameMap[it] }.toSet()
    val waterBlocks = minecraftBlocks.filter { isWaterBlock(it) }.map { blockNameMap[it] }.toSet()
    private val ignoreAlphaBlocks = minecraftBlocks.filter { shouldIgnoreAlpha(it) }.map { blockNameMap[it] }.toSet()
    private val fastLookupBlocks = minecraftBlocks.map { blockNameMap[it]!! }.toTypedArray()
    private val fastLookupBlockColor = fastLookupBlocks.map {
        ExperimentalTextureProvider.getBitmap(it.block)?.run {
            if (it in ignoreAlphaBlocks)
                getAverageLeavesColor()
            else
                getAverageColor()
        } ?: it.material.color.color
    }.toIntArray().also { WhyMapMod.LOGGER.warn("MISSING TEXTURES: ${ExperimentalTextureProvider.missingTextures}") }

    fun encodeBlock(blockState: BlockState): Short {
        val defaultState = blockState.block.translationKey
        return minecraftBlocks.binarySearch(defaultState).toShort()
    }

    fun isOverlay(blockState: BlockState) = (blockState.material.isSolid || (blockState in forceOverlayLookup)) && (blockState !in forceSolidLookup)

    fun decodeBlock(id: Short) = fastLookupBlocks[id.toInt()]

    fun decodeBlockColor(id: Short) = fastLookupBlockColor[id.toInt()]
}