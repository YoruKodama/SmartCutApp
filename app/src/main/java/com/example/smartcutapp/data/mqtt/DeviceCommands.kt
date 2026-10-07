package com.example.smartcutapp.data.mqtt

import com.example.smartcutapp.domain.model.CutType
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/** Сообщения высокого уровня для устройства. Приводом приложение напрямую не управляет. */
object DeviceCommands {

    /**
     * Текущий ингредиент рецепта: какой продукт, какой нарезкой и сколько. Старт нарезки
     * подтверждается на устройстве, если не включён режим автоподтверждения ([autoConfirm]).
     */
    fun recipeStep(product: String, cutType: CutType, grams: Int?, autoConfirm: Boolean): String =
        buildJsonObject {
            put("action", "recipe_step")
            put("product", product)
            put("cut", cutType.apiName)
            if (grams != null) put("grams", grams)
            put("autoconfirm", autoConfirm)
        }.toString()

    fun clearStep(): String = buildJsonObject { put("action", "clear_step") }.toString()

    fun stop(): String = buildJsonObject { put("action", "stop") }.toString()
}
