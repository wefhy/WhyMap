// Copyright (c) 2024 wefhy

package dev.wefhy.whymap.compose.utils

import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.key.Key.Companion
import java.awt.event.KeyEvent.VK_BACK_SPACE
import java.awt.event.KeyEvent as AwtKeyEvent

class WorkaroundKeyEventRecognizer(val interactions: WorkaroundInteractions) {

    companion object {

    }

    object Weird {
        val lShift = 0x154
        val rShift = 0x158
        val leftArraw = 263
        val rightArraw = 262
        val tab = 258
        val enter = 257
        val backspace = 259
    }

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
            Key.DirectionLeft -> interactions.onLeftArrow(keyEvent.isShiftPressed)
            Key.DirectionRight -> interactions.onRightArrow(keyEvent.isShiftPressed)
            Key.Spacebar -> interactions.onCharacterTyped(' ', keyEvent.isShiftPressed)
            in letters -> interactions.onCharacterTyped(AwtKeyEvent.getKeyText(key.nativeKeyCode).first(), keyEvent.isShiftPressed)
            in numbers -> interactions.onCharacterTyped(AwtKeyEvent.getKeyText(key.nativeKeyCode).first(), keyEvent.isShiftPressed)
            in numpad -> interactions.onCharacterTyped(AwtKeyEvent.getKeyText(key.nativeKeyCode).first(), keyEvent.isShiftPressed)
            in symbols -> interactions.onCharacterTyped(AwtKeyEvent.getKeyText(key.nativeKeyCode).first(), keyEvent.isShiftPressed)
            else -> {
                when(key.nativeKeyCode) {
                    Weird.tab -> interactions.onTab()
                    Weird.backspace -> interactions.onBackspace()
                    Weird.enter -> interactions.onEnter()
                    Weird.leftArraw -> interactions.onLeftArrow(keyEvent.isShiftPressed)
                    Weird.rightArraw -> interactions.onRightArrow(keyEvent.isShiftPressed)
                    else -> println("Unhandled key: $key, native: ${key.nativeKeyCode}")
                }
            }
        }
    }
}