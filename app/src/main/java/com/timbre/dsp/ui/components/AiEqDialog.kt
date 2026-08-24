package com.timbre.dsp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.timbre.dsp.R
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.timbre.dsp.audio.AiEqAssistant
import com.timbre.dsp.data.AiPreferences
import com.timbre.dsp.data.AiPreferencesRepository
import com.timbre.dsp.data.api.AiClient
import com.timbre.dsp.data.api.AiEqResponse
import com.timbre.dsp.data.api.AiProvider
import com.timbre.dsp.model.DSPSettings
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun AiEqDialog(
    currentSettings: DSPSettings,
    onDismiss: () -> Unit,
    onApplySettings: (DSPSettings) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var aiPrefs by remember { mutableStateOf(AiPreferencesRepository.load(context)) }
    var showConfig by remember { mutableStateOf(aiPrefs.apiKey.isBlank() && aiPrefs.provider != AiProvider.OLLAMA) }
    var prompt by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var aiResult by remember { mutableStateOf<AiEqResponse?>(null) }

    var providerMenuExpanded by remember { mutableStateOf(false) }
    var modelMenuExpanded by remember { mutableStateOf(false) }

    val quickPrompts = listOf(
        "Warm Acoustic & Intimate Vocals",
        "Deep Sub-Bass EDM Club Banger",
        "Harman Target 2019 In-Ear Curve",
        "Audiophile Crisp Air & Wide Stage",
        "Rock & Metal Mid-Forward Punch",
        "Late-Night Soft Relaxing Lo-Fi",
        "Cinematic Movie Dialogue & Immersion"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AI Audio Assistant",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { showConfig = !showConfig }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "AI Settings",
                        tint = if (showConfig) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // AI Settings Collapse Panel
                AnimatedVisibility(visible = showConfig) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "AI Provider Configuration",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )

                            // Provider Selector
                            Box {
                                OutlinedButton(
                                    onClick = { providerMenuExpanded = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(aiPrefs.provider.displayName)
                                        Icon(Icons.Default.ExpandMore, contentDescription = null)
                                    }
                                }
                                DropdownMenu(
                                    expanded = providerMenuExpanded,
                                    onDismissRequest = { providerMenuExpanded = false }
                                ) {
                                    AiProvider.values().forEach { provider ->
                                        DropdownMenuItem(
                                            text = { Text(provider.displayName) },
                                            onClick = {
                                                aiPrefs = aiPrefs.copy(
                                                    provider = provider,
                                                    model = AiClient.getDefaultModel(provider)
                                                )
                                                AiPreferencesRepository.save(context, aiPrefs)
                                                providerMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Model Selector
                            val models = AiClient.getAvailableModels(aiPrefs.provider)
                            if (models.isNotEmpty()) {
                                Box {
                                    OutlinedButton(
                                        onClick = { modelMenuExpanded = true },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(models.find { it.id == aiPrefs.model }?.displayName ?: aiPrefs.model)
                                            Icon(Icons.Default.ExpandMore, contentDescription = null)
                                        }
                                    }
                                    DropdownMenu(
                                        expanded = modelMenuExpanded,
                                        onDismissRequest = { modelMenuExpanded = false }
                                    ) {
                                        models.forEach { opt ->
                                            DropdownMenuItem(
                                                text = {
                                                    Column {
                                                        Text(opt.displayName, fontWeight = FontWeight.Medium)
                                                        if (opt.description.isNotBlank()) {
                                                            Text(opt.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                        }
                                                    }
                                                },
                                                onClick = {
                                                    aiPrefs = aiPrefs.copy(model = opt.id)
                                                    AiPreferencesRepository.save(context, aiPrefs)
                                                    modelMenuExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            // API Key / Server URL
                            if (aiPrefs.provider == AiProvider.OLLAMA) {
                                OutlinedTextField(
                                    value = aiPrefs.serverUrl,
                                    onValueChange = {
                                        aiPrefs = aiPrefs.copy(serverUrl = it)
                                        AiPreferencesRepository.save(context, aiPrefs)
                                    },
                                    label = { Text(stringResource(R.string.ai_dialog_ollama_url)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            } else {
                                OutlinedTextField(
                                    value = aiPrefs.apiKey,
                                    onValueChange = {
                                        aiPrefs = aiPrefs.copy(apiKey = it)
                                        AiPreferencesRepository.save(context, aiPrefs)
                                    },
                                    label = { Text(stringResource(R.string.ai_dialog_provider_api_key, aiPrefs.provider.displayName)) },
                                    leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                                    visualTransformation = PasswordVisualTransformation(),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                        }
                    }
                }

                Text(
                    text = stringResource(R.string.ai_dialog_desc),
                    style = MaterialTheme.typography.bodyMedium
                )

                // Quick Prompt Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickPrompts.forEach { qp ->
                        AssistChip(
                            onClick = { prompt = qp },
                            label = { Text(qp, style = MaterialTheme.typography.labelSmall) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                            )
                        )
                    }
                }

                // Custom Prompt Input
                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    label = { Text(stringResource(R.string.ai_dialog_acoustic_prompt)) },
                    placeholder = { Text(stringResource(R.string.ai_dialog_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )

                // Error Banner
                if (errorMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                // AI Result Preview Card
                if (aiResult != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = aiResult?.title ?: "AI Tuning Profile",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (!aiResult?.description.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = aiResult?.description ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Preamp: ${String.format(Locale.ROOT, "%.1f", aiResult?.preamp ?: 0f)} dB • Bass: ${aiResult?.bassBoost ?: 0}/1000 • Clarity: ${aiResult?.clarityBoost ?: 0}/1000",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (aiResult != null) {
                Button(
                    onClick = {
                        val newSettings = AiEqAssistant.applyToSettings(aiResult!!, currentSettings)
                        onApplySettings(newSettings)
                        onDismiss()
                    }
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.btn_apply_to_dsp))
                }
            } else {
                Button(
                    onClick = {
                        if (prompt.isBlank()) {
                            errorMessage = "Please enter an acoustic description or select a preset prompt."
                            return@Button
                        }
                        if (aiPrefs.apiKey.isBlank() && aiPrefs.provider != AiProvider.OLLAMA) {
                            errorMessage = "Please enter your ${aiPrefs.provider.displayName} API Key in Settings."
                            showConfig = true
                            return@Button
                        }

                        isLoading = true
                        errorMessage = null
                        scope.launch {
                            val (result, error) = AiEqAssistant.getAdjustment(
                                userPrompt = prompt,
                                currentSettings = currentSettings,
                                provider = aiPrefs.provider,
                                apiKey = aiPrefs.apiKey,
                                serverUrl = aiPrefs.serverUrl,
                                model = aiPrefs.model
                            )
                            isLoading = false
                            if (result != null) {
                                aiResult = result
                            } else {
                                errorMessage = error ?: "Unknown error while generating AI profile."
                            }
                        }
                    },
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.ai_dialog_engineering))
                    } else {
                        Icon(Icons.Default.Psychology, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.btn_generate))
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    )
}
