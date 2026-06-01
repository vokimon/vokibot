package net.canvoki.vokibot

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import net.canvoki.shared.log

private fun BluetoothDevice?.bluetoothClassSafe(): BluetoothClass? = try {
    this?.bluetoothClass
} catch (e: SecurityException) {
    log("Bluetooth permission denied")
    null
}

fun bluetoothDeviceIcon(device: BluetoothDevice?): Int =
    when (device?.bluetoothClassSafe()?.majorDeviceClass) {
        BluetoothClass.Device.Major.AUDIO_VIDEO -> R.drawable.ic_headphones
        BluetoothClass.Device.Major.COMPUTER -> R.drawable.ic_computer
        BluetoothClass.Device.Major.HEALTH -> R.drawable.ic_medical_services
        BluetoothClass.Device.Major.IMAGING -> R.drawable.ic_photo_camera
        BluetoothClass.Device.Major.NETWORKING -> R.drawable.ic_router
        BluetoothClass.Device.Major.PERIPHERAL -> R.drawable.ic_keyboard
        BluetoothClass.Device.Major.PHONE -> R.drawable.ic_phone
        BluetoothClass.Device.Major.TOY -> R.drawable.ic_toys
        BluetoothClass.Device.Major.WEARABLE -> R.drawable.ic_watch
        else -> R.drawable.ic_bluetooth
    }

fun bluetoothDeviceLabelRes(device: BluetoothDevice?): Int? {
    val btClass = device?.bluetoothClassSafe() ?: return null
    // https://developer.android.com/reference/android/bluetooth/BluetoothClass.Device
    return when (btClass.deviceClass) {
        BluetoothClass.Device.COMPUTER_DESKTOP ->
            R.string.bluetooth_device_class_computer_desktop
        BluetoothClass.Device.COMPUTER_SERVER ->
            R.string.bluetooth_device_class_computer_server
        BluetoothClass.Device.COMPUTER_LAPTOP ->
            R.string.bluetooth_device_class_computer_laptop
        BluetoothClass.Device.COMPUTER_HANDHELD_PC_PDA ->
            R.string.bluetooth_device_class_computer_handheld_pc_pda
        BluetoothClass.Device.COMPUTER_PALM_SIZE_PC_PDA ->
            R.string.bluetooth_device_class_computer_palm_size_pc_pda
        BluetoothClass.Device.COMPUTER_WEARABLE ->
            R.string.bluetooth_device_class_computer_wearable
        BluetoothClass.Device.COMPUTER_UNCATEGORIZED ->
            R.string.bluetooth_device_class_computer_uncategorized

        BluetoothClass.Device.PHONE_CELLULAR ->
            R.string.bluetooth_device_class_phone_cellular
        BluetoothClass.Device.PHONE_CORDLESS ->
            R.string.bluetooth_device_class_phone_cordless
        BluetoothClass.Device.PHONE_SMART ->
            R.string.bluetooth_device_class_phone_smart
        BluetoothClass.Device.PHONE_MODEM_OR_GATEWAY ->
            R.string.bluetooth_device_class_phone_modem_or_gateway
        BluetoothClass.Device.PHONE_ISDN ->
            R.string.bluetooth_device_class_phone_isdn
        BluetoothClass.Device.PHONE_UNCATEGORIZED ->
            R.string.bluetooth_device_class_phone_uncategorized

        BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET ->
            R.string.bluetooth_device_class_audio_video_wearable_headset
        BluetoothClass.Device.AUDIO_VIDEO_UNCATEGORIZED ->
            R.string.bluetooth_device_class_audio_video_uncategorized
        BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE ->
            R.string.bluetooth_device_class_audio_video_handsfree
        BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES ->
            R.string.bluetooth_device_class_audio_video_headphones
        BluetoothClass.Device.AUDIO_VIDEO_MICROPHONE ->
            R.string.bluetooth_device_class_audio_video_microphone
        BluetoothClass.Device.AUDIO_VIDEO_LOUDSPEAKER ->
            R.string.bluetooth_device_class_audio_video_loudspeaker
        BluetoothClass.Device.AUDIO_VIDEO_PORTABLE_AUDIO ->
            R.string.bluetooth_device_class_audio_video_portable_audio
        BluetoothClass.Device.AUDIO_VIDEO_CAR_AUDIO ->
            R.string.bluetooth_device_class_audio_video_car_audio
        BluetoothClass.Device.AUDIO_VIDEO_SET_TOP_BOX ->
            R.string.bluetooth_device_class_audio_video_set_top_box
        BluetoothClass.Device.AUDIO_VIDEO_HIFI_AUDIO ->
            R.string.bluetooth_device_class_audio_video_hifi_audio
        BluetoothClass.Device.AUDIO_VIDEO_VCR ->
            R.string.bluetooth_device_class_audio_video_vcr
        BluetoothClass.Device.AUDIO_VIDEO_VIDEO_CAMERA ->
            R.string.bluetooth_device_class_audio_video_video_camera
        BluetoothClass.Device.AUDIO_VIDEO_CAMCORDER ->
            R.string.bluetooth_device_class_audio_video_camcorder
        BluetoothClass.Device.AUDIO_VIDEO_VIDEO_MONITOR ->
            R.string.bluetooth_device_class_audio_video_video_monitor
        BluetoothClass.Device.AUDIO_VIDEO_VIDEO_DISPLAY_AND_LOUDSPEAKER ->
            R.string.bluetooth_device_class_audio_video_video_display_and_loudspeaker
        BluetoothClass.Device.AUDIO_VIDEO_VIDEO_CONFERENCING ->
            R.string.bluetooth_device_class_audio_video_video_conferencing
        BluetoothClass.Device.AUDIO_VIDEO_VIDEO_GAMING_TOY ->
            R.string.bluetooth_device_class_audio_video_video_gaming_toy

        BluetoothClass.Device.PERIPHERAL_KEYBOARD ->
            R.string.bluetooth_device_class_peripheral_keyboard
        BluetoothClass.Device.PERIPHERAL_POINTING ->
            R.string.bluetooth_device_class_peripheral_pointing
        BluetoothClass.Device.PERIPHERAL_KEYBOARD_POINTING ->
            R.string.bluetooth_device_class_peripheral_keyboard_pointing
        BluetoothClass.Device.PERIPHERAL_NON_KEYBOARD_NON_POINTING ->
            R.string.bluetooth_device_class_peripheral_non_keyboard_non_pointing

        BluetoothClass.Device.WEARABLE_WRIST_WATCH ->
            R.string.bluetooth_device_class_wearable_wrist_watch
        BluetoothClass.Device.WEARABLE_PAGER ->
            R.string.bluetooth_device_class_wearable_pager
        BluetoothClass.Device.WEARABLE_JACKET ->
            R.string.bluetooth_device_class_wearable_jacket
        BluetoothClass.Device.WEARABLE_HELMET ->
            R.string.bluetooth_device_class_wearable_helmet
        BluetoothClass.Device.WEARABLE_GLASSES ->
            R.string.bluetooth_device_class_wearable_glasses
        BluetoothClass.Device.WEARABLE_UNCATEGORIZED ->
            R.string.bluetooth_device_class_wearable_uncategorized

        BluetoothClass.Device.TOY_ROBOT ->
            R.string.bluetooth_device_class_toy_robot
        BluetoothClass.Device.TOY_VEHICLE ->
            R.string.bluetooth_device_class_toy_vehicle
        BluetoothClass.Device.TOY_DOLL_ACTION_FIGURE ->
            R.string.bluetooth_device_class_toy_doll_action_figure
        BluetoothClass.Device.TOY_CONTROLLER ->
            R.string.bluetooth_device_class_toy_controller
        BluetoothClass.Device.TOY_GAME ->
            R.string.bluetooth_device_class_toy_game
        BluetoothClass.Device.TOY_UNCATEGORIZED ->
            R.string.bluetooth_device_class_toy_uncategorized

        BluetoothClass.Device.HEALTH_BLOOD_PRESSURE ->
            R.string.bluetooth_device_class_health_blood_pressure
        BluetoothClass.Device.HEALTH_THERMOMETER ->
            R.string.bluetooth_device_class_health_thermometer
        BluetoothClass.Device.HEALTH_WEIGHING ->
            R.string.bluetooth_device_class_health_weighing
        BluetoothClass.Device.HEALTH_GLUCOSE ->
            R.string.bluetooth_device_class_health_glucose
        BluetoothClass.Device.HEALTH_PULSE_OXIMETER ->
            R.string.bluetooth_device_class_health_pulse_oximeter
        BluetoothClass.Device.HEALTH_PULSE_RATE ->
            R.string.bluetooth_device_class_health_pulse_rate
        BluetoothClass.Device.HEALTH_DATA_DISPLAY ->
            R.string.bluetooth_device_class_health_data_display
        BluetoothClass.Device.HEALTH_UNCATEGORIZED ->
            R.string.bluetooth_device_class_health_uncategorized

        else -> R.string.bluetooth_device_class_unknown
    }
}

fun bluetoothDeviceFromMac(macAddress: String): BluetoothDevice? {
    val adapter = BluetoothAdapter.getDefaultAdapter() ?: return null
    return try {
        adapter.getRemoteDevice(macAddress)
    } catch (e: IllegalArgumentException) {
        log("bluetoothDeviceFromMac: invalid MAC")
        null
    } catch (e: SecurityException) {
        log("bluetoothDeviceFromMac: permission denied")
        null
    }
}
