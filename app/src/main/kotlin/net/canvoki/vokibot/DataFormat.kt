package net.canvoki.vokibot

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import androidx.preference.PreferenceManager

/**
 * Compares on init the current version with the last saved version
 * and (todo) launches migration for the repository if they missmatch.
 */
object DataFormat {
    const val CURRENT_VERSION = 1
    private const val KEY = "data_format_version"

    fun initialize(context: Context) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val stored = prefs.getInt(KEY, -1)

        when {
            stored == -1 ->
                prefs.edit { putInt(KEY, CURRENT_VERSION) }
            stored < CURRENT_VERSION -> {
                // TODO: Migrate repository data
                Log.w("DataFormat", "Repo format $stored → $CURRENT_VERSION")
                prefs.edit { putInt(KEY, CURRENT_VERSION) }
            }
        }
    }
}
