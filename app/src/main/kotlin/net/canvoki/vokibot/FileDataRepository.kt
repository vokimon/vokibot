package net.canvoki.vokibot

import java.io.File

class FileDataRepository(directoryPath: String) {
    typealias Command = ApplicationCommand
    var _command: Command? = null
    init {
        File(directoryPath).mkdirs()
    }

    fun saveCommand(id: String, command: Command) {
        _command = command
    }

    fun loadCommand(id: String): Command? {
        return _command
    }
}
