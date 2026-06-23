package net.canvoki.vokibot.common

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import net.canvoki.vokibot.R

interface PermissionState {
    val isGranted: Boolean

    @get:StringRes val actionLabelRes: Int

    fun request()
}

private val alwaysGranted =
    object : PermissionState {
        override val isGranted: Boolean get() = true
        override val actionLabelRes: Int = 0

        override fun request() {}
    }

@Composable
fun rememberPermissionState(permission: String?): PermissionState {
    if (permission == null) return alwaysGranted

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var isGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, permission) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    var deniedOnce by remember { mutableStateOf(false) }

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            isGranted = granted
            if (granted) deniedOnce = false else deniedOnce = true
        }

    val appSettingsIntent =
        remember {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
        }

    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    isGranted =
                        ContextCompat.checkSelfPermission(context, permission) ==
                        PackageManager.PERMISSION_GRANTED
                    if (isGranted) deniedOnce = false
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    return object : PermissionState {
        override val isGranted: Boolean get() = isGranted
        override val actionLabelRes: Int = R.string.bluetooth_device_editor_grant_permission

        override fun request() {
            if (deniedOnce) {
                context.startActivity(appSettingsIntent)
            } else {
                launcher.launch(permission)
            }
        }
    }
}

@Composable
fun MissingPermissionBanner(
    state: PermissionState,
    message: String,
) {
    if (!state.isGranted) {
        WarningBanner(
            message = message,
            buttonText = stringResource(state.actionLabelRes),
            onClick = { state.request() },
        )
    }
}
