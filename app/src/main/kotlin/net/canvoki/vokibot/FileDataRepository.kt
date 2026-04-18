package net.canvoki.vokibot

import android.content.Context
import java.io.File

class FileDataRepository(directoryPath: String = "repodata") {
    private val _directory = File(directoryPath)

    init {
        _directory.mkdirs()
    }

    companion object {
        const val DEFAULT_SUBDIRECTORY = "repodata"

        fun fromContext(context: Context, subdirectory: String = DEFAULT_SUBDIRECTORY): FileDataRepository {
            val dir = File(context.filesDir, subdirectory)
            return FileDataRepository(dir.absolutePath)
        }
    }

    val command: DataSet<ApplicationCommand> by lazy {
        DataSet(_directory, "command_", ApplicationCommand::fromJson)
    }

    val nfcTrigger: DataSet<NfcTrigger> by lazy {
        DataSet(_directory, "trigger_nfc_", NfcTrigger::fromJson)
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

    fun saveNfcTrigger(trigger: NfcTrigger) = nfcTrigger.save(trigger)
    fun loadNfcTrigger(id: String) = nfcTrigger.load(id)
    fun removeNfcTrigger(id: String) = nfcTrigger.remove(id)
    fun existsNfcTrigger(id: String) = nfcTrigger.exists(id)
    fun listNfcTriggers() = nfcTrigger.listIds()
    fun loadAllNfcTriggers() = nfcTrigger.all()
}
