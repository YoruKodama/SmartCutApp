package com.example.smartcutapp.data.mapper

import com.example.smartcutapp.data.remote.dto.IngredientResponseDto
import com.example.smartcutapp.data.remote.dto.RecipeResponseDto
import com.example.smartcutapp.domain.model.Ingredient
import com.example.smartcutapp.domain.model.Recipe

fun RecipeResponseDto.toRecipe() = Recipe(
    id = id,
    name = name,
    cookingTime = cookingTime ?: "",
    imageUrl = imageUrl,
    ingredients = ingredients.map { it.toIngredient() }
)

fun IngredientResponseDto.toIngredient() = Ingredient(
    id = id,
    name = name,
    amount = amount ?: "",
    cuttable = cuttable
)