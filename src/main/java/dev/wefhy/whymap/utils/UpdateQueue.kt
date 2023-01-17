// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.utils

import kotlinx.serialization.Serializable
import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque
import kotlin.collections.ArrayList


/**
 * Heavily optimized for this only purpose
 */
object UpdateQueue { //TODO separate for every world / dimension?

    //TODO add new areas to queue!

    @Serializable
    class QueueResponse (val time: Long, val updates: ArrayList<ChunkUpdatePosition>)

    @Serializable
    class ChunkUpdatePosition (val x: Int, val z: Int, @kotlinx.serialization.Transient val time: Long = 0)

    private var lastCleanup = 0L
    private val capacity = 60L //seconds
    private val queue = ConcurrentLinkedDeque<ChunkUpdatePosition>() //TODO replace with regular linkedlist and add good synchronization
    //Holy crap, kotlin library are so f***ed when dealing with linked lists... They use indexes... EVERYWHERE, even in iterators

    internal fun addUpdate(x: Int, z: Int) {
        removeLast(x, z)
        val time = unixTime()
        queue.addLast(ChunkUpdatePosition(x, z, time))
        if (lastCleanup + capacity < time) {
            removeOld()
        }
    }

//    internal fun getAllUpdates() = queue.toTypedArray()

    internal fun reset() {
        queue.clear()
    }

    internal fun getLatestUpdates(threshold: Long): QueueResponse {
        val time = unixTime()
        val updates = ArrayList<ChunkUpdatePosition>(queue.size)
        val iterator = queue.descendingIterator()
        while (iterator.hasNext()) {
            val t = iterator.next()
            if (t.time < threshold) {
                break
            }
            updates += t
        }
        return QueueResponse(time, updates)
    }

    private fun removeOld() {
        val time = unixTime()
        val threshold = time - capacity
        while (queue.isNotEmpty() && queue.peek().time < threshold) {
            queue.removeFirst()
        }
        lastCleanup = time
    }

    /**
     * Searches list from the end because it's most likely the chunk was updated recently
     *
     * There can be only one occurence anyway
     */
    private fun removeLast(x: Int, z: Int) {
        val iterator = queue.descendingIterator()
        while (iterator.hasNext()) {
            if (iterator.next().let { it.x == x && it.z == z }) {
                iterator.remove()
                return
            }
        }
    }
}