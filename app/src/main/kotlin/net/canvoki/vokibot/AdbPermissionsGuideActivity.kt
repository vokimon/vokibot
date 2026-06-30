package net.canvoki.vokibot

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import kotlinx.coroutines.delay
import net.canvoki.shared.component.AppScaffold
import net.canvoki.shared.component.StackNavigator
import net.canvoki.shared.component.WatermarkBox
import net.canvoki.shared.log
import net.canvoki.vokibot.common.rememberPermissionState
import java.io.BufferedReader
import java.io.InputStreamReader

class AdbPermissionsGuideActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppScaffold(drawer = { Drawer() }) {
                WatermarkBox(
                    watermark = painterResource(R.drawable.ic_brand),
                ) {
                    AdbPermissionsGuide(
                        permission = android.Manifest.permission.WRITE_SECURE_SETTINGS,
                        title = stringResource(R.string.adb_permissions_title),
                    )
                }
            }
        }
    }
}

@Composable
fun <T> rememberPollingState(
    intervalMillis: Long = 1_000,
    producer: () -> T,
): State<T> {
    val state =
        remember {
            mutableStateOf(producer())
        }

    LaunchedEffect(intervalMillis) {
        while (true) {
            delay(intervalMillis)
            state.value = producer()
        }
    }

    return state
}

const val EXTRA_FRAGMENT_ARG_KEY = ":settings:fragment_args_key"
const val EXTRA_SHOW_FRAGMENT_ARGUMENTS = ":settings:show_fragment_args"
const val EXTRA_ENABLE_ADB = "enable_adb" // UNDOCUMENTED API
const val EXTRA_BUILD_NUMBER = "build_number" // UNDOCUMENTED API

fun jumpToParam(
    context: Context,
    page: String,
    param: String,
) {
    val intent =
        Intent(page).apply {
            putExtra(EXTRA_FRAGMENT_ARG_KEY, param)
            putExtra(
                EXTRA_SHOW_FRAGMENT_ARGUMENTS,
                Bundle().apply {
                    putString(EXTRA_FRAGMENT_ARG_KEY, param)
                },
            )
        }
    context.startActivity(intent)
}

@Composable
fun AdbPermissionsGuide(
    title: String,
    permission: String,
) {
    val context = LocalContext.current
    var developerOptionsEnabled = rememberPollingState { isDeveloperOptionsEnabled(context) }
    var usbDebugEnabled = rememberPollingState { isUsbDebuggingEnabled(context) }
    val permState = rememberPermissionState(permission)

    Column {
        Text(
            title,
            color = MaterialTheme.colorScheme.tertiary,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier =
                Modifier
                    .weight(1f)
                    .padding(vertical = 4.dp)
                    .verticalScroll(rememberScrollState()),
        ) {
            Text(stringResource(R.string.adb_permissions_warning))
            Text(stringResource(R.string.adb_permissions_click_for_help))

            // https://developer.android.com/studio/debug/dev-options
            Step(
                title = stringResource(R.string.adb_permissions_developer_options_title),
                description = stringResource(R.string.adb_permissions_developer_options_description),
                done = developerOptionsEnabled.value,
                actionText = stringResource(R.string.adb_permissions_developer_options_button_device_info),
                action = {
                    jumpToParam(
                        context,
                        page = Settings.ACTION_DEVICE_INFO_SETTINGS,
                        param = EXTRA_BUILD_NUMBER,
                    )
                },
            )

            // https://developer.android.com/tools/adb#Enabling
            Step(
                title = stringResource(R.string.adb_permissions_usb_debugging_title),
                description = stringResource(R.string.adb_permissions_usb_debugging_description),
                done = usbDebugEnabled.value,
                actionText = stringResource(R.string.adb_permissions_usb_debugging_button_developer_options),
                action = {
                    jumpToParam(
                        context,
                        page = Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS,
                        param = EXTRA_ENABLE_ADB,
                    )
                },
            )

            Step(
                title = stringResource(R.string.adb_permissions_install_adb_title),
                description = stringResource(R.string.adb_permissions_install_adb_description),
                done = null,
                actionText = stringResource(R.string.adb_permissions_install_adb_button),
                action = {
                    val url = "https://developer.android.com/tools/releases/platform-tools#downloads"
                    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                    context.startActivity(intent)
                },
            )

            Step(
                title = stringResource(R.string.adb_permissions_connect_usb_title),
                description = stringResource(R.string.adb_permissions_connect_usb_description),
                done = null,
            )

            val adbCommand = "adb shell pm grant ${context.packageName} $permission"
            Step(
                title = stringResource(R.string.adb_permissions_run_adb_title),
                done = permState.isGranted,
                actionText = stringResource(R.string.adb_permissions_run_adb_copy_button),
                action = {
                    copyToClipboard(context, adbCommand)
                },
            ) {
                Surface(
                    tonalElevation = 2.dp,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = adbCommand,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(4.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun Step(
    done: Boolean?,
    title: String,
    description: String? = null,
    actionText: String? = null,
    action: () -> Unit = {},
    expandable: @Composable ColumnScope.() -> Unit = {},
) {
    val layoutDirection = LocalLayoutDirection.current

    var expanded by remember { mutableStateOf(false) }
    val icon =
        done?.let { if (done) "✅ " else "❌ " }
            ?: if (layoutDirection == LayoutDirection.Ltr) "👉 " else "👈 "
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
    ) {
        Column(
            modifier =
                Modifier
                    .padding(start = 4.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.Start,
                modifier =
                    Modifier
                        .padding(vertical = 6.dp, horizontal = 8.dp),
            ) {
                Text(icon)
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier =
                        Modifier
                            .padding(start = 4.dp),
                ) {
                    description?.let {
                        Text(description)
                    }
                    expandable()
                    actionText?.let {
                        TextButton(
                            modifier = Modifier.align(Alignment.End),
                            onClick = { action() },
                        ) {
                            Text(it)
                        }
                    }
                }
            }
        }
    }
}

fun copyToClipboard(
    context: Context,
    text: String,
) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    val clip = ClipData.newPlainText("ADB command", text)

    clipboard.setPrimaryClip(clip)
}

fun isDeveloperOptionsEnabled(context: Context): Boolean =
    Settings.Global.getInt(
        context.contentResolver,
        Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
        0,
    ) == 1

fun isUsbDebuggingEnabled(context: Context): Boolean =
    Settings.Global.getInt(
        context.contentResolver,
        Settings.Global.ADB_ENABLED,
        0,
    ) == 1

// TODO: The next functions are diferent unsuccessfull attemps to detect a connected adb host

fun isAdbSessionReady(context: Context): Boolean {
    val adbEnabled =
        Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.ADB_ENABLED,
            0,
        ) == 1

    if (!adbEnabled) return false

    val usbState = context.registerReceiver(null, IntentFilter("Intent.ACTION_USB_STATE"))
    val connected = usbState?.getBooleanExtra("connected", false) ?: false
    val configured = usbState?.getBooleanExtra("configured", false) ?: false

    return connected && configured
}

fun getUsbAdbState(): String {
    val process = Runtime.getRuntime().exec("getprop sys.usb.state")
    val reader = BufferedReader(InputStreamReader(process.inputStream))
    val result = reader.use { it.readLine()?.trim().orEmpty() }
    val exitCode = process.waitFor()
    require(exitCode == 0) {
        "getprop sys.usb.state failed (exitCode=$exitCode)"
    }
    return result
}

fun isUsbInAdbMode(): Boolean = getUsbAdbState().contains("adb")

fun isAdbLikelyActive(): Boolean? =
    try {
        val state =
            java.io
                .File("/sys/class/android_usb/android0/state")
                .readText()
                .trim()
        val functions =
            java.io
                .File("/sys/class/android_usb/android0/functions")
                .readText()
                .trim()
        state == "CONNECTED" && functions.contains("adb")
    } catch (e: Exception) {
        log("isAdbLikelyActive Exception: $e")
        null
    }
