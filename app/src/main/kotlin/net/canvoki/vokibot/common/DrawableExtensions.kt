package net.canvoki.vokibot.common

import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.core.graphics.drawable.toBitmap

fun Drawable.toPainter(): BitmapPainter =
    BitmapPainter(
        toBitmap(
            width = intrinsicWidth.coerceAtLeast(48),
            height = intrinsicHeight.coerceAtLeast(48),
        ).asImageBitmap(),
    )

@Composable
fun Drawable.tintIfFlat(): Color =
    when (this) {
        is VectorDrawable, is BadgeDrawable -> MaterialTheme.colorScheme.primary
        else -> Color.Unspecified
    }
