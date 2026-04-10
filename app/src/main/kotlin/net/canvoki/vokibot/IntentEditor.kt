package net.canvoki.vokibot

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Arrangement

data class ActivityInfo(
    val packageName: String,
    val activityName: String,
    val appLabel: String,
    val appIcon: Drawable?
)

fun loadActivityInfo(
    context: Context,
    packageName: String,
    activityName: String
): ActivityInfo {

    val pm = context.packageManager

    val appInfo = pm.getApplicationInfo(packageName, 0)

    return ActivityInfo(
        packageName = packageName,
        activityName = activityName,
        appLabel = pm.getApplicationLabel(appInfo).toString(),
        appIcon = pm.getApplicationIcon(appInfo)
    )
}

@Composable
fun ActivityHeader(
    info: ActivityInfo
) {
    Row {

        Image(
            painter = info.appIcon?.let {
                BitmapPainter(it.toBitmap().asImageBitmap())
            } ?: painterResource(R.drawable.ic_brand),
            contentDescription = null
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(text = info.appLabel)
            Text(text = info.activityName)
        }
    }
}

@Composable
fun IntentEditor(
    packageName: String,
    activityName: String
) {
    val context = LocalContext.current

    val info = remember(packageName, activityName) {
        loadActivityInfo(context, packageName, activityName)
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
    ) {

        Column {
            ActivityHeader(info = info)

            Spacer(modifier = Modifier.height(16.dp))

            Text("Preparing intent editor...")
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
