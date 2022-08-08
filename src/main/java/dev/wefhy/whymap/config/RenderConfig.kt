// Copyright (c) 2022 wefhy

package dev.wefhy.whymap.config

object RenderConfig {
    private val ignoredOverlayBlocks = arrayOf(
        "string",
        "tripwire"
    )

    fun shouldBlockOverlayBeIgnored(name: String) = ignoredOverlayBlocks.any { name.contains(it) }
}