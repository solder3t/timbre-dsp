package com.timbre.dsp.ui.settings

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timbre.dsp.R
import com.timbre.dsp.data.api.UpdateChecker
import com.timbre.dsp.data.api.UpdateInfo
import com.timbre.dsp.ui.components.UpdateDialog
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AboutSettingsPage(
    onBack: () -> Unit,
    hazeState: HazeState? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isCheckingUpdates by remember { mutableStateOf(false) }
    var detectedUpdate by remember { mutableStateOf<UpdateInfo?>(null) }

    if (detectedUpdate != null) {
        UpdateDialog(
            updateInfo = detectedUpdate!!,
            onDismiss = { detectedUpdate = null }
        )
    }

    val openUrl: (String) -> Unit = { url ->
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open browser", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.settings_section_about),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.btn_close)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 1. App Hero Card
        SettingsCard(hazeState = hazeState) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Glowing Logo Container
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        )
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // App Title & Tagline
                Text(
                    text = "Timbre DSP",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Version Badge Chip
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text(
                        text = "v${UpdateChecker.CURRENT_VERSION} • Production Release",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = stringResource(R.string.about_tagline),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Feature Pill Tags
                FlowRow(
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("64-Bit Float SIMD", "Zero Latency", "AutoEq Inside", "AudioFlinger Hook", "FFT Convolution").forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.padding(horizontal = 3.dp)
                        ) {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Interactive Update Center
        SettingsCard(hazeState = hazeState) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SystemUpdate, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Update Center", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                        Text(
                            "Official releases tracked via GitHub Releases API",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        coroutineScope.launch {
                            isCheckingUpdates = true
                            val result = UpdateChecker.checkForUpdates()
                            isCheckingUpdates = false
                            if (result.isAvailable) {
                                detectedUpdate = result
                            } else {
                                Toast.makeText(context, context.getString(R.string.update_up_to_date, UpdateChecker.CURRENT_VERSION), Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isCheckingUpdates
                ) {
                    if (isCheckingUpdates) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.update_checking))
                    } else {
                        Icon(Icons.Default.SystemUpdate, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.btn_check_updates))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Engine Architecture & Capabilities
        SettingsSectionTitle(title = stringResource(R.string.about_section_engine_specs))
        SettingsCard(hazeState = hazeState) {
            Column(modifier = Modifier.padding(16.dp)) {
                AboutFeatureItem(
                    icon = Icons.Default.Speed,
                    title = stringResource(R.string.about_engine_pipeline),
                    description = stringResource(R.string.about_engine_pipeline_desc),
                    showDivider = true
                )
                AboutFeatureItem(
                    icon = Icons.Default.Tune,
                    title = stringResource(R.string.about_engine_peq),
                    description = stringResource(R.string.about_engine_peq_desc),
                    showDivider = true
                )
                AboutFeatureItem(
                    icon = Icons.Default.Headphones,
                    title = stringResource(R.string.about_engine_autoeq),
                    description = stringResource(R.string.about_engine_autoeq_desc),
                    showDivider = true
                )
                AboutFeatureItem(
                    icon = Icons.Default.SurroundSound,
                    title = stringResource(R.string.about_engine_convolution),
                    description = stringResource(R.string.about_engine_convolution_desc),
                    showDivider = false
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Contributors & Credits (SpotiFLAC style)
        SettingsSectionTitle(title = stringResource(R.string.about_section_credits))
        SettingsCard(hazeState = hazeState) {
            Column(modifier = Modifier.padding(16.dp)) {
                AboutLinkItem(
                    icon = Icons.Default.Person,
                    title = stringResource(R.string.about_author_name),
                    subtitle = stringResource(R.string.about_author_desc),
                    onClick = { openUrl("https://github.com/solder3t") },
                    showDivider = false
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 5. Special Thanks & Open Source
        SettingsSectionTitle(title = stringResource(R.string.about_special_thanks))
        SettingsCard(hazeState = hazeState) {
            Column(modifier = Modifier.padding(16.dp)) {
                AboutLinkItem(
                    icon = Icons.Default.Headphones,
                    title = stringResource(R.string.about_thanks_autoeq),
                    subtitle = stringResource(R.string.about_thanks_autoeq_desc),
                    onClick = { openUrl("https://github.com/jaakkopasanen/AutoEq") },
                    showDivider = true
                )
                AboutLinkItem(
                    icon = Icons.Default.Memory,
                    title = stringResource(R.string.about_thanks_shizuku),
                    subtitle = stringResource(R.string.about_thanks_shizuku_desc),
                    onClick = { openUrl("https://github.com/RikkaApps/Shizuku") },
                    showDivider = true
                )
                AboutLinkItem(
                    icon = Icons.Default.Waves,
                    title = stringResource(R.string.about_thanks_haze),
                    subtitle = stringResource(R.string.about_thanks_haze_desc),
                    onClick = { openUrl("https://github.com/chrisbanes/haze") },
                    showDivider = false
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 6. Source Code, Issues & Legal
        SettingsSectionTitle(title = "Community & Source Code")
        SettingsCard(hazeState = hazeState) {
            Column(modifier = Modifier.padding(16.dp)) {
                AboutLinkItem(
                    icon = Icons.Default.Code,
                    title = stringResource(R.string.about_github_title),
                    subtitle = stringResource(R.string.about_github_desc),
                    onClick = { openUrl("https://github.com/solder3t/timbre-dsp") },
                    showDivider = true
                )
                AboutLinkItem(
                    icon = Icons.Default.History,
                    title = "Releases & Changelogs",
                    subtitle = "github.com/solder3t/timbre-dsp/releases",
                    onClick = { openUrl("https://github.com/solder3t/timbre-dsp/releases") },
                    showDivider = true
                )
                AboutLinkItem(
                    icon = Icons.Default.BugReport,
                    title = stringResource(R.string.about_issues_title),
                    subtitle = stringResource(R.string.about_issues_desc),
                    onClick = { openUrl("https://github.com/solder3t/timbre-dsp/issues/new") },
                    showDivider = true
                )
                AboutLinkItem(
                    icon = Icons.Default.VerifiedUser,
                    title = stringResource(R.string.about_license_title),
                    subtitle = stringResource(R.string.about_license_desc),
                    onClick = { openUrl("https://github.com/solder3t/timbre-dsp/blob/main/LICENSE") },
                    showDivider = false
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Footer
        Text(
            text = stringResource(R.string.about_copyright),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
private fun AboutFeatureItem(
    icon: ImageVector,
    title: String,
    description: String,
    showDivider: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }

    if (showDivider) {
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 4.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
        )
    }
}

@Composable
private fun AboutLinkItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    showDivider: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
    }

    if (showDivider) {
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 4.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
        )
    }
}
