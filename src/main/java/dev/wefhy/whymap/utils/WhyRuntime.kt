// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.utils

import net.fabricmc.loader.api.FabricLoader

object WhyRuntime {
    val isClothConfigInstalled = FabricLoader.getInstance().isModLoaded("cloth-config")
}