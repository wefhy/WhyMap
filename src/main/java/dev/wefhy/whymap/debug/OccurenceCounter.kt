// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.debug

import java.util.concurrent.ConcurrentHashMap

object OccurenceCounter {
    private val map = ConcurrentHashMap<String, Int>()

    fun add(s: String) {
        map[s] = map.getOrDefault(s, 0) + 1
    }

    fun print() {
        map.toList().sortedByDescending { it.second }.forEach {
            println("${it.second}x ${it.first}")
        }
    }

    fun addAndPrintEvery100(s: String) {
        add(s)
        if (map[s]!! % 100 == 0) {
            println("${map[s]}x $s")
        }
    }
}