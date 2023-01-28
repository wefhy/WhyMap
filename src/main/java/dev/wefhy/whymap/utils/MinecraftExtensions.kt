// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.utils

import net.minecraft.world.dimension.DimensionType

fun DimensionType.serialize(): String = when {
    bedWorks -> "Overworld"
    respawnAnchorWorks -> "Nether"
    else -> customDimensionName()
}

private fun DimensionType.customDimensionName(): String {
    val values = booleanArrayOf(
        bedWorks,
        ultrawarm,
        natural,
        piglinSafe(),
        respawnAnchorWorks,
    )
    return "CustomDimension-${values.joinToString("") { if (it) "1" else "0" }}-$coordinateScale-$height-$logicalHeight"
}