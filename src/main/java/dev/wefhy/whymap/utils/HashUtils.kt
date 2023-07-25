// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.utils

object HashUtils {
    fun<T> evaluateHashFunction(
        hashFunction: (T) -> Int,
        elementPopularities: Map<T, Float>,
    ): Float {
        val counts = elementPopularities.mapValues { (element, popularity) ->
            hashFunction(element) to popularity
        }.values.groupBy({ it.first }, { it.second }).values
        return counts.filter { it.size > 1 }.map { it.sum() * it.size }.sum()
    }
}