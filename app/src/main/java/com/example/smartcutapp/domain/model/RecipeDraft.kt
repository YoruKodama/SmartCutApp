package com.example.smartcutapp.domain.model

/** Данные рецепта для создания/правки/импорта; id присваивает сервер. */
data class RecipeDraft(
    val name: String,
    val cookingTime: String = "",
    val imageUrl: String? = null,
    val servings: Int = 1,
    val steps: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val ingredients: List<Ingredient> = emptyList()
)
