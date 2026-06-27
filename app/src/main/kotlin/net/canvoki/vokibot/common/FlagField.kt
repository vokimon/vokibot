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

fun List<FlagOption>.toSelectedValues(bitmask: Int): List<String> =
    mapNotNull { if (it.bitmask and bitmask != 0) it.value else null }

fun List<FlagOption>.toBitmask(values: List<String>): Int =
    firstOrNull { it.value == values.first() }?.value?.toInt() ?:  0

@Composable
fun FlagField(
    options: List<FlagOption>,
    selectedFlags: Int,
    onFlagsChanged: (Int) -> Unit,
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
                val isSelected = selectedFlags and option.bitmask != 0
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        onFlagsChanged(selectedFlags xor option.bitmask)
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
