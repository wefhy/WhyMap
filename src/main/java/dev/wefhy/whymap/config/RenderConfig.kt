// Copyright (c) 2022 wefhy

package dev.wefhy.whymap.config

object RenderConfig {
    private val ignoredOverlayBlocks = arrayOf(
        "string",
        "tripwire"
    )

    private val forceOverlay = arrayOf(
        "pointed_dripstone",
        "mangrove_roots",
        "cobweb",
        "spawner",
        "cactus",
        "bamboo"
    )

    private val forceSolid = arrayOf<String>(

    )

    private val foliageBlocks = listOf(
        "vine",
        "leaves",
        "grass",
        "sugar",
        "fern",
        "lily",
        "bedrock",
    )

    private val blockColorIgnoreAlpha = listOf(
        "leaves"
    )

    private val waterloggedBlocks = listOf(
        "lily",
        "seagrass"
    )

    fun isWaterlogged(name: String) = waterloggedBlocks.any { name.contains(it) }

    fun shouldIgnoreAlpha(name: String) = blockColorIgnoreAlpha.any { name.contains(it) }

    fun isWaterBlock(name: String) = name.contains("water")

    fun isFoliageBlock(name: String) = foliageBlocks.any { name.contains(it) }

    fun shouldBlockOverlayBeIgnored(name: String) = ignoredOverlayBlocks.any { name.contains(it) }

    fun isOverlayForced(name: String) = forceOverlay.any { name.contains(it) }

    fun isSolidForced(name: String) = forceSolid.any { name.contains(it) }
}