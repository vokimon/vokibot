package net.canvoki.vokibot

import android.app.Application
import net.canvoki.shared.crash.CopyCrashBackend
import net.canvoki.shared.crash.CrashReporter
import net.canvoki.shared.crash.CrashReporterConfig
import net.canvoki.shared.crash.GitHubCrashBackend
import net.canvoki.shared.settings.LanguageSettings
import net.canvoki.shared.settings.ThemeSettings

class VokiBotApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CrashReporter.initialize(
            application = this,
            config =
                CrashReporterConfig(
                    appName = getString(R.string.app_name),
                    appVersion = BuildConfig.VERSION_NAME,
                    crashFileName = "crash_report.txt",
                    backends =
                        listOf(
                            GitHubCrashBackend("https://github.com/vokimon/vokibot"),
                            CopyCrashBackend(),
                        ),
                ),
        )
        LanguageSettings.initialize(this)
        ThemeSettings.initialize(this)
    }
}
