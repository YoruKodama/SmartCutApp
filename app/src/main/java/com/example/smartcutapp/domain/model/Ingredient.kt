package com.example.smartcutapp.domain.model

data class Ingredient(
    val id: Int,
    val name: String,
    val amount: String,
    val cuttable: Boolean = false
)