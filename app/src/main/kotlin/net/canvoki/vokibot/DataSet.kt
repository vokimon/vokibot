package net.canvoki.vokibot

import java.io.File

/**
 * Base interface for repository entities.
 * Guarantees stable `id` and self-serialization.
 */
interface StorableEntity {
    val id: String
    fun toJson(): String
}

/**
 * Typed dataset that delegates serialization to the entity itself.
 * No serializers, no Json config passed in — just `toJson()` and a decoder lambda.
 */
class DataSet<T : StorableEntity>(
    private val directory: File,
    private val prefix: String,
    private val fromJson: (String) -> T,
) {
    init {
        directory.mkdirs()
    }

    private fun sanitize(raw: String): String =
        raw.replace(Regex("[^a-zA-Z0-9_.-]"), "_")
            .replace(Regex("_+"), "_")
            .take(64)
            .trim('_')
            .ifBlank { "unnamed" }

    private fun _file(id: String): File =
        File(directory, "${prefix}${sanitize(id)}.json")

    fun save(item: T) {
        directory.mkdirs()
        val file = _file(item.id)
        file.writeText(item.toJson())
    }

    fun load(id: String): T? {
        val file = _file(id)
        return if (file.exists()) fromJson(file.readText()) else null
    }

    fun remove(id: String) {
        val file = _file(id).takeIf { it.exists() }?.delete()
    }

    fun exists(id: String): Boolean {
        return _file(id).exists()
    }

    fun listIds(): List<String> {
        return directory.listFiles { _, name ->
            name.startsWith(prefix) && name.endsWith(".json")
        }?.map { it.name.removePrefix(prefix).removeSuffix(".json") }?.sorted() ?: emptyList()
    }

    fun all(): List<T> = listIds().mapNotNull { load(it) }
}
