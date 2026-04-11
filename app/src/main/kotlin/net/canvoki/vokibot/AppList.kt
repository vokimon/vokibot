package net.canvoki.vokibot

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.canvoki.shared.component.AsyncList
import net.canvoki.vokibot.R

data class AppInfo(
    val packageName: String,
    val appName: String,
    val iconDrawable: Drawable?,
    val category: Int,
    val isSystemApp: Boolean,
)

private fun Drawable.toPainter(): Painter =
    when (this) {
        is BitmapDrawable -> BitmapPainter(bitmap.asImageBitmap())
        else -> {
            val bitmap =
                android.graphics.Bitmap.createBitmap(
                    intrinsicWidth.coerceAtLeast(48),
                    intrinsicHeight.coerceAtLeast(48),
                    android.graphics.Bitmap.Config.ARGB_8888,
                )
            val canvas = android.graphics.Canvas(bitmap)
            setBounds(0, 0, canvas.width, canvas.height)
            draw(canvas)
            BitmapPainter(bitmap.asImageBitmap())
        }
    }

@Composable
fun AppListItem(
    app: AppInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .clickable(onClick = onClick)
                .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val iconPainter = remember(app.iconDrawable) { app.iconDrawable?.toPainter() }

        Icon(
            painter = iconPainter ?: painterResource(R.drawable.ic_brand),
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint =
                if (iconPainter == null) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    androidx.compose.ui.graphics.Color.Unspecified
                },
        )

        Spacer(modifier = Modifier.width(16.dp))
        androidx.compose.foundation.layout.Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.appName,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = app.packageName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private suspend fun loadApps(
    packageManager: PackageManager,
    ownPackageName: String,
    filterText: String,
    filterCategories: Set<Int>,
    showSystemApps: Boolean,
): List<AppInfo> =
    withContext(Dispatchers.IO) {
        packageManager
            .getInstalledPackages(0)
            .filter { it.packageName != ownPackageName }
            .mapNotNull { pkg ->
                try {
                    val ai = pkg.applicationInfo ?: return@mapNotNull null
                    val appName = ai.loadLabel(packageManager).toString()
                    val icon = ai.loadIcon(packageManager)
                    val category = ai.category
                    val isSystem = (ai.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    AppInfo(pkg.packageName, appName, icon, category, isSystem)
                } catch (_: Exception) {
                    null
                }
            }.filter { app ->
                (filterText.isBlank() || app.appName.contains(filterText, ignoreCase = true)) &&
                    (filterCategories.isEmpty() || filterCategories.contains(app.category)) &&
                    (showSystemApps || !app.isSystemApp)
            }.sortedBy { it.appName.lowercase() }
    }

@Composable
fun AppList(
    onSelected: (AppInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    var filterText by remember { mutableStateOf("") }
    var selectedCategories by remember { mutableStateOf(setOf<Int>()) }
    var showSystemApps by remember { mutableStateOf(true) }
    var showSheet by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val packageManager = context.packageManager

    Box(modifier = modifier.fillMaxSize()) {
        AsyncList(
            refreshKeys = listOf(filterText, selectedCategories, showSystemApps),
            loader = { loadApps(packageManager, context.packageName, filterText, selectedCategories, showSystemApps) },
            itemKey = { it.packageName },
            notFoundMessage = stringResource(R.string.applist_not_found),
        ) { app ->
            AppListItem(app, onClick = { onSelected(app) })
        }

        FloatingActionButton(
            onClick = { showSheet = true },
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_filter_list),
                contentDescription = "Filter",
            )
        }
    }

    if (showSheet) {
        @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Filters", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.size(12.dp))

                OutlinedTextField(
                    value = filterText,
                    onValueChange = { filterText = it },
                    label = { Text("Search") },
                    trailingIcon = {
                        if (filterText.isNotEmpty()) {
                            IconButton(onClick = { filterText = "" }) {
                                Icon(painterResource(R.drawable.ic_close), contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.size(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Show system apps", modifier = Modifier.weight(1f))
                    Switch(
                        checked = showSystemApps,
                        onCheckedChange = { showSystemApps = it },
                    )
                }

                Spacer(modifier = Modifier.size(12.dp))

                val categories =
                    listOf(
                        ApplicationInfo.CATEGORY_GAME to "Games",
                        ApplicationInfo.CATEGORY_MAPS to "Maps",
                        ApplicationInfo.CATEGORY_AUDIO to "Music/Audio",
                        ApplicationInfo.CATEGORY_VIDEO to "Video",
                        ApplicationInfo.CATEGORY_SOCIAL to "Social",
                        ApplicationInfo.CATEGORY_PRODUCTIVITY to "Productivity",
                        ApplicationInfo.CATEGORY_UNDEFINED to "Other",
                    )

                Text("Categories", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.size(8.dp))
                categories.forEach { (catId, label) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = selectedCategories.contains(catId),
                            onCheckedChange = { checked ->
                                selectedCategories =
                                    if (checked) {
                                        selectedCategories + catId
                                    } else {
                                        selectedCategories - catId
                                    }
                            },
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(label)
                    }
                }
            }
        }
    }
}
