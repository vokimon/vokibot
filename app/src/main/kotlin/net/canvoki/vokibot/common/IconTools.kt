package net.canvoki.vokibot.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable

fun buildBadgeIcon(
    context: Context,
    main: Drawable,
    badge: Drawable,
    mainScale: Float = 0.8f,
    badgeScale: Float = 0.4f,
    sizeDp: Int = 40,
): Drawable {
    val d = context.resources.displayMetrics.density
    val canvasSize = (sizeDp * d).toInt()

    val mainSize = (canvasSize * mainScale).toInt()
    val badgeSize = (canvasSize * badgeScale).toInt()

    val bitmap =
        Bitmap.createBitmap(
            canvasSize,
            canvasSize,
            Bitmap.Config.ARGB_8888,
        )

    val canvas = Canvas(bitmap)

    // Main icon anchored top-left.
    main.mutate()
    main.setBounds(
        0,
        0,
        mainSize,
        mainSize,
    )
    main.draw(canvas)

    // Badge anchored bottom-right.
    val badgeLeft = canvasSize - badgeSize
    val badgeTop = canvasSize - badgeSize

    // Remove the portion of the main icon that would be covered.
    canvas.drawRect(
        badgeLeft.toFloat(),
        badgeTop.toFloat(),
        canvasSize.toFloat(),
        canvasSize.toFloat(),
        Paint().apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        },
    )

    badge.mutate()
    badge.setBounds(
        badgeLeft,
        badgeTop,
        canvasSize,
        canvasSize,
    )
    badge.draw(canvas)

    return BitmapDrawable(context.resources, bitmap)
}
