package net.canvoki.vokibot

object FeatureFlag {
    val enableDirectActivitySelection: Boolean
        get() = BuildConfig.DEBUG
}
