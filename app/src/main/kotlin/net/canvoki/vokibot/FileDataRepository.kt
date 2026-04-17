package net.canvoki.vokibot

import android.content.Context
import java.io.File

class FileDataRepository(directoryPath: String = "repodata") {
    typealias Command = ApplicationCommand
    private val _directory = File(directoryPath)

    init {
        _directory.mkdirs()
    }

    companion object {
        const val DEFAULT_SUBDIRECTORY = "repodata"

        fun fromContext(context: Context, subdirectory: String = DEFAULT_SUBDIRECTORY): FileDataRepository {
            val dir = File(context.filesDir, subdirectory)
            return FileDataRepository(dir.getAbsolutePath())
        }
    }

    fun _commandFile(id: String): File {
        return File(_directory, "command_$id.json")
    }

    fun saveCommand(id: String, command: Command) {
        val file = _commandFile(id)
        file.writeText(command.toJson())
    }

    fun loadCommand(id: String): Command? {
        val file = _commandFile(id)
        if (!file.exists()) return null
        return ApplicationCommand.fromJson(file.readText())
    }

    fun removeCommand(id: String) {
        val file = _commandFile(id)
        if (!file.exists()) return
        file.delete()
    }

    fun existsCommand(id: String): Boolean {
        val file = _commandFile(id)
        return file.exists()
    }

}
