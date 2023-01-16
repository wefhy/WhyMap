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
        "cactus"
    )

    private val forceSolid = arrayOf<String>(

    )

    fun shouldBlockOverlayBeIgnored(name: String) = ignoredOverlayBlocks.any { name.contains(it) }

    fun isOverlayForced(name: String) = forceOverlay.any { name.contains(it) }

    fun isSolidForced(name: String) = forceSolid.any { name.contains(it) }
}