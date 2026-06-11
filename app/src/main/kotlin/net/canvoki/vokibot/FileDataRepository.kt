package net.canvoki.vokibot

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import java.io.File

class FileDataRepository(
    directoryPath: String = "repodata",
) {
    private val directory = File(directoryPath)

    init {
        directory.mkdirs()
    }

    companion object {
        const val DEFAULT_SUBDIRECTORY = "repodata"

        fun fromContext(
            context: Context,
            subdirectory: String = DEFAULT_SUBDIRECTORY,
        ): FileDataRepository {
            val dir = File(context.filesDir, subdirectory)
            return FileDataRepository(dir.absolutePath)
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Typed DataSets (single source of truth for file logic)
    // ─────────────────────────────────────────────────────────────

    val command: DataSet<Command> by lazy {
        DataSet(directory, "command_", Command::fromJson)
    }

    val trigger: DataSet<Trigger> by lazy {
        DataSet(directory, "trigger_", Trigger::fromJson)
    }

    val automation: DataSet<Automation> by lazy {
        DataSet(directory, "automation_", Automation::fromJson)
    }

    // ─────────────────────────────────────────────────────────────
    // Backward-compatible delegates (keep old tests passing)
    // ─────────────────────────────────────────────────────────────

    fun saveCommand(cmd: Command) = command.save(cmd)

    fun loadCommand(id: String) = command.load(id)

    fun removeCommand(id: String) = command.remove(id)

    fun existsCommand(id: String) = command.exists(id)

    fun listCommands() = command.listIds()

    fun loadAllCommands() = command.all()

    fun saveTrigger(trigger: Trigger) = this.trigger.save(trigger)

    fun loadNfcTrigger(uid: String) = trigger.load(NfcTrigger.idFromUid(uid)) as? NfcTrigger

    fun removeNfcTrigger(uid: String) = trigger.remove(NfcTrigger.idFromUid(uid))

    fun existsNfcTrigger(uid: String) = trigger.exists(NfcTrigger.idFromUid(uid))

    fun listNfcTriggers() =
        trigger.listIds().map { it ->
            it.removePrefix("nfc_").replace("_", ":")
        }

    fun loadAllNfcTriggers() = trigger.all()

    fun saveAutomation(auto: Automation) = automation.save(auto)

    fun loadAutomation(id: String) = automation.load(id)

    fun removeAutomation(id: String) = automation.remove(id)

    fun existsAutomation(id: String) = automation.exists(id)

    fun listAutomations() = automation.listIds()

    fun loadAllAutomations() = automation.all()

    fun exportBundle(): ExportedBundle = ExportedBundle(entities = trigger.all() + command.all() + automation.all())

    fun importBundle(bundle: ExportedBundle) {
        bundle.entities.forEach { entity ->
            when (entity) {
                is Trigger -> trigger.save(entity)
                is Command -> command.save(entity)
                is Automation -> automation.save(entity)
                else -> {}
            }
        }
    }

    fun entityIds(): Set<String> = command.listIds().toSet() + trigger.listIds().toSet()

    @Composable
    fun rememberDataVersion(): Int {
        var version by remember { mutableIntStateOf(0) }
        DisposableEffect(Unit) {
            val listener = { version += 1 }
            DataChangeBus.subscribe(listener)
            onDispose { DataChangeBus.unsubscribe(listener) }
        }
        return version
    }
}
