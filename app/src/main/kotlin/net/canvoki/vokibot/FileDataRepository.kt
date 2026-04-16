package net.canvoki.vokibot

import java.io.File

class FileDataRepository(directoryPath: String) {
    typealias Command = ApplicationCommand
    private val _directory = File(directoryPath)

    init {
        _directory.mkdirs()
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
