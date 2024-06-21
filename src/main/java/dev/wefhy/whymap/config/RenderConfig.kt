// Copyright (c) 2022 wefhy

@file:Suppress("NOTHING_TO_INLINE")
package dev.wefhy.whymap.config

object RenderConfig {

//    init {
//        @Suppress("UnstableApiUsage")
//        FabricWrappedVanillaResourcePack().
//    }

    private val ignoredOverlayBlocks = arrayOf(
        "air",
        "string",
        "tripwire",
        "vine",
        "barrier",
    )

    private val forceOverlay = arrayOf(
        "pointed_dripstone",
        "mangrove_roots",
        "cobweb",
        "spawner",
        "cactus",
        "bamboo",
        "glass",
        "fence",
        "wall",
        "bell",
        "honey_block",
        "chest",
        "trapdoor",
        "cake",
        "lightning_rod",
        "door",
        "anvil",
        "iron_bars",
        "brewing_stand",
        "barrier",
        "amethyst_bud",
        "lantern", //todo don't match sea lantern!
        "sniffer_egg",
//        "ice", TODO can be enabled if I take into account how transparent is every texture. Otherwise it's too transparent on zoom-out. On detail view it's perfect.
    )

    private val ignoreDepthTint = arrayOf(
        "glass"
    )

    private val forceSolid = arrayOf(
        "lava",
        "snow",
    )

    private val foliageBlocks = listOf(
        "vine",
        "leaves",
        "grass",
        "sugar",
        "fern",
        "lily_pad",
        "bedrock",
        "melon_stem",
        "pumpkin_stem",
    )

    private val blockColorIgnoreAlpha = listOf(
        "leaves",
    )

    private val waterloggedBlocks = listOf(
        "lily_pad",
        "seagrass",
        "kelp",
        "coral", //tbh coral blocks shouldn't be waterlogged but... who cares
        "sea_pickle", //TODO fix sea pickle texture
        "bubble",
        "conduit",
    )

    internal inline fun isWaterlogged(name: String) = waterloggedBlocks.any { name.contains(it) }

    internal inline fun shouldIgnoreAlpha(name: String) = blockColorIgnoreAlpha.any { name.contains(it) }

    internal inline fun shouldIgnoreDepthTint(name: String) = ignoreDepthTint.any { name.contains(it) }

    internal inline fun isWaterBlock(name: String) = name.contains("water")// || name.contains("bubble")

    internal inline fun isFoliageBlock(name: String) = foliageBlocks.any { name.contains(it) && !name.contains("cherry")}

    internal inline fun shouldBlockOverlayBeIgnored(name: String) = ignoredOverlayBlocks.any { name.contains(it) }

    internal inline fun isOverlayForced(name: String) = forceOverlay.any { name.contains(it) }

    internal inline fun isSolidForced(name: String) = forceSolid.any { name.contains(it) }
}