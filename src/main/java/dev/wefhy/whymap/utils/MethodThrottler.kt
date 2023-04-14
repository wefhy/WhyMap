// Copyright (c) 2023 wefhy

@file:OptIn(InternalCoroutinesApi::class)

package dev.wefhy.whymap.utils

import kotlinx.coroutines.InternalCoroutinesApi

//class MethodThrottler<A, B>
//private constructor(val delay: Delay, val block: (A) -> B) {
//    operator fun invoke(a: A): B {
//        return block(a)
//    }
//
//    @OptIn(InternalCoroutinesApi::class)
//    companion object {
//        private val throttlers = ConcurrentHashMap<Any, MethodThrottler<*, *>>()
//        //        fun <A, B> MethodThrottler(delay: Delay, block: (A) -> B) = throttlers.getOrPut(block) { MethodThrottler<A, B>(delay, block) } as MethodThrottler<A, B>
//        internal inline fun <reified A : Any, reified B : Any> methodThrottler(delay: Delay, noinline block: (A) -> B): MethodThrottler<A, B> {
//            return throttlers.getOrPut(block) { MethodThrottler<A, B>(delay, block) } as MethodThrottler<A, B>
//        }
//
//        internal inline fun <reified A : Any, reified B : Any> throttleMethod(delay: Delay, argument: A, noinline block: (A) -> B): B {
//            return methodThrottler(delay, block)(argument)
//        }
//    }
//}


//class MethodThrottler<T>
//private constructor(val delay: Delay, val block: (T) -> Unit) {
//
//
//
//    suspend operator fun invoke(a: T) {
//        return block(a)
//    }
//
//    @OptIn(InternalCoroutinesApi::class)
//    companion object {
//        private val throttlers = ConcurrentHashMap<Any, MethodThrottler<*>>()
//        //        fun <A, B> MethodThrottler(delay: Delay, block: (A) -> B) = throttlers.getOrPut(block) { MethodThrottler<A, B>(delay, block) } as MethodThrottler<A, B>
//        internal inline fun <reified T : Any> methodThrottler(delay: Delay, noinline block: (T) -> Unit): MethodThrottler<T> {
//            return throttlers.getOrPut(block) { MethodThrottler<T>(delay, block) } as MethodThrottler<T>
//        }
//
//        internal inline fun <reified T : Any> throttleMethod(delay: Delay, argument: T, noinline block: (T) -> Unit) {
//            GlobalScope.launch { methodThrottler(delay, block)(argument) }
//        }
//    }
//}