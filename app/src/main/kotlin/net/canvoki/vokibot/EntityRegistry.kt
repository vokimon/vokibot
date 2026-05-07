package net.canvoki.vokibot

/**
 * Registry for entity types.
 */
class EntityRegistry {
    private var registeredType: EntityTypeInfo? = null

    fun register(typeInfo: EntityTypeInfo) {
        registeredType = typeInfo
    }

    fun getRegisteredTypes(): List<EntityTypeInfo> =
        if (registeredType != null) listOf(registeredType) else emptyList()
}
