// Copyright (c) 2022 wefhy

package dev.wefhy.whymap.utils

object ObfuscatedLogHelper {

    val objectMap = mutableMapOf<Any, Int>()
//    val commandMap = mutableMapOf<Pair<String, String>, Int>()

    val cmdMap = mutableMapOf<String, MutableList<String>>()
    var i: Int = 0

    fun<T> MutableMap<T, Int>.increment(value: T) {
        this[value] = getOrElse(value) { 0 }
    }

    fun<A, B> MutableMap<A, MutableList<B>>.appendTo(value: A, element: B) {
        getOrPut(value) {
            mutableListOf()
        } += element
    }

    fun obfuscateObject(obj: Any): String {
        val identifier = objectMap.getOrPut(obj) { ++i }
        return identifier.toString(36).uppercase()
    }

    fun obfuscateObjectWithCommand(obj: Any, command: String): String {
        val result = obfuscateObject(obj)
        cmdMap.appendTo(result, command)
//        commandMap.increment(Pair(result, command))
        return result
    }

    fun dumpStats(): String {
        return cmdMap.entries.joinToString("\n") {
            "${it.key}(${it.value.groupBy { it }.entries.joinToString(", ") { "${it.key}: ${it.value.size}" }})"
        }.also { cmdMap.clear() }
    }

    fun dumpMap(): String {
        return objectMap.entries.joinToString("\n") {
            "${it.value.toString(36).uppercase()}: ${it.key}"
        }.also { objectMap.clear(); i = 0 }
    }
}