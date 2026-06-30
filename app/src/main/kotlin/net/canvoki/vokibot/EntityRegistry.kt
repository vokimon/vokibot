package net.canvoki.vokibot

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.canvoki.shared.component.StackedScreen
import net.canvoki.shared.log
import kotlin.reflect.KClass

/**
 * Interface for entity type metadata, used for UI generation.
 */
interface EntityMetadata {
    val typeKey: String
    val entityClass: KClass<out StorableEntity>
    val labelRes: Int
    val iconRes: Int
    val editorFactory: (entityId: String?) -> StackedScreen<Unit>
    val deserializer: ((String) -> StorableEntity)?
    val helpRes: Int
}

/**
 * Registry for entity types.
 */
class EntityRegistry {
    private val typeInfos = mutableMapOf<String, EntityMetadata>()

    // Just to peek the type attribute ignoring anything else
    private val json =
        Json {
            ignoreUnknownKeys = true
        }

    fun register(metadata: EntityMetadata) {
        typeInfos[metadata.typeKey] = metadata
    }

    fun getRegisteredTypes(baseClass: KClass<out StorableEntity> = StorableEntity::class): List<EntityMetadata> =
        typeInfos.values.filter { baseClass.java.isAssignableFrom(it.entityClass.java) }

    fun <T : StorableEntity> fromJson(
        jsonString: String,
        clazz: KClass<T>,
    ): T? {
        val jsonObject =
            json.parseToJsonElement(jsonString) as? JsonObject ?: run {
                log("Invalid JSON object for fromJson")
                return null
            }
        val type =
            jsonObject["type"]?.jsonPrimitive?.content ?: run {
                log("Missing 'type' field in JSON")
                return null
            }
        val typeInfo =
            typeInfos[type] ?: run {
                log("Unknown entity type: $type")
                return null
            }
        val result =
            typeInfo.deserializer?.invoke(jsonString) ?: run {
                log("No deserializer for type: $type")
                return null
            }
        if (!clazz.isInstance(result)) {
            log("Expected ${clazz.simpleName}, got ${result::class.simpleName}")
            return null
        }
        return clazz.java.cast(result)
    }

    fun extractType(jsonString: String): String? {
        val jsonObject = json.parseToJsonElement(jsonString) as? JsonObject ?: return null
        return jsonObject["type"]?.jsonPrimitive?.content
    }

    fun getEditorScreen(
        typeKey: String,
        entityId: String?,
    ): StackedScreen<Unit>? {
        val typeInfo =
            typeInfos[typeKey] ?: run {
                log("Unknown entity type selected: $typeKey")
                return null
            }
        return typeInfo.editorFactory(entityId)
    }

    @Composable
    fun label(type: String): String =
        typeInfos[type]?.let {
            stringResource(it.labelRes)
        } ?: type

    /** Returns the help text resource ID for a type, or 0 if not defined */
    fun helpResId(type: String): Int = typeInfos[type]?.helpRes ?: 0
}
