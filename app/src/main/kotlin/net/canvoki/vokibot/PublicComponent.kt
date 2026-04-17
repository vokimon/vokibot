package net.canvoki.vokibot

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.core.content.pm.PackageInfoCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.canvoki.shared.log as sharedlog

enum class ComponentType(
    val displayName: String,
) {
    ACTIVITY("Activity"),
    SERVICE("Service"),
    RECEIVER("Receiver"),
    PROVIDER("Provider"),
}

fun log(message: String) {
    sharedlog("PublicComponentLogger: $message")
}

enum class ActionFilterType {
    SPECIFIC_ACTIONS,
    ANY_ACTION,
    UNKNOWN,
}

data class PublicComponent(
    val type: ComponentType,
    val name: String,
    val exported: Boolean,
    val label: String,
    val icon: Drawable? = null,
    val permissions: List<String> = emptyList(),
    val authorities: List<String> = emptyList(),
    val actions: List<String> = emptyList(),
    val actionFilterType: ActionFilterType = ActionFilterType.UNKNOWN,
    val dataSchemes: List<String> = emptyList(),
) {
    fun toLogString(): String =
        buildString {
            appendLine("[${type.displayName}] $name")
            appendLine("  Exported: $exported")
            if (label != name) appendLine("  Label: $label")
            if (permissions.isNotEmpty()) appendLine("  Permissions: ${permissions.joinToString(", ")}")
            if (authorities.isNotEmpty()) appendLine("  Authorities: ${authorities.joinToString(", ")}")

            when (actionFilterType) {
                ActionFilterType.SPECIFIC_ACTIONS -> appendLine("  Actions: ${actions.joinToString("\n    ")}")
                ActionFilterType.ANY_ACTION -> appendLine("  Actions: ANY")
                ActionFilterType.UNKNOWN -> appendLine("  Actions: NONE")
            }

            if (dataSchemes.isNotEmpty()) {
                appendLine("  Schemes: ${dataSchemes.joinToString(", ")}")
            }
            appendLine()
        }
}

data class PublicComponentsResult(
    val packageName: String,
    val appName: String,
    val versionName: String,
    val versionCode: Long,
    val components: List<PublicComponent>,
) {
    fun toLogString(exportedOnly: Boolean = false): String =
        buildString {
            val filtered = if (exportedOnly) components.filter { it.exported } else components
            appendLine("=== Public Components for $appName ===")
            appendLine("Package: $packageName")
            appendLine("Version: $versionName ($versionCode)")
            appendLine("Components found: ${filtered.size}")
            appendLine()
            filtered.forEach { append(it.toLogString()) }
            appendLine("=== Suggested <queries> for AndroidManifest.xml ===")
            appendLine("<queries>")
            appendLine("    <package android:name=\"$packageName\" />")
            appendLine("</queries>")
        }

    fun log(exportedOnly: Boolean = false) {
        toLogString(exportedOnly).lines().forEach { log(it) }
    }
}

/**
 * Queries publicly accessible component information from a target application.
 *
 * Returns structured data only. Call [PublicComponentsResult.log] or [PublicComponent.toLogString]
 * to generate output.
 *
 * @param context Android Context for accessing PackageManager
 * @param packageName The package name of the target application
 * @param exportedOnly If true, filter to only exported components (default: false)
 * @return Structured result with component data
 */
suspend fun queryPublicComponents(
    context: Context,
    packageName: String,
    exportedOnly: Boolean = false,
): PublicComponentsResult =
    withContext(Dispatchers.IO) {
        val packageManager = context.packageManager

        @Suppress("DEPRECATION")
        val flags =
            PackageManager.GET_ACTIVITIES or
                PackageManager.GET_SERVICES or
                PackageManager.GET_RECEIVERS or
                PackageManager.GET_PROVIDERS or
                PackageManager.GET_PERMISSIONS or
                PackageManager.GET_META_DATA

        val packageInfo = packageManager.getPackageInfo(packageName, flags)
        val appInfo =
            packageInfo.applicationInfo ?: throw PackageManager.NameNotFoundException("App not found: $packageName")

        val discoveryResult = discoverComponentData(packageManager, packageName)
        val components = mutableListOf<PublicComponent>()

        packageInfo.activities?.forEach { info ->
            val data = discoveryResult[info.name]
            val icon =
                try {
                    val componentName = ComponentName(packageName, info.name)

                    @Suppress("DEPRECATION")
                    val activityInfo = packageManager.getActivityInfo(componentName, 0)
                    activityInfo.loadIcon(packageManager)
                } catch (e: Exception) {
                    null
                }
            components.add(
                PublicComponent(
                    type = ComponentType.ACTIVITY,
                    name = info.name,
                    exported = info.exported,
                    label = info.loadLabel(packageManager).toString(),
                    icon = icon,
                    permissions = listOfNotNull(info.permission),
                    actions = data?.actions ?: emptyList(),
                    actionFilterType = data?.filterType ?: ActionFilterType.UNKNOWN,
                    dataSchemes = data?.dataSchemes ?: emptyList(),
                ),
            )
        }

        packageInfo.services?.forEach { info ->
            val data = discoveryResult[info.name]
            components.add(
                PublicComponent(
                    type = ComponentType.SERVICE,
                    name = info.name,
                    exported = info.exported,
                    label = info.loadLabel(packageManager).toString(),
                    permissions = listOfNotNull(info.permission),
                    actions = data?.actions ?: emptyList(),
                    actionFilterType = data?.filterType ?: ActionFilterType.UNKNOWN,
                    dataSchemes = data?.dataSchemes ?: emptyList(),
                ),
            )
        }

        packageInfo.receivers?.forEach { info ->
            val data = discoveryResult[info.name]
            components.add(
                PublicComponent(
                    type = ComponentType.RECEIVER,
                    name = info.name,
                    exported = info.exported,
                    label = info.loadLabel(packageManager).toString(),
                    permissions = listOfNotNull(info.permission),
                    actions = data?.actions ?: emptyList(),
                    actionFilterType = data?.filterType ?: ActionFilterType.UNKNOWN,
                    dataSchemes = data?.dataSchemes ?: emptyList(),
                ),
            )
        }

        packageInfo.providers?.forEach { info ->
            components.add(
                PublicComponent(
                    type = ComponentType.PROVIDER,
                    name = info.name,
                    exported = info.exported,
                    label = info.loadLabel(packageManager).toString(),
                    authorities = listOfNotNull(info.authority),
                    permissions = listOfNotNull(info.readPermission, info.writePermission),
                ),
            )
        }

        val versionCode =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                PackageInfoCompat.getLongVersionCode(packageInfo)
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }

        val sorted = components.sortedWith(compareBy({ it.type }, { !it.exported }, { it.name }))
        val filtered = if (exportedOnly) sorted.filter { it.exported } else sorted

        PublicComponentsResult(
            packageName = packageName,
            appName = appInfo.loadLabel(packageManager).toString(),
            versionName = packageInfo.versionName ?: "Unknown",
            versionCode = versionCode,
            components = filtered,
        )
    }

/**
 * Queries and logs publicly accessible component information.
 * Convenience wrapper that calls [queryPublicComponents] and logs the result.
 *
 * @param context Android Context for accessing PackageManager
 * @param packageName The package name of the target application
 * @param exportedOnly If true, log only exported components (default: true)
 */
suspend fun logPublicComponents(
    context: Context,
    packageName: String,
    exportedOnly: Boolean = true,
) {
    try {
        val result = queryPublicComponents(context, packageName, exportedOnly)
        result.log(exportedOnly = false)
    } catch (e: PackageManager.NameNotFoundException) {
        log("Error: Package '$packageName' not found")
    } catch (e: Exception) {
        log("Error querying components: ${e.message}")
    }
}

private data class ComponentDiscoveryData(
    val actions: List<String>,
    val dataSchemes: List<String>,
    val filterType: ActionFilterType,
)

private fun discoverComponentData(
    packageManager: PackageManager,
    packageName: String,
): Map<String, ComponentDiscoveryData> {
    val componentActions = mutableMapOf<String, MutableList<String>>()
    val componentSchemes = mutableMapOf<String, MutableList<String>>()
    val allStandardActions = StandardActions.all().map { it.action }

    for (standard in StandardActions.all()) {
        val intent =
            Intent(standard.action).apply {
                setPackage(packageName)
            }

        packageManager
            .queryIntentActivities(intent, PackageManager.MATCH_ALL)
            .forEach { resolveInfo ->
                val name = resolveInfo.activityInfo.name
                componentActions.getOrPut(name) { mutableListOf() }.add(standard.action)

                resolveInfo.filter?.let { filter ->
                    for (i in 0 until filter.countDataSchemes()) {
                        filter.getDataScheme(i)?.let { scheme ->
                            componentSchemes.getOrPut(name) { mutableListOf() }.add(scheme)
                        }
                    }
                }
            }

        packageManager
            .queryIntentServices(intent, PackageManager.MATCH_ALL)
            .forEach { resolveInfo ->
                val name = resolveInfo.serviceInfo.name
                componentActions.getOrPut(name) { mutableListOf() }.add(standard.action)

                resolveInfo.filter?.let { filter ->
                    for (i in 0 until filter.countDataSchemes()) {
                        filter.getDataScheme(i)?.let { scheme ->
                            componentSchemes.getOrPut(name) { mutableListOf() }.add(scheme)
                        }
                    }
                }
            }

        packageManager
            .queryBroadcastReceivers(intent, PackageManager.MATCH_ALL)
            .forEach { resolveInfo ->
                val name = resolveInfo.activityInfo.name
                componentActions.getOrPut(name) { mutableListOf() }.add(standard.action)

                resolveInfo.filter?.let { filter ->
                    for (i in 0 until filter.countDataSchemes()) {
                        filter.getDataScheme(i)?.let { scheme ->
                            componentSchemes.getOrPut(name) { mutableListOf() }.add(scheme)
                        }
                    }
                }
            }
    }

    return componentActions.mapValues { (name, actions) ->
        val schemes = componentSchemes[name]?.distinct() ?: emptyList()
        val filterType =
            when {
                actions.isEmpty() -> ActionFilterType.UNKNOWN
                actions.size == allStandardActions.size -> ActionFilterType.ANY_ACTION
                else -> ActionFilterType.SPECIFIC_ACTIONS
            }
        ComponentDiscoveryData(
            actions = actions.distinct(),
            dataSchemes = schemes,
            filterType = filterType,
        )
    }
}

private fun inferActionFilterType(
    acceptedActions: List<String>,
    allStandardActions: List<String>,
): ActionFilterType =
    when {
        acceptedActions.isEmpty() -> ActionFilterType.UNKNOWN
        acceptedActions.size == allStandardActions.size -> ActionFilterType.ANY_ACTION
        else -> ActionFilterType.SPECIFIC_ACTIONS
    }
