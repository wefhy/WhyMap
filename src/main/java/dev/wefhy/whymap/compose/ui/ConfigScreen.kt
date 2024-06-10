// Copyright (c) 2024 wefhy

package dev.wefhy.whymap.compose.ui

import androidx.compose.animation.*
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import dev.wefhy.whymap.WhyMapMod
import dev.wefhy.whymap.compose.ComposeView
import dev.wefhy.whymap.utils.Accessors.clientInstance
import dev.wefhy.whymap.utils.Accessors.clientWindow
import dev.wefhy.whymap.utils.LocalTileBlock
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import java.util.*

class ConfigScreen : Screen(Text.of("Config")) {

    private val vm = MapViewModel()

    private val composeView = ComposeView(
        width = clientWindow.width,
        height = clientWindow.height,
        density = Density(3f)
    ) {
        var visible by remember { mutableStateOf(false) }
        MaterialTheme(colors = if(vm.isDarkTheme) darkColors() else lightColors()) { //todo change theme according to minecraft day/night or real life
            LaunchedEffect(Unit) {
                visible = true
            }
            AnimatedVisibility(visible, enter = scaleIn() + fadeIn()) {
                UI(vm)
            }
//            Scaffold {
//            }
        }
    }

//    init {
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
//    }

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

private var i = 0

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun UI(vm: MapViewModel) {
    var clicks by remember { mutableStateOf(0) }
    var color by remember { mutableStateOf(Color.Green) }
    var showList by remember { mutableStateOf(true) }
    var showMap by remember { mutableStateOf(false) }
    Card(
        border = BorderStroke(1.dp, Color(0.05f, 0.1f, 0.2f)),
        elevation = 20.dp, modifier = Modifier/*.padding(200.dp, 0.dp, 0.dp, 0.dp)*/.padding(8.dp)
    ) {
        Box {
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
                        MapTileView(LocalTileBlock(clientInstance.player!!.pos))
                    }
//                    MapTileView(LocalTileThumbnail(16383, 16384, TileZoom.ThumbnailZoom))
                }


                AnimatedVisibility(
                    showList,
                    enter = expandIn(),
                    exit = shrinkOut()
                ) {
                    val waypoints = WhyMapMod.activeWorld?.waypoints?.onlineWaypoints ?: emptyList()
                    val entries = waypoints.mapIndexed { i, it ->
                        WaypointEntry(
                            name = it.name, distance = 0.0f, waypointId = i, date = Date(), waypointStatus = WaypointEntry.Status.NEW, waypointType = WaypointEntry.Type.SIGHTSEEING
                        )
                    }
                    WaypointsView(entries) {
                        println("Refresh!")
                    }
//                    val rememberScrollState = rememberScrollState()
//                    Column(Modifier.scrollable(rememberScrollState, orientation = Orientation.Vertical)) {
//                    Column(Modifier.verticalScroll(rememberScrollState)) {
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
//                    }
                }
            }
            FloatingActionButton(onClick = { vm.isDarkTheme = !vm.isDarkTheme }, Modifier.align(Alignment.TopEnd).padding(8.dp)) {
                val im = if (vm.isDarkTheme) Icons.TwoTone.ModeNight else Icons.TwoTone.WbSunny
                Icon(im, contentDescription = "Theme")
            }
        }
    }
}

@Preview
@Composable
private fun preview() {
    val vm = MapViewModel()
    vm.isDarkTheme = true
    MaterialTheme(colors = if(vm.isDarkTheme) darkColors() else lightColors()) {
        Scaffold {
            UI(vm)
        }
    }
}