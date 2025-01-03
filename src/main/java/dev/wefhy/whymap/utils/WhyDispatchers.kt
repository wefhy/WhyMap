// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.max

object WhyDispatchers {
    private val threads = Runtime.getRuntime().availableProcessors()
    private val safeThreads = max(threads - 1, 1) // could change to 1->1, 2->2, 3->2, 4->3,...
    val IO = Executors.newCachedThreadPool().asCoroutineDispatcher()
    val ChunkLoad = Executors.newFixedThreadPool(safeThreads).asCoroutineDispatcher()
    val Render = newReversePriorityFixedThreadPool(safeThreads).asCoroutineDispatcher()
    val RenderScope = CoroutineScope(SupervisorJob() + Render)
    val Encoding = newReversePriorityFixedThreadPool(safeThreads).asCoroutineDispatcher()
    val LowPriority = Executors.newFixedThreadPool(safeThreads, LowPriorityThreadFactory).asCoroutineDispatcher()

    object LowPriorityThreadFactory : ThreadFactory {
        private const val priority = Thread.MIN_PRIORITY
        private val i = AtomicInteger(0)
        override fun newThread(r: Runnable): Thread {
            val t = Thread(r, "LowPriorityThread" + i.getAndIncrement())
            t.isDaemon = false
            t.priority = priority
            return t
        }
    }

    fun getRenderDispatcherHealth(): Int {
        return (Render.executor as ThreadPoolExecutor).queue.size
        //todo also get max time of a task to complete
    }



    class LinkedReverseBlockingQueue<E>: LinkedBlockingDeque<E>() {
        override fun offer(e: E): Boolean {
            return super.offerFirst(e)
        }

        override fun offer(e: E, timeout: Long, unit: TimeUnit): Boolean {
            return super.offerFirst(e, timeout, unit)
        }
    }

    private fun newReversePriorityFixedThreadPool(nThreads: Int): ExecutorService {
        return ThreadPoolExecutor(
            nThreads, nThreads,
            0L, TimeUnit.MILLISECONDS,
            LinkedReverseBlockingQueue()
        )
    }
}