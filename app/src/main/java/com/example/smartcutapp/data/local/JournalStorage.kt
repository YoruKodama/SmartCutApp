package com.example.smartcutapp.data.local

import com.example.smartcutapp.domain.model.JournalEntry
import com.example.smartcutapp.domain.model.Nutrition
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.util.Calendar

@Serializable
private data class JournalRow(
    val id: Long,
    val timestamp: Long,
    val title: String,
    val grams: Int? = null,
    val kcal: Double = 0.0,
    val protein: Double = 0.0,
    val fat: Double = 0.0,
    val carbs: Double = 0.0,
    val source: String = "manual"
)

/** Журнал питания: хранится только на телефоне, работает без связи с устройством и сервером. */
object JournalStorage {

    private val json = Json { ignoreUnknownKeys = true }
    private val serializer = ListSerializer(JournalRow.serializer())

    private val _entries = MutableStateFlow(runCatching { load() }.getOrDefault(emptyList()))

    /** Записи от новых к старым. */
    val entries: StateFlow<List<JournalEntry>> = _entries

    fun add(
        title: String,
        nutrition: Nutrition,
        grams: Int? = null,
        source: String = "manual",
        timestamp: Long = System.currentTimeMillis()
    ) {
        val entry = JournalEntry(timestamp, timestamp, title, grams, nutrition, source)
        update { listOf(entry) + it }
    }

    fun delete(id: Long) = update { list -> list.filterNot { it.id == id } }

    private fun update(block: (List<JournalEntry>) -> List<JournalEntry>) {
        val next = block(_entries.value).sortedByDescending { it.timestamp }
        _entries.value = next
        runCatching { PreferencesManager.journalJson = json.encodeToString(serializer, next.map { it.toRow() }) }
    }

    private fun load(): List<JournalEntry> =
        json.decodeFromString(serializer, PreferencesManager.journalJson).map { it.toEntry() }

    private fun JournalEntry.toRow() =
        JournalRow(id, timestamp, title, grams, nutrition.kcal, nutrition.protein, nutrition.fat, nutrition.carbs, source)

    private fun JournalRow.toEntry() =
        JournalEntry(id, timestamp, title, grams, Nutrition(kcal, protein, fat, carbs), source)

    /** Начало суток (локальное время) для группировки записей по дням. */
    fun dayStart(timestamp: Long): Long = Calendar.getInstance().apply {
        timeInMillis = timestamp
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}
