package net.canvoki.vokibot.common

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import kotlinx.coroutines.delay
import net.canvoki.vokibot.AdbPermissionsGuideActivity
import net.canvoki.vokibot.R

interface PermissionState {
    val isGranted: Boolean

    fun request()
}

private val alwaysGranted =
    object : PermissionState {
        override val isGranted: Boolean get() = true

        override fun request() {}
    }

@Composable
fun rememberPermissionState(permission: String?): PermissionState {
    if (permission == null) return alwaysGranted

    when (permission) {
        Manifest.permission.WRITE_SETTINGS ->
            return rememberWriteSettingsPermissionState()
        Manifest.permission.WRITE_SECURE_SETTINGS ->
            return rememberWriteSecureSettingsPermissionState()
    }

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
private fun rememberWriteSettingsPermissionState(): PermissionState {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var isGranted by remember {
        mutableStateOf(Settings.System.canWrite(context))
    }

    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    isGranted = Settings.System.canWrite(context)
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    return object : PermissionState {
        override val isGranted: Boolean get() = isGranted

        override fun request() {
            context.startActivity(
                Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                },
            )
        }
    }
}

@Composable
private fun rememberWriteSecureSettingsPermissionState(): PermissionState {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    fun canWrite() =
        ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.WRITE_SECURE_SETTINGS,
        ) == PackageManager.PERMISSION_GRANTED

    var isGranted by remember { mutableStateOf(canWrite()) }
    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    isGranted = canWrite()
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val intervalMillis = 3_000L
    LaunchedEffect(Unit) {
        while (true) {
            delay(intervalMillis)
            isGranted = canWrite()
        }
    }

    return object : PermissionState {
        override val isGranted: Boolean get() = isGranted

        override fun request() {
            val intent = Intent(context, AdbPermissionsGuideActivity::class.java)
            context.startActivity(intent)
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
            buttonText = stringResource(R.string.missing_permission_banner_button_grant_permission),
            onClick = { state.request() },
        )
    }
}
