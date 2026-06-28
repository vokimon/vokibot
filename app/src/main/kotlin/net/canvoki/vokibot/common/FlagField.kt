package net.canvoki.vokibot.common

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.serialization.Serializable
import net.canvoki.vokibot.R

@Serializable
data class FlagOption(
    val value: String,
    @get:StringRes val label: Int,
) {
    val bitmask: Int = value.toIntOrNull() ?: 0
}

private fun List<FlagOption>.toSelectedValues(bitmask: Int): List<String> =
    mapNotNull { if (it.bitmask and bitmask != 0) it.value else null }

private fun List<FlagOption>.toBitmask(values: List<String>): Int =
    map { it.value }.intersect(values.toSet()).mapNotNull { it.toInt() }.fold(0) { a, b -> a or b }

sealed interface FlagSerialization {
    abstract fun toString(
        values: List<String>,
        options: List<FlagOption>,
    ): String

    abstract fun fromString(
        value: String,
        options: List<FlagOption>,
    ): List<String>

    class BitMask : FlagSerialization {
        override fun toString(
            values: List<String>,
            options: List<FlagOption>,
        ): String = options.toBitmask(values).toString()

        override fun fromString(
            value: String,
            options: List<FlagOption>,
        ): List<String> = options.toSelectedValues(value.toInt())
    }
}

@Composable
fun FlagField(
    options: List<FlagOption>,
    selection: List<String>,
    onSelectionChanged: (List<String>) -> Unit,
    label: String,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            options.forEach { option ->
                val isSelected = option.value in selection
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        onSelectionChanged(
                            if (option.value in selection) {
                                selection - option.value
                            } else {
                                selection + option.value
                            },
                        )
                    },
                    label = {
                        Text(
                            stringResource(option.label),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    },
                    leadingIcon = {
                        if (isSelected) {
                            Icon(
                                painter = painterResource(R.drawable.ic_check),
                                contentDescription = null,
                                modifier = Modifier.size(FilterChipDefaults.IconSize),
                            )
                        }
                    },
                )
            }
        }
    }
}
