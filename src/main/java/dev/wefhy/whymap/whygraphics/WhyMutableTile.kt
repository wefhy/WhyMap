// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.whygraphics

class WhyMutableTile: WhyTile() {

    fun draw(o: WhyTile) {
        for (i in 0 until WhyTile.arraySize) {
            data[i] = o.data[i] alphaOver data[i]
        }
    }

    fun getLineView(y: Int): List<WhyColor> {
        return data.asList().subList(y shl lineShl, y shl lineShl - 1)
    }
}