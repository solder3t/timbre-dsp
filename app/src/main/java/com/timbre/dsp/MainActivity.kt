package com.timbre.dsp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Slider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.timbre.dsp.theme.TimbreTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val routingManager = RoutingManager(this)
        routingManager.detectAndApplyMode()

        setContent {
            TimbreTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DSPControlPanel(
                        onStartService = {
                            val intent = Intent(this, DSPForegroundService::class.java)
                            startForegroundService(intent)
                        },
                        onStopService = {
                            val intent = Intent(this, DSPForegroundService::class.java)
                            stopService(intent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DSPControlPanel(onStartService: () -> Unit, onStopService: () -> Unit) {
    var isServiceRunning by remember { mutableStateOf(false) }
    
    // Store gains for 10 bands (-15dB to +15dB)
    val bandGains = remember { mutableStateListOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f) }
    val bandLabels = listOf("31", "62", "125", "250", "500", "1k", "2k", "4k", "8k", "16k")

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(text = "Timbre DSP Settings", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 16.dp, top = 32.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { 
                onStartService()
                isServiceRunning = true
            }, enabled = !isServiceRunning) {
                Text("Start Service")
            }
            Button(onClick = { 
                onStopService()
                isServiceRunning = false
            }, enabled = isServiceRunning) {
                Text("Stop Service")
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        Text(text = "Graphic Equalizer", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        // Display horizontal sliders for now (could be vertical with rotation or custom modifiers)
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(10) { index ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "${bandLabels[index]} Hz",
                        modifier = Modifier.width(60.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Slider(
                        value = bandGains[index],
                        onValueChange = { newValue ->
                            bandGains[index] = newValue
                            DSPEngine.setBandGain(index, newValue)
                        },
                        valueRange = -15f..15f,
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                    )
                    
                    Text(
                        text = String.format("%.1f dB", bandGains[index]),
                        modifier = Modifier.width(60.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}
