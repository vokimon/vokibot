package net.canvoki.vokibot.common

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.Drawable

class BadgeDrawable(
    private val main: Drawable,
    private val badge: Drawable,
    private val mainScale: Float = 0.8f,
    private val badgeScale: Float = 0.4f,
    private val sizeDp: Int = 40,
) : Drawable() {
    private val density =
        android.content.res.Resources
            .getSystem()
            .displayMetrics.density

    override fun getIntrinsicWidth(): Int = (sizeDp * density).toInt()

    override fun getIntrinsicHeight(): Int = (sizeDp * density).toInt()

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = android.graphics.PixelFormat.TRANSLUCENT

    override fun draw(canvas: Canvas) {
        val canvasSize = bounds.width()
        if (canvasSize == 0) return

        val mainSize = (canvasSize * mainScale).toInt()
        val badgeSize = (canvasSize * badgeScale).toInt()

        // Main icon anchored top-left.
        main.mutate()
        main.setBounds(0, 0, mainSize, mainSize)
        main.draw(canvas)

        // Badge anchored bottom-right.
        val badgeLeft = canvasSize - badgeSize
        val badgeTop = canvasSize - badgeSize

        // Clear the region under the badge.
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
        badge.setBounds(badgeLeft, badgeTop, canvasSize, canvasSize)
        badge.draw(canvas)
    }

    override fun setAlpha(alpha: Int) {}

    override fun setColorFilter(colorFilter: android.graphics.ColorFilter?) {}
}
