package com.example.smartcutapp.domain.model

data class Recipe(
    val id: Int,
    val name: String,
    val ingredients: List<Ingredient>,
    val cookingTime: String,
    val imageUrl: String?
)