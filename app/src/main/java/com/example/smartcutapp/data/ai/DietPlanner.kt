package com.example.smartcutapp.data.ai

/** Составление рациона на заданное число калорий с помощью нейросети. */
object DietPlanner {

    private const val SYSTEM =
        "Ты диетолог в кухонном приложении SmartCut. Составляешь рацион на день. Отвечай по-русски, " +
            "кратко и структурно: приёмы пищи, блюда с массой в граммах, ккал по каждому блюду, " +
            "в конце итог за день (ккал, белки, жиры, углеводы). Предпочитай блюда из овощей и фруктов, " +
            "которые можно нарезать брусочками, шинковкой или тёркой."

    suspend fun plan(targetKcal: Int, meals: Int, preferences: String, alreadyEatenKcal: Int): String {
        val request = buildString {
            append("Составь рацион на день на $targetKcal ккал, приёмов пищи: $meals.")
            if (alreadyEatenKcal > 0) {
                append(" Сегодня уже съедено $alreadyEatenKcal ккал — составь план на оставшиеся ${(targetKcal - alreadyEatenKcal).coerceAtLeast(0)} ккал.")
            }
            if (preferences.isNotBlank()) append(" Пожелания и ограничения: ${preferences.trim()}.")
        }
        return LlmClient.chat(SYSTEM, request, temperature = 0.6, maxTokens = 1500)
    }
}
