// Copyright (c) 2024 wefhy

package dev.wefhy.whymap.compose.views

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.sharp.Edit
import androidx.compose.material.icons.sharp.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import dev.wefhy.whymap.compose.ui.WaypointSorting

@Composable
fun IconRadioButton(
    selected: Boolean,
    onSelected: () -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onSelected,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) MaterialTheme.colors.primary else Color.Gray
        ),
        modifier = modifier.width(48.dp).height(48.dp)
    ) {
        Icon(
            modifier = Modifier.requiredWidth(48.dp).requiredHeight(48.dp),
            imageVector = icon,
            contentDescription = null
        )
    }
}

@Composable
fun SortingOptions(
    selectedOption: WaypointSorting,
    onOptionSelected: (WaypointSorting) -> Unit
) {
    Row(Modifier.padding(16.dp, 0.dp)) {
        IconRadioButton(
            selected = selectedOption == WaypointSorting.ALPHABETICAL,
            onSelected = { onOptionSelected(WaypointSorting.ALPHABETICAL) },
            icon = Icons.Sharp.Edit
        )
        IconRadioButton(
            selected = selectedOption == WaypointSorting.DISTANCE,
            onSelected = { onOptionSelected(WaypointSorting.DISTANCE) },
            icon = Icons.Sharp.Person
        )
        IconRadioButton(
            selected = selectedOption == WaypointSorting.DATE,
            onSelected = { onOptionSelected(WaypointSorting.DATE) },
            icon = Icons.Default.DateRange
        )
        IconRadioButton(
            selected = selectedOption == WaypointSorting.LOCATION,
            onSelected = { onOptionSelected(WaypointSorting.LOCATION) },
            icon = Icons.Default.LocationOn
        )
    }
}

@Preview
@Composable
fun Preview() {
    SortingOptions(WaypointSorting.DATE) {}
}