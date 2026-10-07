package com.example.smartcutapp.data.ai

import com.example.smartcutapp.domain.model.Nutrition
import com.example.smartcutapp.domain.model.ProductNutrition
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonObject
import kotlin.math.roundToInt

/** Разбор JSON-ответа ИИ с питательностью продуктов. Чистая функция — покрыта тестами. */
object NutritionParser {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    /**
     * Ожидает `{"items":[{"index":1,"kcal100":..,"protein100":..,"fat100":..,"carbs100":..,"grams":..}]}`
     * (допускается и голый массив, и ограждение ```json). Возвращает результат по индексам запроса
     * (с нуля); продукты, для которых ИИ ничего не вернул, отсутствуют в карте.
     */
    fun parse(raw: String, names: List<String>): Map<Int, ProductNutrition> {
        val text = raw.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
        val root = runCatching { json.parseToJsonElement(text) }.getOrNull() ?: return emptyMap()
        val items: JsonArray = when (root) {
            is JsonArray -> root
            is JsonObject -> root["items"] as? JsonArray ?: return emptyMap()
            else -> return emptyMap()
        }
        val result = LinkedHashMap<Int, ProductNutrition>()
        items.forEachIndexed { pos, el ->
            val obj = runCatching { el.jsonObject }.getOrNull() ?: return@forEachIndexed
            // index в ответе начинается с 1; если его нет — берём позицию в массиве
            val idx = (obj.num("index")?.roundToInt()?.minus(1)) ?: pos
            val name = names.getOrNull(idx) ?: return@forEachIndexed
            val kcal = obj.num("kcal100") ?: return@forEachIndexed
            result[idx] = ProductNutrition(
                name = name,
                per100g = Nutrition(
                    kcal = kcal.coerceAtLeast(0.0),
                    protein = (obj.num("protein100") ?: 0.0).coerceAtLeast(0.0),
                    fat = (obj.num("fat100") ?: 0.0).coerceAtLeast(0.0),
                    carbs = (obj.num("carbs100") ?: 0.0).coerceAtLeast(0.0)
                ),
                estimatedGrams = obj.num("grams")?.roundToInt()?.takeIf { it > 0 }
            )
        }
        return result
    }

    private fun JsonObject.num(key: String): Double? {
        val p = this[key] as? JsonPrimitive ?: return null
        return p.doubleOrNull ?: p.content.replace(',', '.').toDoubleOrNull()
    }
}
