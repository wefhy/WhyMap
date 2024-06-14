// Copyright (c) 2024 wefhy

package dev.wefhy.whymap.compose.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.TextFieldColors
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TextFieldDefaults.indicatorLine
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import dev.wefhy.whymap.compose.utils.WorkaroundInteractions
import dev.wefhy.whymap.compose.utils.WorkaroundKeyEventRecognizer


@Composable
fun WorkaroundTextFieldSimple(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions(),
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = TextFieldDefaults.TextFieldShape,
    colors: TextFieldColors = TextFieldDefaults.textFieldColors(),
    onSubmit: () -> Unit = {},
    onTab: () -> Unit = {},
) {
    // If color is not provided via the text style, use content color as a default
    val textColor = textStyle.color.takeOrElse {
        colors.textColor(enabled).value
    }
    val mergedTextStyle = textStyle.merge(TextStyle(color = textColor))
    var text by remember { mutableStateOf(TextFieldValue(value)) }
    remember(value) { text = text.copy(text = value) }

    val recognizer = remember {
        WorkaroundKeyEventRecognizer(object : WorkaroundInteractions() {

            override fun onCharacterTyped(c: Char, shift: Boolean) {
                val char = if (shift) c.uppercaseChar() else c
                val newText = text.text.take(text.selection.min) + char + text.text.drop(text.selection.max)
                text = text.copy(
                    text = newText,
                    selection = textRange(text.selection.min + 1, text.selection.max + 1)
                )
                onValueChange(newText)
            }

            override fun onBackspace() {
                val newText = if (text.selection.min == text.selection.max) {
                    text.text.safeTake(text.selection.min - 1) + text.text.drop(text.selection.max)
                } else {
                    text.text.take(text.selection.min) + text.text.drop(text.selection.max)
                }
//                val newText = text.text.dropLast(1)
                text = text.copy(
                    text = newText,
                    selection = cursor(text.selection.min - 1)
                )
                onValueChange(newText)
            }

            override fun onDelete() {
                val newText = text.text.drop(1)
                text = text.copy(
                    text = newText,
                    selection = cursor(text.selection.min - 1)
                )
                onValueChange(newText)
            }

            override fun onEnter() {
                onSubmit()
            }

            override fun onTab() {
                onTab()
            }

            override fun onLeftArrow(shift: Boolean) {
                text = text.copy(
                    selection = cursor(text.selection.min - 1)
                )
            }

            override fun onRightArrow(shift: Boolean) {
                text = text.copy(
                    selection = cursor(text.selection.max + 1)
                )
            }
        })
    }

    @OptIn(ExperimentalMaterialApi::class)
    (BasicTextField(
        value = text,
        modifier = modifier
            .background(colors.backgroundColor(enabled).value, shape)
            .indicatorLine(enabled, isError, interactionSource, colors)
            .defaultMinSize(
                minWidth = TextFieldDefaults.MinHeight * 2, //TextFieldDefaults.MinWidth,
                minHeight = TextFieldDefaults.MinHeight
            )
            .onKeyEvent {
                recognizer.onKeyEvent(it)
                true
            },
        onValueChange = { text = it },
        enabled = enabled,
        readOnly = readOnly,
        textStyle = mergedTextStyle,
        cursorBrush = SolidColor(colors.cursorColor(isError).value),
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        interactionSource = interactionSource,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        decorationBox = @Composable { innerTextField ->
            // places leading icon, text field with label and placeholder, trailing icon
            TextFieldDefaults.TextFieldDecorationBox(
                value = value,
                visualTransformation = visualTransformation,
                innerTextField = innerTextField,
                placeholder = placeholder,
                label = label,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                singleLine = singleLine,
                enabled = enabled,
                isError = isError,
                interactionSource = interactionSource,
                colors = colors
            )
        }
    ))
}