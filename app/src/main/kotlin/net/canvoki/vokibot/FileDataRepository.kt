package net.canvoki.vokibot

import java.io.File

class FileDataRepository(directoryPath: String) {
    typealias Command = ApplicationCommand
    private val _commands = mutableMapOf<String, Command>()
    init {
        File(directoryPath).mkdirs()
    }

    fun saveCommand(id: String, command: Command) {
        _commands[id] = command
    }

    fun loadCommand(id: String): Command? {
        return _commands[id]
    }
}
