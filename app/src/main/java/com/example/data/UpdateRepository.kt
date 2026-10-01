package com.example.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class UpdateCheckState {
    object Idle : UpdateCheckState()
    object Checking : UpdateCheckState()
    data class UpdateAvailable(val info: AppUpdateInfo) : UpdateCheckState()
    object UpToDate : UpdateCheckState()
    data class Error(val message: String) : UpdateCheckState()
    data class Downloading(val progressPercent: Int) : UpdateCheckState()
    data class DownloadCompleted(val apkPath: String) : UpdateCheckState()
}

class UpdateRepository {

    private val _updateInfo = MutableStateFlow(AppUpdateInfo())
    val updateInfo: StateFlow<AppUpdateInfo> = _updateInfo.asStateFlow()

    private val _checkState = MutableStateFlow<UpdateCheckState>(UpdateCheckState.Idle)
    val checkState: StateFlow<UpdateCheckState> = _checkState.asStateFlow()

    private val _autoCheckEnabled = MutableStateFlow(true)
    val autoCheckEnabled: StateFlow<Boolean> = _autoCheckEnabled.asStateFlow()

    suspend fun checkForUpdates(forceSimulateAvailable: Boolean = true): UpdateCheckState {
        _checkState.value = UpdateCheckState.Checking
        delay(1200) // Realistic check delay

        return if (forceSimulateAvailable) {
            val info = _updateInfo.value
            _checkState.value = UpdateCheckState.UpdateAvailable(info)
            UpdateCheckState.UpdateAvailable(info)
        } else {
            _checkState.value = UpdateCheckState.UpToDate
            UpdateCheckState.UpToDate
        }
    }

    suspend fun simulateDownloadUpdate(onComplete: () -> Unit = {}) {
        for (i in 1..100 step 5) {
            _checkState.value = UpdateCheckState.Downloading(i)
            delay(100)
        }
        _checkState.value = UpdateCheckState.DownloadCompleted("/storage/emulated/0/Download/siebrass_v1.2.0.apk")
        onComplete()
    }

    fun setAutoCheckEnabled(enabled: Boolean) {
        _autoCheckEnabled.value = enabled
    }

    fun resetState() {
        _checkState.value = UpdateCheckState.Idle
    }
}
