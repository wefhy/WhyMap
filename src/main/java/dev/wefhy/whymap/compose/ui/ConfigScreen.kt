// Copyright (c) 2024 wefhy

package dev.wefhy.whymap.compose.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.shrinkOut
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import dev.wefhy.whymap.WhyMapMod
import dev.wefhy.whymap.compose.ComposeView
import dev.wefhy.whymap.utils.Accessors.clientWindow
import dev.wefhy.whymap.utils.MapTile
import dev.wefhy.whymap.utils.TileZoom
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import java.util.*
import kotlin.random.Random

class ConfigScreen : Screen(Text.of("Config")) {

    companion object {
        private var initializationCount = 0
    }

    var i = 0
    val random = Random(0)

    private val composeView = ComposeView(
        width = clientWindow.width,
        height = clientWindow.height,
        density = Density(3f)
    ) {
        UI()
    }

    init {
        println("ConfigScreen init ${++initializationCount}")
//        RenderThreadScope.launch {
//            while (true) {
////                composeView.passLMBClick(148.8671875f, 43.724609375f)
//                composeView.passLMBClick(191f, 90f)
//                delay(random.nextLong(25, 50))
////                composeView.passLMBRelease(148.8671875f, 43.724609375f)
//                composeView.passLMBRelease(191f, 90f)
//                delay(random.nextLong(50, 550))
//            }
//        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Preview
    @Composable
    fun UI() {
        var clicks by remember { mutableStateOf(0) }
        var color by remember { mutableStateOf(Color.Green) }
        var showList by remember { mutableStateOf(true) }
        var showMap by remember { mutableStateOf(false) }
        Card(
            elevation = 20.dp, modifier = Modifier.padding(200.dp, 0.dp, 0.dp, 0.dp).padding(8.dp)/*.onPointerEvent(PointerEventType.Move) {
            val position = it.changes.first().position
            color = Color(position.x.toInt() % 256, position.y.toInt() % 256, 0)
        }*/
        ) {
            Row(Modifier.padding(8.dp)) {
                println("Recomposition ${i++}")
                Column {
                    Text("Clicks: $clicks")
                    Button(onClick = { clicks++ }) {
                        Text("Click me!")
                        color = Color(0x7F777700)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Show List")
                        Switch(checked = showList, onCheckedChange = { showList = it })
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Show Map")
                        Switch(checked = showMap, onCheckedChange = { showMap = it })
                    }
                }
//                if(showList) {
//                    LazyColumn { //TODO scrolling LazyColumn will cause race condition in Recomposer, broadcastFrameClock
//                        items(20) {
//                            Text("Item $it")
//                        }
//                    }
//                }
//                return@Row

                AnimatedVisibility(
                    showMap,
                    enter = expandIn(),
                    exit = shrinkOut()
                ) {
                    Column {
                        Text("Map")
                        MapTileView(MapTile(65532, 65543, TileZoom.RegionZoom).toLocalTile())
                    }
//                    MapTileView(LocalTileThumbnail(16383, 16384, TileZoom.ThumbnailZoom))
                }


                AnimatedVisibility(
                    showList,
                    enter = expandIn(),
                    exit = shrinkOut()
                ) {
                    val waypoints = WhyMapMod.activeWorld?.waypoints?.onlineWaypoints ?: emptyList()
                    val entries = waypoints.mapIndexed() { i, it ->
                        WaypointEntry(
                            name = it.name, distance = 0.0f, waypointId = i, date = Date(), waypointStatus = WaypointEntry.Status.NEW, waypointType = WaypointEntry.Type.SIGHTSEEING
                        )
                    }
                    WaypointsView(entries) {
                        println("Refresh!")
                    }
                    val rememberScrollState = rememberScrollState()
//                    Column(Modifier.scrollable(rememberScrollState, orientation = Orientation.Vertical)) {
                    Column(Modifier.verticalScroll(rememberScrollState)) {
//                        for (it in 0..20) {
//                            val hovered = remember { mutableStateOf(false) }
//                            Text("Item $it", Modifier.background(if (hovered.value) Color.Gray else Color.Transparent).padding(8.dp).onPointerEvent(
//                                PointerEventType.Move) {
//                                hovered.value = true
//                            })
//                        }

//                        for (entry in entries) {
//                            WaypointEntryView(entry)
//                        }
//                        WaypointsView(entries) {
//                            println("Refresh!")
//                        }
                    }
                }
            }
        }
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
//        super.render(context, mouseX, mouseY, delta)
        composeView.render(context, delta)
    }


    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
//        println("Mouse clicked at $mouseX, $mouseY")
        composeView.passLMBClick(mouseX.toFloat(), mouseY.toFloat())
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseMoved(mouseX: Double, mouseY: Double) {
        composeView.passMouseMove(mouseX.toFloat(), mouseY.toFloat())
        super.mouseMoved(mouseX, mouseY)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
//        println("Mouse released at $mouseX, $mouseY")
        composeView.passLMBRelease(mouseX.toFloat(), mouseY.toFloat())
        return super.mouseReleased(mouseX, mouseY, button)
    }

    override fun mouseScrolled(
        mouseX: Double,
        mouseY: Double,
        horizontalAmount: Double,
        verticalAmount: Double
    ): Boolean {
//        println("Mouse scrolled at $mouseX, $mouseY, $horizontalAmount, $verticalAmount")
        composeView.passScroll(mouseX.toFloat(), mouseY.toFloat(), horizontalAmount.toFloat(), verticalAmount.toFloat())
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
    }

    override fun close() {
        composeView.close()
        super.close()
    }
}