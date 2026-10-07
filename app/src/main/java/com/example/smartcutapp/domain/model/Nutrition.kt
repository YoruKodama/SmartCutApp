package com.example.smartcutapp.domain.model

/** Калории и БЖУ. Значения — на ту массу, к которой относятся (порция, продукт, блюдо). */
data class Nutrition(
    val kcal: Double = 0.0,
    val protein: Double = 0.0,
    val fat: Double = 0.0,
    val carbs: Double = 0.0
) {
    operator fun plus(o: Nutrition) =
        Nutrition(kcal + o.kcal, protein + o.protein, fat + o.fat, carbs + o.carbs)

    operator fun times(k: Double) = Nutrition(kcal * k, protein * k, fat * k, carbs * k)

    operator fun div(k: Double) = if (k == 0.0) this else times(1.0 / k)

    companion object {
        val ZERO = Nutrition()
    }
}

/** Питательность продукта на 100 г, оценённая нейросетью. */
data class ProductNutrition(
    val name: String,
    val per100g: Nutrition,
    /** Оценка массы, соответствующей количеству из рецепта (например «2 шт»), г. */
    val estimatedGrams: Int? = null
) {
    fun forGrams(grams: Double): Nutrition = per100g * (grams / 100.0)
}

data class JournalEntry(
    val id: Long,
    val timestamp: Long,
    val title: String,
    val grams: Int?,
    val nutrition: Nutrition,
    /** recipe | free | manual */
    val source: String
)
