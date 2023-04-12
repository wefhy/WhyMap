// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.whygraphics

import dev.wefhy.whymap.utils.ExpensiveCall

abstract class WhyImage(val width: Int, val height: Int) {

    @ExpensiveCall
    abstract operator fun get(y: Int, x: Int): WhyColor?
//    abstract fun average(): WhyColor
}