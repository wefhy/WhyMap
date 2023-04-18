// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.utils

import net.minecraft.world.dimension.DimensionType

fun DimensionType.serialize(): String = when {
    isBedWorking -> "Overworld"
    isRespawnAnchorWorking -> "Nether"
    else -> customDimensionName()
}

private fun DimensionType.customDimensionName(): String {
    val values = booleanArrayOf(
        isBedWorking,
        isUltrawarm,
        isNatural,
        isPiglinSafe,
        isRespawnAnchorWorking,
    )
    return "CustomDimension-${values.joinToString("") { if (it) "1" else "0" }}-$coordinateScale-0-$logicalHeight"
}