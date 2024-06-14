package dev.wefhy.whymap.compose.ui

/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val Icons.TwoTone.ModeNight: ImageVector
    get() {
        if (_modeNight != null) {
            return _modeNight!!
        }
        _modeNight = materialIcon(name = "TwoTone.ModeNight") {
            materialPath(fillAlpha = 0.3f, strokeAlpha = 0.3f) {
                moveTo(9.5f, 4.0f)
                curveTo(9.16f, 4.0f, 8.82f, 4.02f, 8.49f, 4.07f)
                curveTo(10.4f, 6.23f, 11.5f, 9.05f, 11.5f, 12.0f)
                reflectiveCurveToRelative(-1.1f, 5.77f, -3.01f, 7.93f)
                curveTo(8.82f, 19.98f, 9.16f, 20.0f, 9.5f, 20.0f)
                curveToRelative(4.41f, 0.0f, 8.0f, -3.59f, 8.0f, -8.0f)
                reflectiveCurveTo(13.91f, 4.0f, 9.5f, 4.0f)
                close()
            }
            materialPath {
                moveTo(9.5f, 2.0f)
                curveToRelative(-1.82f, 0.0f, -3.53f, 0.5f, -5.0f, 1.35f)
                curveToRelative(2.99f, 1.73f, 5.0f, 4.95f, 5.0f, 8.65f)
                reflectiveCurveToRelative(-2.01f, 6.92f, -5.0f, 8.65f)
                curveTo(5.97f, 21.5f, 7.68f, 22.0f, 9.5f, 22.0f)
                curveToRelative(5.52f, 0.0f, 10.0f, -4.48f, 10.0f, -10.0f)
                reflectiveCurveTo(15.02f, 2.0f, 9.5f, 2.0f)
                close()
                moveTo(9.5f, 20.0f)
                curveToRelative(-0.34f, 0.0f, -0.68f, -0.02f, -1.01f, -0.07f)
                curveToRelative(1.91f, -2.16f, 3.01f, -4.98f, 3.01f, -7.93f)
                reflectiveCurveToRelative(-1.1f, -5.77f, -3.01f, -7.93f)
                curveTo(8.82f, 4.02f, 9.16f, 4.0f, 9.5f, 4.0f)
                curveToRelative(4.41f, 0.0f, 8.0f, 3.59f, 8.0f, 8.0f)
                reflectiveCurveTo(13.91f, 20.0f, 9.5f, 20.0f)
                close()
            }
        }
        return _modeNight!!
    }

private var _modeNight: ImageVector? = null

val Icons.TwoTone.WbSunny: ImageVector
    get() {
        if (_wbSunny != null) {
            return _wbSunny!!
        }
        _wbSunny = materialIcon(name = "TwoTone.WbSunny") {
            materialPath(fillAlpha = 0.3f, strokeAlpha = 0.3f) {
                moveTo(12.0f, 7.5f)
                curveToRelative(-2.21f, 0.0f, -4.0f, 1.79f, -4.0f, 4.0f)
                reflectiveCurveToRelative(1.79f, 4.0f, 4.0f, 4.0f)
                reflectiveCurveToRelative(4.0f, -1.79f, 4.0f, -4.0f)
                reflectiveCurveToRelative(-1.79f, -4.0f, -4.0f, -4.0f)
                close()
            }
            materialPath {
                moveTo(5.34f, 6.25f)
                lineToRelative(1.42f, -1.41f)
                lineToRelative(-1.8f, -1.79f)
                lineToRelative(-1.41f, 1.41f)
                close()
                moveTo(1.0f, 10.5f)
                horizontalLineToRelative(3.0f)
                verticalLineToRelative(2.0f)
                lineTo(1.0f, 12.5f)
                close()
                moveTo(11.0f, 0.55f)
                horizontalLineToRelative(2.0f)
                lineTo(13.0f, 3.5f)
                horizontalLineToRelative(-2.0f)
                close()
                moveTo(18.66f, 6.255f)
                lineToRelative(-1.41f, -1.407f)
                lineToRelative(1.79f, -1.79f)
                lineToRelative(1.406f, 1.41f)
                close()
                moveTo(17.24f, 18.16f)
                lineToRelative(1.79f, 1.8f)
                lineToRelative(1.41f, -1.41f)
                lineToRelative(-1.8f, -1.79f)
                close()
                moveTo(20.0f, 10.5f)
                horizontalLineToRelative(3.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(-3.0f)
                close()
                moveTo(12.0f, 5.5f)
                curveToRelative(-3.31f, 0.0f, -6.0f, 2.69f, -6.0f, 6.0f)
                reflectiveCurveToRelative(2.69f, 6.0f, 6.0f, 6.0f)
                reflectiveCurveToRelative(6.0f, -2.69f, 6.0f, -6.0f)
                reflectiveCurveToRelative(-2.69f, -6.0f, -6.0f, -6.0f)
                close()
                moveTo(12.0f, 15.5f)
                curveToRelative(-2.21f, 0.0f, -4.0f, -1.79f, -4.0f, -4.0f)
                reflectiveCurveToRelative(1.79f, -4.0f, 4.0f, -4.0f)
                reflectiveCurveToRelative(4.0f, 1.79f, 4.0f, 4.0f)
                reflectiveCurveToRelative(-1.79f, 4.0f, -4.0f, 4.0f)
                close()
                moveTo(11.0f, 19.5f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.95f)
                horizontalLineToRelative(-2.0f)
                close()
                moveTo(3.55f, 18.54f)
                lineToRelative(1.41f, 1.41f)
                lineToRelative(1.79f, -1.8f)
                lineToRelative(-1.41f, -1.41f)
                close()
            }
        }
        return _wbSunny!!
    }

private var _wbSunny: ImageVector? = null
