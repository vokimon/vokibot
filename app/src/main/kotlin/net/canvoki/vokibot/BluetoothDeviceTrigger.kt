package net.canvoki.vokibot

import android.view.Gravity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.BitmapDrawable
import kotlinx.serialization.Serializable

@Serializable
data class BluetoothDeviceTrigger(
    val name: String,
    val macAddress: String,
) : Trigger() {
    companion object : EntityMetadata {
        const val ID_PREFIX = "bluetooth_device_"

        override val typeKey = "trigger_bluetooth_device"
        override val entityClass = BluetoothDeviceTrigger::class
        override val labelRes = R.string.triggerlist_option_bluetooth_device
        override val iconRes = R.drawable.ic_bluetooth
        override val editorFactory =
            { triggerId: String? -> BluetoothDeviceTriggerEditor(triggerId) }
        override val deserializer = { jsonString: String -> fromJson(jsonString) }
        override val helpRes = R.string.trigger_bluetooth_device_help

        fun idFromMac(mac: String) = "$ID_PREFIX${toFileSystemId(mac)}"

        fun register() = StorableEntity.register(this)

        fun fromJson(jsonString: String): BluetoothDeviceTrigger = JsonConfig.decodeFromString(serializer(), jsonString)
    }

    override val type: String = BluetoothDeviceTrigger.typeKey
    override val iconRes: Int get() = bluetoothDeviceIcon(bluetoothDeviceFromMac(macAddress))
    override val id: String get() = idFromMac(macAddress)

    override fun getTitle(context: Context): String {
        val device = bluetoothDeviceFromMac(macAddress)
        val label = device?.let { bluetoothDeviceLabelRes(it) }?.let { context.getString(it) }
        return if (label != null) "$name ($label)" else name
    }

    override val description: String get() = macAddress

    override fun loadIcon(context: Context): Drawable {
        val device = bluetoothDeviceFromMac(macAddress)
        val deviceIcon = bluetoothDeviceIcon(device)

        if (deviceIcon == BluetoothDeviceTrigger.iconRes) {
            return context.getDrawable(deviceIcon)!!
        }

        return buildBadgeIcon(
            context = context,
            main = context.getDrawable(deviceIcon)!!,
            badge = context.getDrawable(BluetoothDeviceTrigger.iconRes)!!,
            mainScale = 0.8f,
            badgeScale = 0.4f
        )
    }

    override fun toJson(): String = JsonConfig.encodeToString(serializer(), this)
}

fun buildBadgeIcon(
    context: Context,
    main: Drawable,
    badge: Drawable,
    mainScale: Float = 0.9f,
    badgeScale: Float = 0.6f,
    sizeDp: Int = 24
): Drawable {
    val d = context.resources.displayMetrics.density
    val canvasSize = (sizeDp * d).toInt()

    val mainSize = (canvasSize * mainScale).toInt()
    val badgeSize = (canvasSize * badgeScale).toInt()

    val bitmap = Bitmap.createBitmap(
        canvasSize,
        canvasSize,
        Bitmap.Config.ARGB_8888
    )

    val canvas = Canvas(bitmap)

    // Main icon anchored top-left.
    main.mutate()
    main.setBounds(
        0,
        0,
        mainSize,
        mainSize
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
        }
    )

    badge.mutate()
    badge.setBounds(
        badgeLeft,
        badgeTop,
        canvasSize,
        canvasSize
    )
    badge.draw(canvas)

    return BitmapDrawable(context.resources, bitmap)
}
