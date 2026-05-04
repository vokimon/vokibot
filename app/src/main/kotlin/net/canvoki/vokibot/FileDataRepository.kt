package net.canvoki.vokibot

import android.content.Context
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

    val command: DataSet<ApplicationCommand> by lazy {
        DataSet(directory, "command_", ApplicationCommand::fromJson)
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

    fun saveCommand(cmd: ApplicationCommand) = command.save(cmd)

    fun loadCommand(id: String) = command.load(id)

    fun removeCommand(id: String) = command.remove(id)

    fun existsCommand(id: String) = command.exists(id)

    fun listCommands() = command.listIds()

    fun loadAllCommands() = command.all()

    fun saveNfcTrigger(trigger: NfcTrigger) = this.trigger.save(trigger)

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
}
