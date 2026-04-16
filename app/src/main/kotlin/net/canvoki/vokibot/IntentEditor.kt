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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap

data class ActivityContextSnapshot(
    val packageName: String,
    val componentName: String,
    val appLabel: String,
    val appIcon: Drawable?,
    val supportedActions: List<ActionDefinition>,
)

fun probeSupportedActions(
    context: Context,
    packageName: String,
    componentName: String,
): List<ActionDefinition> {
    val pm = context.packageManager
    val supported = mutableListOf<ActionDefinition>()

    val targetPackage = packageName
    val targetActivity = componentName

    for (actionDef in StandardActions.all()) {
        val intent = Intent(actionDef.action)

        val resolved = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)

        val matchesTarget =
            resolved.any { ri ->
                ri.activityInfo?.packageName == targetPackage &&
                    ri.activityInfo?.name == targetActivity
            }

        if (matchesTarget) {
            supported.add(actionDef)
        }
    }

    return supported
}

fun loadActivityContextSnapshot(
    context: Context,
    packageName: String,
    componentName: String,
): ActivityContextSnapshot {
    val pm = context.packageManager
    val appInfo = pm.getApplicationInfo(packageName, 0)

    return ActivityContextSnapshot(
        packageName = packageName,
        componentName = componentName,
        appLabel = pm.getApplicationLabel(appInfo).toString(),
        appIcon = pm.getApplicationIcon(appInfo),
        supportedActions = probeSupportedActions(context, packageName, componentName),
    )
}

@Composable
fun ActivityHeader(component: PublicComponent) {
    Row {
        Image(
            painter =
                component.icon?.let {
                    BitmapPainter(it.toBitmap().asImageBitmap())
                } ?: painterResource(R.drawable.ic_brand),
            contentDescription = null,
            modifier = Modifier.size(48.dp),
        )

        Spacer(modifier = Modifier.size(12.dp))

        Column {
            Text(component.label)
            Text(component.name)
        }
    }
}

@Composable
fun IntentActionSelector(
    supportedActions: List<ActionDefinition>,
    onSelected: (ActionDefinition?) -> Unit,
    onCustomChanged: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    var selected by remember { mutableStateOf<ActionDefinition?>(null) }
    var custom by remember { mutableStateOf("") }

    val actionsToShow =
        if (supportedActions.isNotEmpty()) {
            supportedActions
        } else {
            StandardActions.all()
        }

    LaunchedEffect(actionsToShow) {
        if (selected == null) {
            selected = actionsToShow.firstOrNull()
            onSelected(selected)
        }
    }

    Column {
        Text("Action")

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true }
                    .padding(12.dp),
        ) {
            Text(
                text = selected?.label ?: "Custom or none",
                modifier = Modifier.weight(1f),
            )

            Icon(
                painter = painterResource(R.drawable.ic_arrow_drop_down),
                contentDescription = null,
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            actionsToShow.forEach { action ->

                DropdownMenuItem(
                    text = { Text(action.label) },
                    onClick = {
                        selected = action
                        expanded = false
                        onSelected(action)
                    },
                )
            }

            DropdownMenuItem(
                text = { Text("Custom or none") },
                onClick = {
                    selected = null
                    expanded = false
                    onSelected(null)
                },
            )
        }

        if (selected == null) {
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = custom,
                onValueChange = {
                    custom = it
                    onCustomChanged(it)
                },
                label = { Text("Action string (optional)") },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
fun IntentEditor(
    packageName: String,
    component: PublicComponent,
) {
    val context = LocalContext.current
    val componentName = component.name
    val snapshot =
        remember(packageName, componentName) {
            loadActivityContextSnapshot(context, packageName, componentName)
        }

    var selectedAction by remember { mutableStateOf<ActionDefinition?>(null) }
    var customAction by remember { mutableStateOf("") }
    var extrasState by remember { mutableStateOf<Map<String, Any?>>(emptyMap()) }

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
        ) {
            ActivityHeader(component)

            Spacer(modifier = Modifier.height(16.dp))

            IntentActionSelector(
                supportedActions = snapshot.supportedActions,
                onSelected = { selectedAction = it },
                onCustomChanged = { customAction = it },
            )

            selectedAction?.let { action ->

                Spacer(modifier = Modifier.height(16.dp))

                action.extras.forEach { spec ->

                    when (spec.type) {
                        ExtraType.STRING -> {
                            val value = extrasState[spec.key] as? String ?: ""

                            OutlinedTextField(
                                value = value,
                                onValueChange = {
                                    extrasState = extrasState + (spec.key to it)
                                },
                                label = { Text(spec.label) },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }

                        ExtraType.INT -> {
                            val value = (extrasState[spec.key] as? Int)?.toString() ?: ""

                            OutlinedTextField(
                                value = value,
                                onValueChange = {
                                    extrasState = extrasState + (spec.key to it.toIntOrNull())
                                },
                                label = { Text(spec.label) },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }

                        ExtraType.BOOLEAN -> {
                            val value = extrasState[spec.key] as? Boolean ?: false

                            Row {
                                Text(spec.label, modifier = Modifier.weight(1f))
                                Switch(
                                    checked = value,
                                    onCheckedChange = {
                                        extrasState = extrasState + (spec.key to it)
                                    },
                                )
                            }
                        }

                        ExtraType.STRING_ARRAY -> {
                            val value = extrasState[spec.key] as? String ?: ""

                            OutlinedTextField(
                                value = value,
                                onValueChange = {
                                    extrasState = extrasState + (spec.key to it)
                                },
                                label = { Text("${spec.label} (comma separated)") },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }

                        ExtraType.URI,
                        ExtraType.URI_LIST,
                        -> {
                            val value = extrasState[spec.key] as? String ?: ""

                            OutlinedTextField(
                                value = value,
                                onValueChange = {
                                    extrasState = extrasState + (spec.key to it)
                                },
                                label = { Text(spec.label) },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        Button(
            onClick = {
                val intent =
                    Intent().apply {
                        setClassName(packageName, componentName)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                        when {
                            selectedAction != null -> action = selectedAction!!.action
                            customAction.isNotBlank() -> action = customAction
                        }

                        extrasState.forEach { (key, value) ->
                            when (value) {
                                is String -> putExtra(key, value)
                                is Int -> putExtra(key, value)
                                is Boolean -> putExtra(key, value)
                            }
                        }
                    }

                context.startActivity(intent)
            },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        ) {
            Text("Try")
        }
    }
}
