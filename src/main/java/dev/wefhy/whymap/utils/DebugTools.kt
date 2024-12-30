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


    fun<T: Any> valStatPrintLog(r: T) {
        val v = valueStats.getOrDefault(r, 0)
        val i = v + 1
        valueStats[r] = i
        if (i.isPowerOfTwo()) {
            println("$r: $i")
        }
    }

    fun Array<ShortArray>.stats() {
        val mins = this.map { it.minOrNull() ?: 0 }
        val maxs = this.map { it.maxOrNull() ?: 0 }
        println("Mins: $mins")
        println("Maxs: $maxs")
    }
}

private fun Int.isPowerOfTwo(): Boolean {
    return this > 0 && this and (this - 1) == 0
}
