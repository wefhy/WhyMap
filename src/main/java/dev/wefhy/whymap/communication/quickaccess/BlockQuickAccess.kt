// Copyright (c) 2023 wefhy

@file:Suppress("NOTHING_TO_INLINE")
package dev.wefhy.whymap.communication.quickaccess

import dev.wefhy.whymap.WhyMapMod
import dev.wefhy.whymap.config.RenderConfig
import dev.wefhy.whymap.config.RenderConfig.isFoliageBlock
import dev.wefhy.whymap.config.RenderConfig.isWaterBlock
import dev.wefhy.whymap.config.RenderConfig.isWaterlogged
import dev.wefhy.whymap.config.RenderConfig.shouldIgnoreAlpha
import dev.wefhy.whymap.config.RenderConfig.shouldIgnoreDepthTint
import dev.wefhy.whymap.tiles.details.ExperimentalTextureProvider
import dev.wefhy.whymap.whygraphics.WhyColor
import net.minecraft.block.Block
import net.minecraft.block.BlockState

object BlockQuickAccess { //todo should be a class per world
    //TODO rehash all the maps until most common blocks have unique buckets!
    //Maybe use ImmutableMap from guava?
    //TODO use truth tables instead of sets
    //TODO bruteforce best map capacity for vanilla block sets
    internal val minecraftBlocks = Block.STATE_IDS.map { it.block.translationKey }.toSet().toTypedArray().sortedArray() //TODO get them from the world
    private val blockNameMap = Block.STATE_IDS.map { it.block.defaultState }.associateBy { it.block.translationKey }
    private val forceOverlayLookup = Block.STATE_IDS.filter { RenderConfig.isOverlayForced(it.block.translationKey) }.toSet()
    private val forceSolidLookup = Block.STATE_IDS.filter { RenderConfig.isSolidForced(it.block.translationKey) }.toSet()
    internal val fastIgnoreLookup = minecraftBlocks.map { RenderConfig.shouldBlockOverlayBeIgnored(it) }.toTypedArray()
    internal val foliageBlocksSet = minecraftBlocks.filter { isFoliageBlock(it) }.map { blockNameMap[it] }.toSet()
    internal val waterBlocks = minecraftBlocks.filter { isWaterBlock(it) }.map { blockNameMap[it] }.toSet()
    internal val waterLoggedBlocks = minecraftBlocks.filter { isWaterlogged(it) }.map { blockNameMap[it] }.toSet()
    private val ignoreAlphaBlocks = minecraftBlocks.filter { shouldIgnoreAlpha(it) }.map { blockNameMap[it] }.toSet()
    internal val ignoreDepthTint = minecraftBlocks.filter { shouldIgnoreDepthTint(it) }.map { blockNameMap[it] }.toSet()
    private val fastLookupBlocks = minecraftBlocks.map { blockNameMap[it]!! }.toTypedArray()
    private val fastLookupBlockColor = fastLookupBlocks.map {
        ExperimentalTextureProvider.getWhyTile(it.block)?.run {
            if (it in ignoreAlphaBlocks)
                alphaInvariantAverage()//for leaves
            else
                average()//for regular blocks
        } ?: WhyColor.fromRGB(it.getMapColor(null, null).color)
    }.toTypedArray().also { WhyMapMod.LOGGER.warn("MISSING TEXTURES: ${ExperimentalTextureProvider.missingTextures}") }

    internal inline fun encodeBlock(blockState: BlockState): Short {
        val defaultState = blockState.block.translationKey
        return minecraftBlocks.binarySearch(defaultState).toShort()
    }

    internal inline fun isSolid(blockState: BlockState) = (blockState.isSolid || blockState in forceSolidLookup) && (blockState !in forceOverlayLookup)

    internal inline fun isOverlay(blockState: BlockState) = !isSolid(blockState)

    internal inline fun decodeBlock(id: Short) = fastLookupBlocks[id.toInt()]

    internal inline fun decodeBlockColor(id: Short) = fastLookupBlockColor[id.toInt()]
}