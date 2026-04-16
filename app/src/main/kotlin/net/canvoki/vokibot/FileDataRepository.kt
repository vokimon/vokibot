package net.canvoki.vokibot

import java.io.File

class FileDataRepository(directoryPath: String) {
    typealias Command = ApplicationCommand
    private val _commands = mutableMapOf<String, Command>()
    private val _directory = File(directoryPath)

    init {
        _directory.mkdirs()
    }

    fun saveCommand(id: String, command: Command) {
        val file = File(_directory, "$id.json")
        file.writeText(command.toJson())
    }

    fun loadCommand(id: String): Command? {
        val file = File(_directory, "$id.json")
        if (!file.exists()) return null
        return ApplicationCommand.fromJson(file.readText())
    }

    fun removeCommand(id: String) {
        val file = File(_directory, "$id.json")
        if (!file.exists()) return
        file.delete()
    }
}
