package net.canvoki.vokibot

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap

// ---- Known actions ----

data class KnownAction(
    val action: String,
    @DrawableRes val iconRes: Int
)

private val knownActions = listOf(
    KnownAction(Intent.ACTION_VIEW, R.drawable.ic_visibility),
    KnownAction(Intent.ACTION_SEND, R.drawable.ic_send),
    KnownAction(Intent.ACTION_SENDTO, R.drawable.ic_mail),
    KnownAction(Intent.ACTION_DIAL, R.drawable.ic_call),
    KnownAction(Intent.ACTION_CALL, R.drawable.ic_phone),
    KnownAction(Intent.ACTION_EDIT, R.drawable.ic_edit),
    KnownAction(Intent.ACTION_PICK, R.drawable.ic_photo_library),
    KnownAction(Intent.ACTION_MAIN, R.drawable.ic_apps)
)

// ---- Model ----

data class AppIntentItem(
    val label: String,
    val activityName: String,
    val icon: Drawable?,
    val supportedActions: List<String> = emptyList()
)

// ---- Query logic ----

private fun queryAppIntents(
    context: Context,
    packageName: String
): List<AppIntentItem> {
    val pm = context.packageManager

    val packageInfo = pm.getPackageInfo(
        packageName,
        PackageManager.GET_ACTIVITIES
    )

    val activities = packageInfo.activities ?: return emptyList()

    val activityActions = mutableMapOf<String, MutableList<String>>()

    for (known in knownActions) {
        val intent = Intent(known.action).apply {
            `package` = packageName
        }

        val results = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)

        for (resolveInfo in results) {
            val name = resolveInfo.activityInfo.name
            activityActions.getOrPut(name) { mutableListOf() }
                .add(known.action)
        }
    }

    return activities
        .filter { it.exported }
        .map { activityInfo ->
            val actions = activityActions[activityInfo.name] ?: emptyList()

            AppIntentItem(
                label = activityInfo.loadLabel(pm)?.toString()
                    ?: activityInfo.name.substringAfterLast('.'),
                activityName = activityInfo.name,
                icon = activityInfo.loadIcon(pm),
                supportedActions = actions
            )
        }
        .sortedBy { it.label.lowercase() }
}

// ---- Drawable -> Painter ----

@Composable
private fun drawableToPainter(drawable: Drawable?): Painter {
    return drawable?.let {
        BitmapPainter(it.toBitmap().asImageBitmap())
    } ?: painterResource(R.drawable.ic_brand)
}

// ---- Action icon ----

@Composable
private fun actionIcon(actions: List<String>): Painter? {
    val match = knownActions.firstOrNull { it.action in actions }
    return match?.let { painterResource(it.iconRes) }
}

// ---- Public composable ----

@Composable
fun AppIntentList(
    packageName: String,
    onSelected: (AppIntentItem) -> Unit
) {
    val context = LocalContext.current

    var items by remember(packageName) {
        mutableStateOf(emptyList<AppIntentItem>())
    }

    LaunchedEffect(packageName) {
        items = queryAppIntents(context, packageName)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(items) { item ->
            AppIntentRow(item, onSelected)
        }
    }
}

// ---- Row ----

@Composable
private fun AppIntentRow(
    item: AppIntentItem,
    onClick: (AppIntentItem) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(item) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = drawableToPainter(item.icon),
            contentDescription = item.label,
            modifier = Modifier.size(40.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.label,
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = item.activityName,
                style = MaterialTheme.typography.bodySmall
            )
        }

        val trailing = actionIcon(item.supportedActions)

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            painter = trailing ?: painterResource(R.drawable.ic_brand),
            contentDescription = "Action",
            modifier = Modifier.size(20.dp)
        )
    }
}
