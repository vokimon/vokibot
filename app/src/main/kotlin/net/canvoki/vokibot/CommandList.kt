package net.canvoki.vokibot

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import net.canvoki.shared.component.ChooserDialog
import net.canvoki.shared.component.ChooserOption

@Composable
fun CommandList(
    onLaunchAppSelected: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showTypeChooser by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        /*
        AsyncList(
            refreshKeys = listOf(filterText, selectedCategories, showSystemApps),
            loader = { loadApps(packageManager, context.packageName, filterText, selectedCategories, showSystemApps) },
            itemKey = { it.packageName },
            notFoundMessage = stringResource(R.string.applist_not_found),
            listState = listState,
        ) { app ->
            AppListItem(app, onClick = { onSelected(app) })
        }
        */

        FloatingActionButton(
            onClick = { showTypeChooser = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_add),
                contentDescription = "Create command",
            )
        }
        if (showTypeChooser) {
            ChooserDialog(
                title = "Create Command",
                options = listOf(
                    ChooserOption(value = "application", label = "Launch an Application"),
                ),
                selectedValue = "",
                onConfirm = { value ->
                    showTypeChooser = false
                    if (value == "application") {
                        onLaunchAppSelected()
                    }
                },
                onDismiss = { showTypeChooser = false },

            )
        }
    }
}
