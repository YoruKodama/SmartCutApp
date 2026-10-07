package com.example.smartcutapp.domain.model

data class Recipe(
    val id: Int,
    val name: String,
    val ingredients: List<Ingredient>,
    val cookingTime: String,
    val imageUrl: String?,
    /** Базовое число порций, на которое рассчитаны массы ингредиентов. */
    val servings: Int = 1,
    val steps: List<String> = emptyList(),
    val tags: List<String> = emptyList()
)
