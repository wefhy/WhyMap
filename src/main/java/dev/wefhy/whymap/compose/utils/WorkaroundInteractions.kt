// Copyright (c) 2024 wefhy

package dev.wefhy.whymap.compose.utils

interface WorkaroundInteractions {
    fun onCharacterTyped(c: Char)
    fun onBackspace()
    fun onDelete()
    fun onEnter()
    fun onLeftArrow()
    fun onRightArrow()
}