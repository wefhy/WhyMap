// Copyright (c) 2024 wefhy

@file:Suppress("NOTHING_TO_INLINE")

package dev.wefhy.whymap.compose.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


class MutableStateAdapter<T>(
    private val state: State<T>,
    private val mutate: (T) -> Unit
) : MutableState<T> {

    override var value: T
        get() = state.value
        set(value) {
            mutate(value)
        }

    override fun component1(): T = value
    override fun component2(): (T) -> Unit = { value = it }
}


@Composable
inline fun <T> MutableStateFlow<T>.collectAsMutableState(
    context: CoroutineContext = EmptyCoroutineContext
): MutableState<T> = MutableStateAdapter(
    state = collectAsState(context),
    mutate = { value = it }
)

//@Composable
//fun <T> MutableStateFlow<T>.collectAsMutableState(
//    context: CoroutineContext = EmptyCoroutineContext
//): MutableState<T> = MutableStateFlowWrapperState(this)
//
//private class MutableStateFlowWrapperState<T>(
//    private val state: MutableStateFlow<T>
//) : MutableState<T> {
//        override var value: T
//            get() = state.value
//            set(value) { state.value = value }
//
//        override fun component1(): T = value
//        override fun component2(): (T) -> Unit = { value = it }
//}