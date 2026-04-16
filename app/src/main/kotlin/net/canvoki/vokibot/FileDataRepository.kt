package net.canvoki.vokibot

import java.io.File

class FileDataRepository(directoryPath: String) {
    init {
        File(directoryPath).mkdirs()
    }
}
