// Copyright (c) 2024 wefhy

package dev.wefhy.whymap.compose.utils

import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.key.Key.Companion
import java.awt.event.KeyEvent as AwtKeyEvent

class WorkaroundKeyEventRecognizer(val interactions: WorkaroundInteractions) {

    val letters = setOf(
        Key.A, Key.B, Key.C, Key.D, Key.E, Key.F, Key.G, Key.H, Key.I, Key.J, Key.K, Key.L, Key.M, Key.N, Key.O, Key.P, Key.Q, Key.R, Key.S, Key.T, Key.U, Key.V, Key.W, Key.X, Key.Y, Key.Z
    )
    val numbers = setOf(
        Key.One, Key.Two, Key.Three, Key.Four, Key.Five, Key.Six, Key.Seven, Key.Eight, Key.Nine, Key.Zero
    )
    val numpad = setOf(
        Key.NumPad0, Key.NumPad1, Key.NumPad2, Key.NumPad3, Key.NumPad4, Key.NumPad5, Key.NumPad6, Key.NumPad7, Key.NumPad8, Key.NumPad9
    )
    val symbols = setOf(
        Key.Spacebar, Key.Comma, Key.Period, Key.Semicolon, Key.Slash, Key.Minus, Key.Equals
    )
    val special = setOf(
        Key.Backspace, Key.Delete, Key.Enter, Key.DirectionLeft, Key.DirectionRight
    )
    val modifiers = setOf(
        Key.ShiftRight, Key.ShiftLeft, Key.CtrlRight, Key.CtrlLeft, Key.AltLeft, Key.AltRight
    )

    fun onKeyEvent(keyEvent: KeyEvent) {
        if (keyEvent.type != KeyEventType.KeyDown) return
        val key = keyEvent.key
        when (key) {
            Key.Backspace -> interactions.onBackspace()
            Key.Delete -> interactions.onDelete()
            Key.Enter -> interactions.onEnter()
            Key.DirectionLeft -> interactions.onLeftArrow()
            Key.DirectionRight -> interactions.onRightArrow()
            in letters -> interactions.onCharacterTyped(AwtKeyEvent.getKeyText(key.nativeKeyCode).first())
            in numbers -> interactions.onCharacterTyped(AwtKeyEvent.getKeyText(key.nativeKeyCode).first())
            in numpad -> interactions.onCharacterTyped(AwtKeyEvent.getKeyText(key.nativeKeyCode).first())
            in symbols -> interactions.onCharacterTyped(AwtKeyEvent.getKeyText(key.nativeKeyCode).first())
            else -> {
            }
        }
    }
}