// Copyright (c) 2022 wefhy

package dev.wefhy.whymap.utils

object DebugTools {

    val valueStats = mutableMapOf<Any, Int>()

    inline fun <T : Any> valueStatistics(block: () -> T): T {

        val r = block()
        val v = valueStats.getOrDefault(r, 0)
        valueStats[r] = v + 1
        return r
    }

    inline fun <T : Any> valueStatistics(r: T): T {
        val v = valueStats.getOrDefault(r, 0)
        valueStats[r] = v + 1
        return r
    }
}