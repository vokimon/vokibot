package net.canvoki.vokibot

object DataChangeBus {
    private val listeners = mutableListOf<() -> Unit>()

    fun emit() {
        listeners.map { it.invoke() }
    }

    fun subscribe(listener: () -> Unit) {
        listeners.add(listener)
    }

    fun unsubscribe(listener: () -> Unit) {
        listeners.remove(listener)
    }

    fun reset() {
        listeners.clear()
    }
}
