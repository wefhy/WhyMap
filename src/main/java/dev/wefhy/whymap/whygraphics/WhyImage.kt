// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.whygraphics

abstract class WhyImage(val width: Int, val height: Int) {

    @ExpensiveCall
    abstract operator fun get(y: Int, x: Int): WhyColor?
//    abstract fun average(): WhyColor

    @RequiresOptIn("This call might be expensive, consider using direct array access")
    annotation class ExpensiveCall
}