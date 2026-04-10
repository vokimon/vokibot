package net.canvoki.vokibot

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap

data class ActivityContextSnapshot(
    val packageName: String,
    val activityName: String,
    val appLabel: String,
    val appIcon: Drawable?,
    val supportedActions: List<ActionDefinition>
)

fun probeSupportedActions(
    context: Context,
    packageName: String,
    activityName: String
): List<ActionDefinition> {

    val pm = context.packageManager
    val supported = mutableListOf<ActionDefinition>()

    for (actionDef in StandardActions.all()) {

        val intent = Intent(actionDef.action).apply {
            setClassName(packageName, activityName)
        }

        val resolved = pm.queryIntentActivities(intent, 0)

        if (resolved.isNotEmpty()) {
            supported.add(actionDef)
        }
    }

    return supported
}

fun loadActivityContextSnapshot(
    context: Context,
    packageName: String,
    activityName: String
): ActivityContextSnapshot {

    val pm = context.packageManager

    val appInfo = pm.getApplicationInfo(packageName, 0)

    return ActivityContextSnapshot(
        packageName = packageName,
        activityName = activityName,
        appLabel = pm.getApplicationLabel(appInfo).toString(),
        appIcon = pm.getApplicationIcon(appInfo),
        supportedActions = probeSupportedActions(
            context,
            packageName,
            activityName
        )
    )
}

@Composable
fun ActivityHeader(snapshot: ActivityContextSnapshot) {

    Row {

        Image(
            painter = snapshot.appIcon?.let {
                BitmapPainter(it.toBitmap().asImageBitmap())
            } ?: painterResource(R.drawable.ic_brand),
            contentDescription = null,
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.size(12.dp))

        Column {
            Text(snapshot.appLabel)
            Text(snapshot.activityName)
        }
    }
}

@Composable
fun IntentActionSelector(
    supportedActions: List<ActionDefinition>,
    onSelected: (ActionDefinition?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val allActions = StandardActions.all()

    val useAll = supportedActions.isEmpty() || supportedActions.size == allActions.size
    val options = if (useAll) allActions else supportedActions

    var selected by remember {
        mutableStateOf<ActionDefinition?>(
            if (useAll) null else options.firstOrNull()
        )
    }

    Column {

        Text("Action")

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .padding(12.dp)
        ) {

            Text(
                text = selected?.label ?: "Custom",
                modifier = Modifier.weight(1f)
            )

            Icon(
                painter = painterResource(R.drawable.ic_arrow_drop_down),
                contentDescription = null
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {

            DropdownMenuItem(
                text = { Text("Custom") },
                onClick = {
                    selected = null
                    expanded = false
                    onSelected(null)
                }
            )

            options.forEach { action ->

                DropdownMenuItem(
                    text = { Text(action.label) },
                    onClick = {
                        selected = action
                        expanded = false
                        onSelected(action)
                    }
                )
            }
        }
    }
}

@Composable
fun IntentEditor(
    packageName: String,
    activityName: String
) {
    val context = LocalContext.current

    val snapshot = remember(packageName, activityName) {
        loadActivityContextSnapshot(
            context,
            packageName,
            activityName
        )
    }

    var selectedAction by remember { mutableStateOf<ActionDefinition?>(null) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {

            ActivityHeader(snapshot)

            Spacer(modifier = Modifier.height(16.dp))

            IntentActionSelector(
                supportedActions = snapshot.supportedActions,
                onSelected = { selectedAction = it }
            )
        }

        Button(
            onClick = {
                val intent = Intent().apply {
                    setClassName(packageName, activityName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                    selectedAction?.let {
                        action = it.action
                    }
                }

                context.startActivity(intent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Try")
        }
    }
}
