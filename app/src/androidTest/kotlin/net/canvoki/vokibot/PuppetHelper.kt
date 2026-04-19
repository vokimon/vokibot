package net.canvoki.vokibot

import android.content.pm.PackageManager
import androidx.test.platform.app.InstrumentationRegistry

object PuppetHelper {
    private const val PACKAGE_NAME = "net.canvoki.puppet"

    fun isInstalled(): Boolean {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        return try {
            context.packageManager.getPackageInfo(PACKAGE_NAME, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun requireInstalled() {
        if (!isInstalled()) {
            throw AssertionError(
                "Puppet app ($PACKAGE_NAME) is not installed. " +
                    "Run './gradlew :app:connectedDebugAndroidTest' to auto-install, " +
                    "or install manually via 'adb install puppet-debug.apk'.",
            )
        }
    }
}
