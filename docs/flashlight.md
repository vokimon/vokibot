# Analysis flashlight command

## Permissions and Manifest

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-feature android:name="android.hardware.camera.flash" android:required="false"/>
```

`CAMERA` permission requires runtime user grant.

## Camera selection/enumeration

Look for the first camera with flash and looking back:

```kotlin
val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
    val characteristics = cameraManager.getCameraCharacteristics(id)

    val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
    val facing = characteristics.get(CameraCharacteristics.LENS_FACING)

    hasFlash && facing == CameraCharacteristics.LENS_FACING_BACK
}
```

We could also give the option to choose which one.

```
CameraCharacteristics.LENS_FACING_FRONT
CameraCharacteristics.LENS_FACING_BACK
CameraCharacteristics.LENS_FACING_EXTERNAL
```

## Set TorchMode on/off

```kotlin
try {
    cameraManager.setTorchMode(cameraId, onoff)
} catch (e: CameraAccessException) {
    // manage
}
```

## Toggle

There is not synchronous way to obtain the current torch mode.
Strategy: asynchronously wait a timeout for a value and fallback to the last recorded state the app knows.
This way if the first toggle does not work, a second one will.

```kotlin
suspend fun toggleTorch(cameraId: String) {
    val manager = context.getSystemService(CameraManager::class.java)

    val detected = CompletableDeferred<Boolean>()

    val callback = object : CameraManager.TorchCallback() {
        override fun onTorchModeChanged(id: String, enabled: Boolean) {
            if (id == cameraId && !detected.isCompleted) {
                detected.complete(enabled)
            }
        }
    }

    manager.registerTorchCallback(callback, Handler(Looper.getMainLooper()))

    try {
        val current = withTimeoutOrNull(200) {
            detected.await()
        } ?: prefs.lastTorchState // pseudocode

        val next = !current
        try {
            manager.setTorchMode(cameraId, next)
            prefs.lastTorchState = next // pseudocode
        } catch (e: CameraAccessException) {
            notifyUser(e)
        }
    } finally {
        manager.unregisterTorchCallback(callback)
    }
}
```

## Torch strength control

https://source.android.com/docs/core/camera/torch-strength-control

Only available for API 33+ (Android 13+)

- Getter: `public int getTorchStrengthLevel (String cameraId)`
- Setter: `public void turnOnTorchWithStrengthLevel (String cameraId, int torchStrength)

With value 0, is equivalent to off.
With value different than zero the torch turns on with the level.
The maximum and default value can be queried as CameraCharacteristics

- CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL: Maximum brightness level. The camera HAL advertises this feature by setting a value greater than 1.
- CameraCharacteristics.FLASH_INFO_STRENGTH_DEFAULT_LEVEL: Default flashlight brightness level.


