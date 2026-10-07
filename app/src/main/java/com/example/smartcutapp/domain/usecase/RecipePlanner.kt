package com.example.smartcutapp.domain.usecase

import com.example.smartcutapp.domain.model.CutType
import com.example.smartcutapp.domain.model.Ingredient
import com.example.smartcutapp.domain.model.Nutrition
import com.example.smartcutapp.domain.model.ProductNutrition
import com.example.smartcutapp.domain.model.Recipe
import kotlin.math.roundToInt

/** Ингредиенты, которые режутся одной насадкой. */
data class AttachmentGroup(val cutType: CutType, val ingredients: List<Ingredient>)

/** Чистая логика рецептов: масштабирование порций, порядок насадок, фильтр, подсчёт ккал. */
object RecipePlanner {

    fun scaleGrams(grams: Int, baseServings: Int, targetServings: Int): Int {
        if (baseServings <= 0) return grams
        return (grams.toDouble() * targetServings / baseServings).roundToInt()
    }

    /** Масса ингредиента на нужное число порций: сначала заданные граммы, затем «300 г» из текста. */
    fun scaledGrams(recipe: Recipe, ingredient: Ingredient, servings: Int): Int? {
        val base = ingredient.grams ?: parseGrams(ingredient.amount) ?: return null
        return scaleGrams(base, recipe.servings, servings)
    }

    /** «300 г» → 300, «1.5 кг» → 1500, «2 шт» / «по вкусу» → null. */
    fun parseGrams(amount: String): Int? {
        val m = Regex("""^\s*(\d+(?:[.,]\d+)?)\s*(кг|kg|г|гр|g)\.?\s*$""", RegexOption.IGNORE_CASE)
            .find(amount) ?: return null
        val value = m.groupValues[1].replace(',', '.').toDoubleOrNull() ?: return null
        val unit = m.groupValues[2].lowercase()
        return (if (unit == "кг" || unit == "kg") value * 1000 else value).roundToInt()
    }

    /**
     * Группы по насадкам в порядке первого появления в рецепте: ингредиенты с одной насадкой идут
     * подряд, чтобы её меняли реже. Ингредиент без вида нарезки в план не входит.
     */
    fun attachmentPlan(recipe: Recipe): List<AttachmentGroup> {
        val order = LinkedHashMap<CutType, MutableList<Ingredient>>()
        recipe.ingredients.forEach { ing ->
            val type = ing.cutType ?: return@forEach
            order.getOrPut(type) { mutableListOf() }.add(ing)
        }
        return order.map { AttachmentGroup(it.key, it.value) }
    }

    fun requiredAttachments(recipe: Recipe): Set<CutType> =
        recipe.ingredients.mapNotNull { it.cutType }.toSet()

    /** Фильтр «под мои насадки»: рецепт подходит, если все нужные насадки есть у пользователя. */
    fun matchesAttachments(recipe: Recipe, available: Set<CutType>): Boolean =
        available.containsAll(requiredAttachments(recipe))

    /** Ккал/БЖУ всего блюда. [perIngredient] — оценка на 100 г по id ингредиента. */
    fun totalNutrition(
        recipe: Recipe,
        servings: Int,
        perIngredient: Map<Int, ProductNutrition>
    ): Nutrition = recipe.ingredients.fold(Nutrition.ZERO) { acc, ing ->
        val info = perIngredient[ing.id] ?: return@fold acc
        val grams = scaledGrams(recipe, ing, servings) ?: info.estimatedGrams?.let {
            scaleGrams(it, recipe.servings, servings)
        } ?: return@fold acc
        acc + info.forGrams(grams.toDouble())
    }
}
