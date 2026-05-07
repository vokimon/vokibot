package net.canvoki.vokibot

import kotlin.reflect.KClass

/**
 * Registry for entity types.
 */
class EntityRegistry {
    private val typeInfos = mutableMapOf<String, EntityTypeInfo>()

    fun register(typeInfo: EntityTypeInfo) {
        typeInfos[typeInfo.typeKey] = typeInfo
    }

    fun getRegisteredTypes(baseClass: KClass<out StorableEntity> = StorableEntity::class): List<EntityTypeInfo> =
        typeInfos.values.toList().filter { baseClass == StorableEntity::class || baseClass == it.entityClass }
}
