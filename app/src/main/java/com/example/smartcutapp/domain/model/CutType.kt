package com.example.smartcutapp.domain.model

/** Вид нарезки. Каждому виду соответствует своя насадка. */
enum class CutType(val apiName: String, val label: String, val attachment: String) {
    STICKS("sticks", "Брусочки", "Насадка «Брусочки»"),
    SHRED("shred", "Шинковка", "Насадка «Шинковка»"),
    GRATE("grate", "Терка", "Насадка «Терка»");

    companion object {
        fun fromApi(value: String?): CutType? = entries.firstOrNull { it.apiName == value }
    }
}
