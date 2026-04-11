package net.canvoki.vokibot

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import net.canvoki.shared.component.AsyncList

// ---- Model ----

data class ActivityItem(
    val label: String,
    val activityName: String,
    val icon: Drawable?,
    val supportedActions: List<String> = emptyList()
)

// ---- Query logic ----

private fun queryActivitys(
    context: Context,
    packageName: String
): List<ActivityItem> {
    val pm = context.packageManager

    val packageInfo = pm.getPackageInfo(
        packageName,
        PackageManager.GET_ACTIVITIES
    )

    val activities = packageInfo.activities ?: return emptyList()

    val activityActions = mutableMapOf<String, MutableList<String>>()

    for (standard in StandardActions.all()) {

        val intent = Intent(standard.action).apply {
            `package` = packageName
        }

        val results = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)

        for (resolveInfo in results) {
            val name = resolveInfo.activityInfo.name
            activityActions.getOrPut(name) { mutableListOf() }
                .add(standard.action)
        }
    }

    return activities
        .filter { it.exported }
        .map { activityInfo ->
            val actions = activityActions[activityInfo.name] ?: emptyList()

            ActivityItem(
                label = activityInfo.loadLabel(pm).toString(),
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

// ---- Actions icons ----

@Composable
private fun ActionIcons(actions: List<String>) {
    Row(
        modifier = Modifier.wrapContentWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        actions.take(3).forEach { action ->

            val iconRes = StandardActions.icon(action)
            Icon(
                painter = painterResource(iconRes),
                contentDescription = action,
                modifier = Modifier
                    .size(18.dp)
                    .padding(start = 4.dp)
            )
        }
    }
}

// ---- Public composable ----

@Composable
fun ActivityList(
    packageName: String,
    onSelected: (ActivityItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    AsyncList(
        refreshKeys = listOf(packageName),
        loader = { queryActivitys(context, packageName) },
        itemKey = { it.activityName },
    ) { item ->
        ActivityRow(item, onSelected)
    }
}

// ---- Row ----

@Composable
private fun ActivityRow(
    item: ActivityItem,
    onClick: (ActivityItem) -> Unit
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

        Spacer(modifier = Modifier.width(8.dp))

        ActionIcons(item.supportedActions)
    }
}
