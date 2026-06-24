package net.canvoki.vokibot.common

import android.graphics.drawable.Drawable
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
