package com.example.smartcutapp.presentation.screens.device

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.smartcutapp.app.ui.theme.SmartCutColors
import com.example.smartcutapp.data.local.PreferencesManager
import com.example.smartcutapp.data.mqtt.MqttManager
import com.example.smartcutapp.domain.model.CutType
import com.example.smartcutapp.presentation.components.AppHeader
import com.example.smartcutapp.presentation.components.SectionCard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DeviceSetupViewModel : ViewModel() {

    private val _attachments = MutableStateFlow(PreferencesManager.availableAttachments)
    val attachments: StateFlow<Set<CutType>> = _attachments

    private val _deviceId = MutableStateFlow(PreferencesManager.deviceId)
    val deviceId: StateFlow<String> = _deviceId

    val isConnected: StateFlow<Boolean> = MqttManager.isConnected
    val esp32Online: StateFlow<Boolean> = MqttManager.esp32Online

    fun setAttachment(type: CutType, enabled: Boolean) {
        val next = if (enabled) _attachments.value + type else _attachments.value - type
        _attachments.value = next
        PreferencesManager.availableAttachments = next
    }

    /** Привязка меняет топики обмена, поэтому активное соединение переустанавливаем. */
    fun bindDevice(id: String) {
        val clean = id.trim().trim('/').replace(Regex("[^A-Za-z0-9_-]"), "")
        PreferencesManager.deviceId = clean
        _deviceId.value = clean
        if (MqttManager.isConnected.value) viewModelScope.launch { MqttManager.connect() }
    }
}

@Composable
fun DeviceSetupScreen(navController: NavController) {
    val viewModel: DeviceSetupViewModel = viewModel()
    val attachments by viewModel.attachments.collectAsState()
    val deviceId by viewModel.deviceId.collectAsState()
    val connected by viewModel.isConnected.collectAsState()
    val online by viewModel.esp32Online.collectAsState()

    var input by remember(deviceId) { mutableStateOf(deviceId) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0)
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            AppHeader(title = "Устройство и насадки", onBack = { navController.popBackStack() })

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SectionCard {
                    Text("Мои насадки", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        "По ним работает фильтр «Под мои насадки» в списке рецептов",
                        style = MaterialTheme.typography.bodySmall,
                        color = SmartCutColors.TextSecondary
                    )
                    Spacer(Modifier.height(4.dp))
                    CutType.entries.forEach { type ->
                        Row(
                            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(type.attachment, modifier = Modifier.weight(1f))
                            Switch(
                                checked = type in attachments,
                                onCheckedChange = { viewModel.setAttachment(type, it) }
                            )
                        }
                    }
                }

                SectionCard {
                    Text("Привязка устройства", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        if (deviceId.isEmpty()) "Не привязано: используются общие топики smartcut/…"
                        else "Привязано: $deviceId (топики smartcut/$deviceId/…)",
                        style = MaterialTheme.typography.bodySmall,
                        color = SmartCutColors.TextSecondary
                    )
                    Text(
                        if (connected && online) "Устройство на связи"
                        else if (connected) "Брокер доступен, устройство молчит"
                        else "Нет подключения (включается в Настройках)",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (connected && online) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        label = { Text("Код устройства (с наклейки / QR)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { viewModel.bindDevice(input) },
                            modifier = Modifier.weight(1f).height(52.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("Привязать") }
                        OutlinedButton(
                            onClick = { input = ""; viewModel.bindDevice("") },
                            enabled = deviceId.isNotEmpty(),
                            modifier = Modifier.weight(1f).height(52.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("Отвязать") }
                    }
                }
                Spacer(Modifier.height(88.dp))
            }
        }
    }
}
