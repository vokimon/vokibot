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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.canvoki.vokibot.R

data class AppInfo(
    val packageName: String,
    val appName: String,
    val iconDrawable: Drawable?,
)

/**
 * Converteix un Drawable a Painter per a Compose.
 * Gestiona BitmapDrawable directament i fallback per a altres tipus.
 */
private fun Drawable.toPainter(): Painter {
    return when (this) {
        is BitmapDrawable -> BitmapPainter(bitmap.asImageBitmap())
        else -> {
            // Per a VectorDrawable, AdaptiveIconDrawable, etc.
            // Dibuxem a un bitmap temporal
            val bitmap = android.graphics.Bitmap.createBitmap(
                intrinsicWidth.coerceAtLeast(48),
                intrinsicHeight.coerceAtLeast(48),
                android.graphics.Bitmap.Config.ARGB_8888
            )
            val canvas = android.graphics.Canvas(bitmap)
            setBounds(0, 0, canvas.width, canvas.height)
            draw(canvas)
            BitmapPainter(bitmap.asImageBitmap())
        }
    }
}

@Composable
fun AppListItem(
    app: AppInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val iconPainter = remember(app.iconDrawable) {
            app.iconDrawable?.toPainter()
        }

        Icon(
            painter = iconPainter ?: painterResource(R.drawable.ic_brand),
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint =
                if (iconPainter == null )
                    MaterialTheme.colorScheme.onSurfaceVariant
                else
                    androidx.compose.ui.graphics.Color.Unspecified,
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

@Composable
fun AppList(
    onAppSelected: (AppInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val packageManager = context.packageManager
    var apps by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val installedApps = packageManager
                    .getInstalledPackages(0)
                    .filter { it.packageName != context.packageName }
                    .mapNotNull { pkg ->
                        try {
                            val appInfo: ApplicationInfo = pkg.applicationInfo ?: return@mapNotNull null
                            val appName = appInfo.loadLabel(packageManager).toString()
                            val icon = appInfo.loadIcon(packageManager)
                            AppInfo(pkg.packageName, appName, icon)
                        } catch (_: Exception) {
                            null
                        }
                    }
                    .sortedBy { it.appName.lowercase() }
                apps = installedApps
            } catch (e: Exception) {
                error = e.message ?: "Unknown error"
            } finally {
                isLoading = false
            }
        }
    }

    when {
        isLoading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        error != null -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                )
            }
        }
        apps.isEmpty() -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No applications found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        else -> {
            LazyColumn(modifier = modifier.fillMaxSize()) {
                items(apps, key = { it.packageName }) { app ->
                    AppListItem(
                        app = app,
                        onClick = { onAppSelected(app) },
                    )
                }
            }
        }
    }
}
