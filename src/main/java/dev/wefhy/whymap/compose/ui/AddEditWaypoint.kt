// Copyright (c) 2024 wefhy

package dev.wefhy.whymap.compose.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.wefhy.whymap.WhyMapMod.Companion.activeWorld
import dev.wefhy.whymap.compose.utils.ComposeUtils.goodBackground
import dev.wefhy.whymap.waypoints.CoordXYZ
import dev.wefhy.whymap.waypoints.CoordXYZ.Companion.toCoordXYZ
import net.minecraft.client.MinecraftClient

@Composable
fun AddEditWaypoint(original: WaypointEntry? = null, onDismiss: () -> Unit = {}) {
    var waypoint by remember(original) { mutableStateOf(original ?: WaypointEntry.new(0).copy(coords = MinecraftClient.getInstance()?.player?.pos?.toCoordXYZ() ?: CoordXYZ.ZERO)) }
    Column(Modifier.background(waypoint.color.copy(alpha = 0.25f)).height(220.dp).padding(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            WorkaroundTextFieldSimple(waypoint.name, { waypoint = waypoint.copy(name = it) }, Modifier.weight(1f), label = { Text("Name") })
//            WorkaroundTextFieldSimple(waypoint.name, { waypoint = waypoint.copy(name = it) }, Modifier.weight(1f), label = { Text("Name") })
            if (original != null) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", Modifier.clickable {
                    activeWorld?.waypoints?.remove(original.asOnlineWaypoint())
                    onDismiss()
                }.padding(8.dp))
            }
        }
        Row(Modifier.padding(0.dp, 4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            WorkaroundTextFieldSimple(waypoint.coords.x.toString(), { waypoint = waypoint.copy(coords = waypoint.coords.copy(x = it.toIntOrNull() ?: 0)) }, Modifier.weight(1f), label = { Text("X") })
            WorkaroundTextFieldSimple(waypoint.coords.y.toString(), { waypoint = waypoint.copy(coords = waypoint.coords.copy(y = it.toIntOrNull() ?: 0)) }, Modifier.weight(1f), label = { Text("Y") })
            WorkaroundTextFieldSimple(waypoint.coords.z.toString(), { waypoint = waypoint.copy(coords = waypoint.coords.copy(z = it.toIntOrNull() ?: 0)) }, Modifier.weight(1f), label = { Text("Z") })
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            ColorSelector { waypoint = waypoint.copy(color = it) }
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.error, contentColor = MaterialTheme.colors.onError)
                ) {
                    Icon(Icons.Default.Clear, contentDescription = "Cancel")
                }
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (original != null) {
                            activeWorld?.waypoints?.remove(original.asOnlineWaypoint())
                        }
                        activeWorld?.waypoints?.add(waypoint.asOnlineWaypoint())
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = waypoint.color, contentColor = waypoint.color.goodBackground())
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Add")
                }
            }
        }
    }
}

@Preview
@Composable
private fun PreviewAddEditWaypoint() {
    Column {
        AddEditWaypoint()
        AddEditWaypoint(WaypointEntry(12, "Test", Color.Red, 0f, CoordXYZ(0, 0, 0)))
    }
}