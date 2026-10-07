package com.example.smartcutapp.data.mapper

import com.example.smartcutapp.data.remote.dto.RecipeRequestDto
import com.example.smartcutapp.domain.model.CutType
import com.example.smartcutapp.domain.model.Ingredient
import com.example.smartcutapp.domain.model.Recipe
import com.example.smartcutapp.domain.model.RecipeDraft
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject

@Serializable
private data class ExchangeFile(
    val format: String = FORMAT,
    val version: Int = 1,
    val recipes: List<RecipeRequestDto>
)

private const val FORMAT = "smartcut-recipes"

/** Экспорт и импорт рецептов файлом (JSON). Идентификаторы сервера в файл не попадают. */
object RecipeExchange {

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true; encodeDefaults = true }

    fun encode(recipes: List<Recipe>): String =
        json.encodeToString(ExchangeFile.serializer(), ExchangeFile(recipes = recipes.map { it.toDraft().toRequest() }))

    /** Принимает файл приложения, а также «голый» массив рецептов. Бросает IllegalArgumentException с текстом для пользователя. */
    fun decode(text: String): List<RecipeDraft> {
        val root = runCatching { json.parseToJsonElement(text) }
            .getOrElse { throw IllegalArgumentException("Файл не похож на JSON с рецептами") }
        val list: List<RecipeRequestDto> = try {
            when (root) {
                is JsonArray -> json.decodeFromJsonElement(ListSerializer(RecipeRequestDto.serializer()), root)
                is JsonObject -> json.decodeFromJsonElement(ExchangeFile.serializer(), root).recipes
                else -> throw IllegalArgumentException()
            }
        } catch (e: Exception) {
            throw IllegalArgumentException("Не удалось прочитать рецепты из файла: неверный формат")
        }
        val drafts = list.filter { it.name.isNotBlank() }.map { it.toDraft() }
        if (drafts.isEmpty()) throw IllegalArgumentException("В файле нет рецептов")
        return drafts
    }

    private fun RecipeRequestDto.toDraft() = RecipeDraft(
        name = name.trim(),
        cookingTime = cookingTime.orEmpty(),
        imageUrl = imageUrl,
        servings = servings.coerceAtLeast(1),
        steps = steps.orEmpty().lines().map { it.trim() }.filter { it.isNotEmpty() },
        tags = tags.orEmpty().split(",").map { it.trim() }.filter { it.isNotEmpty() },
        ingredients = ingredients.filter { it.name.isNotBlank() }.mapIndexed { i, ing ->
            val type = CutType.fromApi(ing.cutType)
            Ingredient(
                id = i,
                name = ing.name.trim(),
                amount = ing.amount.orEmpty(),
                cuttable = ing.cuttable || type != null,
                grams = ing.grams,
                cutType = type
            )
        }
    )
}
