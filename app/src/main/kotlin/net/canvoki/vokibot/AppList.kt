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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import net.canvoki.shared.component.AsyncList
import net.canvoki.shared.component.StackNavigatorState
import net.canvoki.shared.component.StackedScreen
import net.canvoki.shared.component.preferences.rememberMutablePreference
import net.canvoki.vokibot.R

data class AppInfo(
    val packageName: String,
    val appName: String,
    val iconDrawable: Drawable?,
    val category: Int,
    val isSystemApp: Boolean,
) {
    companion object {
        val CATEGORY_OPTIONS: List<Pair<Int, Int>> =
            listOf(
                ApplicationInfo.CATEGORY_GAME to R.string.app_list_filter_category_game,
                ApplicationInfo.CATEGORY_MAPS to R.string.app_list_filter_category_maps,
                ApplicationInfo.CATEGORY_AUDIO to R.string.app_list_filter_category_audio,
                ApplicationInfo.CATEGORY_VIDEO to R.string.app_list_filter_category_video,
                ApplicationInfo.CATEGORY_SOCIAL to R.string.app_list_filter_category_social,
                ApplicationInfo.CATEGORY_PRODUCTIVITY to R.string.app_list_filter_category_productivity,
                ApplicationInfo.CATEGORY_UNDEFINED to R.string.app_list_filter_category_other,
            )
    }
}

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

@Serializable
data object AppList : StackedScreen<Unit>() {
    @Composable
    override fun Screen(nav: StackNavigatorState) {
        val listState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }

        AppList(
            listState = listState,
            onSelected = { app ->
                nav.push(AppComponentList(app.packageName)) { result: Unit? -> }
            },
        )
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
        Column(modifier = Modifier.weight(1f)) {
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

private fun Set<Int>.toPreferenceString(): String = joinToString(",") { it.toString() }

private fun String.toCategorySet(): Set<Int> =
    split(",").filter { it.isNotBlank() }.mapNotNull { it.toIntOrNull() }.toSet()

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
    listState: LazyListState = rememberLazyListState(),
) {
    val context = LocalContext.current
    val packageManager = context.packageManager

    var filterText by rememberMutablePreference("app_list_filter_text", "")
    var showSystemApps by rememberMutablePreference("app_list_show_system", false)
    var categoryFilterString by rememberMutablePreference("app_list_show_categories", "")

    val selectedCategories by remember { derivedStateOf { categoryFilterString.toCategorySet() } }

    var showSheet by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        AsyncList(
            refreshKeys = listOf(filterText, selectedCategories, showSystemApps),
            loader = { loadApps(packageManager, context.packageName, filterText, selectedCategories, showSystemApps) },
            itemKey = { it.packageName },
            notFoundMessage = stringResource(R.string.applist_not_found),
            listState = listState,
        ) { app ->
            AppListItem(app, onClick = { onSelected(app) })
        }

        FloatingActionButton(
            onClick = { showSheet = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_filter_list),
                contentDescription = stringResource(R.string.app_list_filter_fab_cd),
            )
        }
    }

    if (showSheet) {
        @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
        ModalBottomSheet(onDismissRequest = { showSheet = false }) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    stringResource(R.string.app_list_filter_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.size(12.dp))

                OutlinedTextField(
                    value = filterText,
                    onValueChange = { filterText = it },
                    label = {
                        Text(stringResource(R.string.app_list_filter_search_label))
                    },
                    trailingIcon = {
                        if (filterText.isNotEmpty()) {
                            IconButton(onClick = { filterText = "" }) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_close),
                                    contentDescription = stringResource(R.string.app_list_filter_clear_cd),
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.size(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.app_list_filter_show_system), modifier = Modifier.weight(1f))
                    Switch(
                        checked = showSystemApps,
                        onCheckedChange = { showSystemApps = it },
                    )
                }

                Spacer(modifier = Modifier.size(12.dp))

                Text(
                    stringResource(R.string.app_list_filter_categories_title),
                    style = MaterialTheme.typography.titleSmall,
                )
                Spacer(modifier = Modifier.size(8.dp))
                AppInfo.CATEGORY_OPTIONS.forEach { (catId, label) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = selectedCategories.contains(catId),
                            onCheckedChange = { checked ->
                                val newSet = if (checked) selectedCategories + catId else selectedCategories - catId
                                categoryFilterString = newSet.toPreferenceString()
                            },
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(label))
                    }
                }
            }
        }
    }
}
