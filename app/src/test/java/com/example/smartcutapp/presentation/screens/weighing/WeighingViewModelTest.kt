package com.example.smartcutapp.presentation.screens.weighing

import com.example.smartcutapp.data.mqtt.MqttManager
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class WeighingViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockkObject(MqttManager)
        every { MqttManager.isConnected } returns MutableStateFlow(false)
        every { MqttManager.esp32Online } returns MutableStateFlow(false)
        every { MqttManager.weightGrams } returns MutableStateFlow(0f)
    }

    @After
    fun tearDown() {
        unmockkObject(MqttManager)
        Dispatchers.resetMain()
    }

    private fun vm() = WeighingViewModel()

    @Test
    fun `initial unit is GRAMS`() {
        assertEquals(WeightUnit.GRAMS, vm().unit.value)
    }

    @Test
    fun `setUnit changes unit to KG`() {
        val vm = vm()
        vm.setUnit(WeightUnit.KG)
        assertEquals(WeightUnit.KG, vm.unit.value)
    }

    @Test
    fun `setUnit back to GRAMS after KG`() {
        val vm = vm()
        vm.setUnit(WeightUnit.KG)
        vm.setUnit(WeightUnit.GRAMS)
        assertEquals(WeightUnit.GRAMS, vm.unit.value)
    }

    @Test
    fun `resetTare resets displayWeight to initial zero`() {
        val vm = vm()
        vm.tare()
        vm.resetTare()
        assertEquals(0f, vm.displayWeight.value, 0.001f)
    }

    @Test
    fun `initial displayWeight is zero`() {
        assertEquals(0f, vm().displayWeight.value, 0.001f)
    }

    @Test
    fun `tare with positive weight reduces displayWeight to zero`() {
        val weightFlow = MutableStateFlow(250f)
        every { MqttManager.weightGrams } returns weightFlow
        val vm = vm()
        vm.tare()
        assertEquals(0f, vm.displayWeight.value, 0.001f)
    }
}
