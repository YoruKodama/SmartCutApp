package com.example.smartcutapp.data.mapper

import com.example.smartcutapp.data.remote.dto.IngredientRequestDto
import com.example.smartcutapp.data.remote.dto.IngredientResponseDto
import com.example.smartcutapp.data.remote.dto.RecipeRequestDto
import com.example.smartcutapp.data.remote.dto.RecipeResponseDto
import com.example.smartcutapp.domain.model.CutType
import com.example.smartcutapp.domain.model.Ingredient
import com.example.smartcutapp.domain.model.Recipe
import com.example.smartcutapp.domain.model.RecipeDraft

fun RecipeResponseDto.toRecipe() = Recipe(
    id = id,
    name = name,
    cookingTime = cookingTime ?: "",
    imageUrl = imageUrl,
    ingredients = ingredients.map { it.toIngredient() },
    servings = servings.coerceAtLeast(1),
    steps = steps.orEmpty().lines().map { it.trim() }.filter { it.isNotEmpty() },
    tags = tags.orEmpty().split(",").map { it.trim() }.filter { it.isNotEmpty() }
)

fun IngredientResponseDto.toIngredient(): Ingredient {
    val type = CutType.fromApi(cutType)
    return Ingredient(
        id = id,
        name = name,
        amount = amount ?: "",
        cuttable = cuttable || type != null,
        grams = grams,
        cutType = type
    )
}

fun Recipe.toDraft() = RecipeDraft(
    name = name,
    cookingTime = cookingTime,
    imageUrl = imageUrl,
    servings = servings,
    steps = steps,
    tags = tags,
    ingredients = ingredients
)

fun RecipeDraft.toRequest() = RecipeRequestDto(
    name = name.trim(),
    cookingTime = cookingTime.trim().ifEmpty { null },
    imageUrl = imageUrl,
    servings = servings.coerceAtLeast(1),
    steps = steps.joinToString("\n").ifBlank { null },
    tags = tags.joinToString(",").ifBlank { null },
    ingredients = ingredients.filter { it.name.isNotBlank() }.map {
        IngredientRequestDto(
            name = it.name.trim(),
            amount = it.amount.trim().ifEmpty { null },
            cuttable = it.cutType != null || it.cuttable,
            grams = it.grams,
            cutType = it.cutType?.apiName
        )
    }
)
