// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.events

import dev.wefhy.whymap.utils.unixTime
import kotlinx.serialization.Serializable
import java.util.concurrent.ConcurrentLinkedDeque

abstract class UpdateQueue<T> {

    @Serializable
    class QueueResponse<T>(val time: Long, val updates: ArrayList<T>)

    private var lastCleanup = 0L
    protected open val capacity = 60L //seconds
    private val queue = ConcurrentLinkedDeque<EventQueueEntry<T>>() //TODO replace with regular linkedlist and add good synchronization
    //Holy crap, kotlin library are so f***ed when dealing with linked lists... They use indexes... EVERYWHERE, even in iterators

    @Serializable
    class EventQueueEntry<T>(val entry: T, @kotlinx.serialization.Transient val time: Long = 0)

    internal fun addUpdate(item: T) {
        removeLast(item)
        val time = unixTime()
        queue.addLast(EventQueueEntry(item, time))
        if (lastCleanup + capacity < time) {
            removeOld()
        }
    }

    internal fun reset() {
        queue.clear()
    }

    internal fun getLatestUpdates(threshold: Long): QueueResponse<T> {
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