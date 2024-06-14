// Copyright (c) 2024 wefhy

package dev.wefhy.whymap.compose.utils

import androidx.compose.ui.text.TextRange

abstract class WorkaroundInteractions {
    abstract fun onCharacterTyped(c: Char, shift: Boolean = false)
    abstract fun onBackspace()
    abstract fun onDelete()
    abstract fun onEnter()
    abstract fun onLeftArrow(shift: Boolean = false)
    abstract fun onRightArrow(shift: Boolean = false)
    abstract fun onTab()
    protected inline fun cursor(position: Int) = textRange(position, position)
    protected inline fun textRange(min: Int, max: Int) = TextRange(min.coerceAtLeast(0), max.coerceAtLeast(0))
    protected fun String.replace(range: TextRange, replacement: String) = take(range.min) + replacement + drop(range.max)
    protected fun String.safeTake(index: Int) = take(index.coerceAtLeast(0))
}