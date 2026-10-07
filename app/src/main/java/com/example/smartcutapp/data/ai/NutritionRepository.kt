package com.example.smartcutapp.data.ai

import com.example.smartcutapp.data.local.PreferencesManager
import com.example.smartcutapp.domain.model.Nutrition
import com.example.smartcutapp.domain.model.ProductNutrition
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

/** Продукт и количество из рецепта («Огурец», «2 шт»). */
data class NutritionQuery(val name: String, val amount: String = "")

@Serializable
private data class CachedItem(
    val kcal: Double,
    val protein: Double,
    val fat: Double,
    val carbs: Double,
    val grams: Int? = null
)

/**
 * Калории и БЖУ продуктов от нейросети. Результаты кэшируются на телефоне: повторный расчёт
 * тех же продуктов (в том числе при другом числе порций) работает без интернета.
 */
object NutritionRepository {

    private val json = Json { ignoreUnknownKeys = true }
    private val cacheSerializer = MapSerializer(String.serializer(), CachedItem.serializer())

    private const val SYSTEM = "Ты диетолог-нутрициолог. Отвечай ТОЛЬКО валидным JSON-объектом, без пояснений."

    /** Только то, что уже посчитано и лежит в кэше (работает без интернета). */
    fun cached(queries: List<NutritionQuery>): Map<Int, ProductNutrition> {
        val cache = loadCache()
        return queries.mapIndexedNotNull { i, q -> cache[key(q)]?.let { i to it.toProduct(q.name) } }.toMap()
    }

    /** Оценка на 100 г для каждого запроса; ключ карты — индекс в [queries]. */
    suspend fun estimate(queries: List<NutritionQuery>): Map<Int, ProductNutrition> {
        if (queries.isEmpty()) return emptyMap()
        val cache = loadCache().toMutableMap()
        val result = LinkedHashMap<Int, ProductNutrition>()
        val missing = mutableListOf<Int>()

        queries.forEachIndexed { i, q ->
            val hit = cache[key(q)]
            if (hit != null) result[i] = hit.toProduct(q.name) else missing += i
        }

        if (missing.isNotEmpty()) {
            val names = missing.map { queries[it].name }
            val raw = LlmClient.chat(SYSTEM, prompt(missing.map { queries[it] }), json = true)
            val parsed = NutritionParser.parse(raw, names)
            if (parsed.isEmpty()) error("Не удалось разобрать ответ ИИ. Попробуйте ещё раз.")
            parsed.forEach { (pos, product) ->
                val original = missing[pos]
                result[original] = product
                cache[key(queries[original])] = CachedItem(
                    product.per100g.kcal, product.per100g.protein, product.per100g.fat,
                    product.per100g.carbs, product.estimatedGrams
                )
            }
            saveCache(cache)
        }
        return result
    }

    private fun prompt(queries: List<NutritionQuery>): String = buildString {
        appendLine("Для каждого продукта дай питательность СЫРОГО продукта на 100 г и оцени массу указанного количества.")
        appendLine("Формат ответа: {\"items\":[{\"index\":1,\"kcal100\":число,\"protein100\":число,\"fat100\":число,\"carbs100\":число,\"grams\":число}]}")
        appendLine("grams — масса указанного количества в граммах (для «по вкусу» — 3; для «ст.л» масла — 15 г за ложку).")
        appendLine("Продукты:")
        queries.forEachIndexed { i, q ->
            appendLine("${i + 1}. ${q.name}${if (q.amount.isNotBlank()) " — ${q.amount}" else ""}")
        }
    }

    private fun key(q: NutritionQuery) = "${q.name.trim().lowercase()}|${q.amount.trim().lowercase()}"

    private fun CachedItem.toProduct(name: String) =
        ProductNutrition(name, Nutrition(kcal, protein, fat, carbs), grams)

    private fun loadCache(): Map<String, CachedItem> =
        runCatching { json.decodeFromString(cacheSerializer, PreferencesManager.nutritionCacheJson) }
            .getOrDefault(emptyMap())

    private fun saveCache(cache: Map<String, CachedItem>) {
        runCatching { PreferencesManager.nutritionCacheJson = json.encodeToString(cacheSerializer, cache) }
    }
}
