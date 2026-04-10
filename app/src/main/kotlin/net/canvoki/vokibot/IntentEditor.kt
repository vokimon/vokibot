package net.canvoki.vokibot

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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

    val appLabel = pm.getApplicationLabel(appInfo).toString()
    val appIcon = pm.getApplicationIcon(appInfo)

    return ActivityContextSnapshot(
        packageName = packageName,
        activityName = activityName,
        appLabel = appLabel,
        appIcon = appIcon,
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
            contentDescription = null
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(snapshot.appLabel)
            Text(snapshot.activityName)
        }
    }
}

@Composable
fun ActionIcons(actions: List<ActionDefinition>) {
    Row {

        actions.take(4).forEach { action ->

            Icon(
                painter = painterResource(action.iconRes),
                contentDescription = action.action,
                modifier = Modifier
                    .width(18.dp)
                    .height(18.dp)
            )

            Spacer(modifier = Modifier.width(4.dp))
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

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        Column {

            ActivityHeader(snapshot)

            Spacer(modifier = Modifier.height(16.dp))

            Text("Supported actions:")

            ActionIcons(snapshot.supportedActions)
        }

        Button(
            onClick = {
                val intent = Intent().apply {
                    setClassName(packageName, activityName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
        ) {
            Text("Try")
        }
    }
}
