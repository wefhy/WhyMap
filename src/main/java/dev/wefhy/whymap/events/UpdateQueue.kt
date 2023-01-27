// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.events

import dev.wefhy.whymap.utils.unixTime
import kotlinx.serialization.Serializable
import java.util.*

/**
 * Heavily optimized for this only purpose
 */
abstract class UpdateQueue<T : Any> {

    @Serializable
    class QueueResponse<T : Any>(val time: Long, val updates: ArrayList<T>)

    private var lastCleanup = 0L
    protected abstract val capacity: Long //seconds
    private val queue = LinkedList<EventQueueEntry<T>>()
    //Holy crap, kotlin library are so f***ed when dealing with linked lists... They use indexes... EVERYWHERE, even in iterators

    @Serializable
    class EventQueueEntry<T : Any>(val entry: T, @kotlinx.serialization.Transient val time: Long = 0) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as EventQueueEntry<*>
            return entry == other.entry
        }

        override fun hashCode(): Int = entry.hashCode()
        override fun toString(): String = "EQE(e=$entry, t=$time)"
    }

    internal fun addUpdate(item: T) = synchronized(this) {
        removeLast(item)
//            println("Added $item, removed: $didRemove, length: ${queue.size}, distinct: ${queue.toSet().size}, items: ${queue.joinToString { "($it: ${it.hashCode()})" }}")
        val time = unixTime()
        queue.addLast(EventQueueEntry(item, time))
        if (lastCleanup + capacity < time) {
            removeOld()
        }
    }

    internal fun reset() = synchronized(this) {
        queue.clear()
    }


    internal fun getLatestUpdates(threshold: Long): QueueResponse<T> = synchronized(this) {
        val time = unixTime()
        val updates = ArrayList<T>(queue.size)
        val iterator = queue.descendingIterator()
        while (iterator.hasNext()) {
            val t = iterator.next()
            if (t.time < threshold) {
                break
            }
            updates += t.entry
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
     * There can be only one occurence anyway
     */
    private fun removeLast(entry: T) {
        val iterator = queue.descendingIterator()
        while (iterator.hasNext()) {
            if (iterator.next().entry == entry) {
                iterator.remove()
                return
            }
        }
    }
}