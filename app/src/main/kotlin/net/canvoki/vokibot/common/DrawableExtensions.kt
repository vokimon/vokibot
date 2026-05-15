package net.canvoki.vokibot.common

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter

fun Drawable.toPainter(): BitmapPainter =
    when (this) {
        is BitmapDrawable -> BitmapPainter(bitmap.asImageBitmap())
        else -> {
            val bitmap =
                Bitmap.createBitmap(
                    intrinsicWidth.coerceAtLeast(48),
                    intrinsicHeight.coerceAtLeast(48),
                    Bitmap.Config.ARGB_8888,
                )
            val canvas = android.graphics.Canvas(bitmap)
            setBounds(0, 0, canvas.width, canvas.height)
            draw(canvas)
            BitmapPainter(bitmap.asImageBitmap())
        }
    }
