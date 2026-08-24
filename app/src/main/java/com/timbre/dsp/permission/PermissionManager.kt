package com.timbre.dsp.permission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.os.Process
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.timbre.dsp.model.PermissionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class PermissionManager(private val context: Context) {

    private val _status = MutableStateFlow(PermissionStatus())
    val status: StateFlow<PermissionStatus> = _status.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO)

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        checkShizukuStatus()
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        checkShizukuStatus()
    }

    private val permissionResultListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        if (requestCode == SHIZUKU_PERMISSION_REQUEST_CODE) {
            val granted = grantResult == PackageManager.PERMISSION_GRANTED
            Log.i(TAG, "Shizuku permission result: granted=$granted")
            if (granted) {
                scope.launch {
                    grantDumpPermissionViaShizuku()
                    refreshStatus()
                }
            } else {
                refreshStatus()
            }
        }
    }

    init {
        try {
            Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)
            Shizuku.addBinderDeadListener(binderDeadListener)
            Shizuku.addRequestPermissionResultListener(permissionResultListener)
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to register Shizuku listeners", e)
        }
        refreshStatus()
    }

    fun refreshStatus() {
        scope.launch {
            val shizukuRunning = isShizukuAvailable()
            val shizukuGranted = if (shizukuRunning) {
                try {
                    Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
                } catch (e: Throwable) {
                    false
                }
            } else false

            val rootAvailable = checkRootAvailable()
            val rootGranted = if (rootAvailable) testRootAccess() else false

            val dumpGranted = context.checkPermission(
                Manifest.permission.DUMP,
                Process.myPid(),
                Process.myUid()
            ) == PackageManager.PERMISSION_GRANTED

            val notifAccess = isNotificationListenerEnabled()
            val batteryIgnored = isBatteryOptimizationIgnored()
            val postNotifGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }

            val recordAudioGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED

            _status.value = PermissionStatus(
                isShizukuRunning = shizukuRunning,
                hasShizukuPermission = shizukuGranted,
                isRootAvailable = rootAvailable,
                hasRootPermission = rootGranted,
                hasDumpPermission = dumpGranted,
                hasNotificationAccess = notifAccess,
                isBatteryOptimizationIgnored = batteryIgnored,
                hasPostNotificationPermission = postNotifGranted,
                hasRecordAudioPermission = recordAudioGranted
            )
        }
    }

    fun requestShizukuPermission() {
        try {
            if (Shizuku.getVersion() < 11) {
                Log.w(TAG, "Shizuku version is older than 11")
                return
            }
            if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Shizuku permission already granted")
                scope.launch {
                    grantDumpPermissionViaShizuku()
                    refreshStatus()
                }
            } else if (Shizuku.shouldShowRequestPermissionRationale()) {
                Shizuku.requestPermission(SHIZUKU_PERMISSION_REQUEST_CODE)
            } else {
                Shizuku.requestPermission(SHIZUKU_PERMISSION_REQUEST_CODE)
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Error requesting Shizuku permission", e)
        }
    }

    suspend fun grantDumpPermissionViaShizuku(): Boolean = withContext(Dispatchers.IO) {
        if (!isShizukuAvailable() || Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
            return@withContext false
        }
        try {
            val cmd = arrayOf("pm", "grant", context.packageName, Manifest.permission.DUMP)
            val method = Shizuku::class.java.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java,
                Array<String>::class.java,
                String::class.java
            )
            method.isAccessible = true
            val process = method.invoke(null, cmd, null, null) as? java.lang.Process
            val exitCode = process?.waitFor() ?: -1
            Log.i(TAG, "Granted DUMP via Shizuku exitCode=$exitCode")
            refreshStatus()
            exitCode == 0
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to grant DUMP via Shizuku", e)
            false
        }
    }

    suspend fun grantDumpPermissionViaRoot(): Boolean = withContext(Dispatchers.IO) {
        try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "pm grant ${context.packageName} ${Manifest.permission.DUMP}"))
            val exitCode = process.waitFor()
            Log.i(TAG, "Granted DUMP via Root exitCode=$exitCode")
            refreshStatus()
            exitCode == 0
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to grant DUMP via Root", e)
            false
        }
    }

    fun openNotificationAccessSettings(context: Context) {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    fun requestIgnoreBatteryOptimization(context: Context) {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${context.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            val fallback = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(fallback)
        }
    }

    private fun isShizukuAvailable(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (e: Throwable) {
            false
        }
    }

    private fun checkShizukuStatus() {
        refreshStatus()
    }

    private fun checkRootAvailable(): Boolean {
        val paths = arrayOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/system/sd/xbin/su",
            "/vendor/bin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/bin/.ext/.su"
        )
        return paths.any { File(it).exists() } || canExecuteSu()
    }

    private fun canExecuteSu(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("which", "su"))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val line = reader.readLine()
            process.waitFor() == 0 && !line.isNullOrBlank()
        } catch (e: Throwable) {
            false
        }
    }

    private fun testRootAccess(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = reader.readLine() ?: ""
            process.waitFor() == 0 && output.contains("uid=0")
        } catch (e: Throwable) {
            false
        }
    }

    private fun isNotificationListenerEnabled(): Boolean {
        val listeners = NotificationManagerCompat.getEnabledListenerPackages(context)
        return listeners.contains(context.packageName)
    }

    private fun isBatteryOptimizationIgnored(): Boolean {
        val pm = context.getSystemService(PowerManager::class.java) ?: return false
        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    companion object {
        private const val TAG = "PermissionManager"
        const val SHIZUKU_PERMISSION_REQUEST_CODE = 4201
        const val ADB_DUMP_COMMAND = "adb shell pm grant com.timbre.dsp android.permission.DUMP"
    }
}
