// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.communication.quickaccess

class UnsafeTrieMap<T: Any> private constructor() {

    // TODO benchmark binary search vs trie vs hashmap

    /*
    TODO
    currently if there are keys: "aaa" and "baaa" then both will be accessed with 1 operation
    but if there are keys ilke "aaaaaabc" and "aaaaaabd" then both will be accessed 8 operations
    maybe there is a way to optimize this add some 'step' value to the node that says how many characters to skip
    Note that in minecraft there's a lot of keys like this:
    block.minecraft.stone
    block.minecraft.stone_brick_slab
    block.minecraft.stone_brick_stairs
    block.minecraft.stone_brick_wall
    block.minecraft.stone_bricks
    block.minecraft.stone_button
    block.minecraft.stone_pressure_plate
    block.minecraft.stone_slab
    block.minecraft.stone_stairs
    block.minecraft.stonecutter

    So a node should have a 'step' value that says how many characters to skip when accessing it
    of course this will require optimization after adding all the keys
     */

    private val root = Node<T>()

    operator fun get(key: String): T? {
        var node = root
        for (c in key) {
//            val id = c.safeId ?: continue
            val id = if(c.inRange) c.id else continue
            node = node.children[id] ?: return null
            if (node.childrenSize == 0 && node.value != null) return node.value
        }
        return node.value
    }

    private operator fun set(key: String, value: T) {
        var node = root
        for (c in key) {
//            val id = c.safeId ?: continue
            val id = if(c.inRange) c.id else continue
            val child = node.children[id] ?: Node<T>().also { node.children[id] = it }
            node.childrenSize++
            node = child
        }
        if (node.value == null) {
            node.value = value
        } else {
            throw IllegalStateException("Key '$key' already exists")
        }
    }

    private fun optimize() {
        root.optimize()
    }

    private fun Node<T>.optimize() {
        if (childrenSize == 0) return
        if (childrenSize == 1 && value == null) {
            value = getOnlyValue()
            fillNulls()
            childrenSize = 0
            return
        }
        for (child in children) {
            child?.optimize()
        }
    }

    private fun Node<T>.getOnlyValue(): T {
        assert(childrenSize == 1 || (value != null && childrenSize == 0)) // TODO remove when map is tested
        return value ?: children.first { it != null }!!.getOnlyValue() // tbh this should be smart cast without !!
    }

    private fun Node<T>.fillNulls() {
        for (i in children.indices) {
            if (children[i] == null) {
                children[i] = null
            }
        }
    }

    class Node<T> {
        var value: T? = null
        var childrenSize = 0
        val children = Array<Node<T>?>(size) { null }
    }

    companion object {
        private const val maxIndex = 'z' - 'a'
        private const val size = maxIndex + 1

        private inline val Char.id
            inline get() = this - 'a'

        private inline val Char.inRange
            inline get() = ('a' <= this) && (this <= 'z')
//            inline get() = this in 'a'..'z'

        private inline val Char.safeId
            inline get() = if(inRange) id else null

        fun fromStringArray(array: Array<String>): UnsafeTrieMap<Int> {
            val map = UnsafeTrieMap<Int>()
            for (i in array.indices) {
                map[array[i]] = i
            }
            map.optimize()
            return map
        }

        fun<T: Any> fromMap(map: Map<String, T>): UnsafeTrieMap<T> {
            val trieMap = UnsafeTrieMap<T>()
            for ((key, value) in map) {
                trieMap[key] = value
            }
            trieMap.optimize()
            return trieMap
        }
    }
}