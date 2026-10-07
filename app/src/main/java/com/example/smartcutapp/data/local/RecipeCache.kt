package com.example.smartcutapp.data.local

import com.example.smartcutapp.data.remote.dto.RecipeResponseDto
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/** Последний успешно загруженный список рецептов: рецепты открываются без связи с сервером. */
object RecipeCache {

    private val json = Json { ignoreUnknownKeys = true }
    private val serializer = ListSerializer(RecipeResponseDto.serializer())

    /** true, если последний ответ репозитория взят из кэша, а не с сервера. */
    @Volatile
    var servedFromCache: Boolean = false

    fun save(recipes: List<RecipeResponseDto>) {
        runCatching { PreferencesManager.recipesCache = json.encodeToString(serializer, recipes) }
    }

    fun load(): List<RecipeResponseDto> =
        runCatching { json.decodeFromString(serializer, PreferencesManager.recipesCache) }
            .getOrDefault(emptyList())
}
