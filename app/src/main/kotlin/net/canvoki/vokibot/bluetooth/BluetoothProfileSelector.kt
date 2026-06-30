package net.canvoki.vokibot.bluetooth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.canvoki.vokibot.R

@Composable
fun BluetoothProfileSelector(
    affectedRoles: Set<DisconnectableRole>,
    onAffectedRolesChange: (Set<DisconnectableRole>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var selectiveMode by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier) {
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            listOf(
                false to stringResource(R.string.bluetooth_editor_disconnect_mode_full),
                true to stringResource(R.string.bluetooth_editor_disconnect_mode_selective),
            ).forEachIndexed { index, (isSelective, label) ->
                SegmentedButton(
                    selected = selectiveMode == isSelective,
                    onClick = {
                        selectiveMode = isSelective
                        if (isSelective) {
                            if (affectedRoles.isEmpty()) {
                                onAffectedRolesChange(DisconnectableRole.entries.toSet())
                            }
                        } else {
                            onAffectedRolesChange(emptySet())
                        }
                    },
                    shape = SegmentedButtonDefaults.itemShape(index, 2),
                    label = { Text(label) },
                )
            }
        }
        if (selectiveMode) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                DisconnectableRole.entries.forEach { role ->
                    FilterChip(
                        selected = role in affectedRoles,
                        onClick = {
                            onAffectedRolesChange(
                                if (role in affectedRoles) {
                                    affectedRoles - role
                                } else {
                                    affectedRoles + role
                                },
                            )
                        },
                        label = {
                            Text(
                                role.getLabel(context),
                                style = MaterialTheme.typography.labelSmall,
                            )
                        },
                        leadingIcon = {
                            if (role in affectedRoles) {
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
}
