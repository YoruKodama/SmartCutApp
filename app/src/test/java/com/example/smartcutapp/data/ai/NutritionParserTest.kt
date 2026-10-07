package com.example.smartcutapp.data.ai

import org.junit.Assert.*
import org.junit.Test

class NutritionParserTest {

    @Test fun `parses items object`() {
        val raw = """{"items":[{"index":1,"kcal100":15,"protein100":0.8,"fat100":0.1,"carbs100":2.8,"grams":250},
            {"index":2,"kcal100":"32,0","protein100":1.3,"fat100":0.1,"carbs100":6.9,"grams":80}]}"""
        val r = NutritionParser.parse(raw, listOf("Огурец", "Морковь"))
        assertEquals(2, r.size)
        assertEquals(15.0, r[0]!!.per100g.kcal, 0.001)
        assertEquals(250, r[0]!!.estimatedGrams)
        assertEquals(32.0, r[1]!!.per100g.kcal, 0.001)
    }

    @Test fun `strips code fences and accepts bare array`() {
        val raw = "```json\n[{\"kcal100\":20,\"protein100\":1,\"fat100\":0,\"carbs100\":4}]\n```"
        val r = NutritionParser.parse(raw, listOf("Помидор"))
        assertEquals(20.0, r[0]!!.per100g.kcal, 0.001)
    }

    @Test fun `garbage yields empty map`() {
        assertTrue(NutritionParser.parse("не json", listOf("a")).isEmpty())
        assertTrue(NutritionParser.parse("""{"items":[{"index":1}]}""", listOf("a")).isEmpty())
    }
}
