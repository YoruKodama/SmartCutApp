package com.example.smartcutapp.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RecipeResponseDto(
    val id: Int,
    val name: String,
    val cookingTime: String? = null,
    val imageUrl: String? = null,
    val userId: Int,
    val servings: Int = 1,
    val steps: String? = null,
    val tags: String? = null,
    val ingredients: List<IngredientResponseDto> = emptyList()
)

@Serializable
data class IngredientResponseDto(
    val id: Int,
    val name: String,
    val amount: String? = null,
    val cuttable: Boolean = false,
    val grams: Int? = null,
    val cutType: String? = null
)

@Serializable
data class RecipeRequestDto(
    val name: String,
    val cookingTime: String? = null,
    val imageUrl: String? = null,
    val servings: Int = 1,
    val steps: String? = null,
    val tags: String? = null,
    val ingredients: List<IngredientRequestDto> = emptyList()
)

@Serializable
data class IngredientRequestDto(
    val name: String,
    val amount: String? = null,
    val cuttable: Boolean = false,
    val grams: Int? = null,
    val cutType: String? = null
)

@Serializable
data class UploadResponseDto(val url: String)
