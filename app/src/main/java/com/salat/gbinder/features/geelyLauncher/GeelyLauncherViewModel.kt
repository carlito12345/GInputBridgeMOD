package com.salat.gbinder.features.geelyLauncher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salat.gbinder.APP_ICON_QUALITY
import com.salat.gbinder.APP_ICON_ROUND
import com.salat.gbinder.NATIVE_LAUNCHER_BATCH_SIZE
import com.salat.gbinder.R
import com.salat.gbinder.adb.domain.repository.AdbRepository
import com.salat.gbinder.features.geelyLauncher.entity.GLScreenState
import com.salat.gbinder.mappers.toDisplayIcon
import com.salat.gbinder.util.SystemAppsLightRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class GeelyLauncherViewModel @Inject constructor(
    private val adbRepository: AdbRepository,
    private val systemApps: SystemAppsLightRepository
) : ViewModel() {

    private val _screenType = MutableStateFlow(GLScreenState.LOADING)
    val screenType = _screenType.asStateFlow()

    private val _availableApps = MutableStateFlow<List<GeelyLauncherApp>>(emptyList())
    val availableApps = _availableApps.asStateFlow()

    private val _launcherApps = MutableStateFlow<List<GeelyLauncherApp>>(emptyList())
    val launcherApps = _launcherApps.asStateFlow()

    private val _isApplying = MutableStateFlow(false)
    val isApplying = _isApplying.asStateFlow()

    private val _applyProgress = MutableStateFlow(0f)
    val applyProgress = _applyProgress.asStateFlow()

    private val _applyProgressAnimationDurationMs = MutableStateFlow(APPLY_PROGRESS_ANIMATION_MS)
    val applyProgressAnimationDurationMs = _applyProgressAnimationDurationMs.asStateFlow()

    private val _hasChanges = MutableStateFlow(false)
    val hasChanges = _hasChanges.asStateFlow()

    private val _toastMessage = MutableSharedFlow<Int>()
    val toastMessage = _toastMessage.asSharedFlow()

    private var initialLauncherPackages: List<String> = emptyList()
    private var loadingStarted = false

    fun initialCheck() = viewModelScope.launch(Dispatchers.IO) {
        val withAppStorage = systemApps.isPackageInstalled("com.salat.gappstorage")
                || systemApps.isPackageInstalled("com.geely.appstore")
        setProviderState(withAppStorage)
    }

    fun setProviderState(withAppStorage: Boolean) {
        if (!withAppStorage) {
            loadingStarted = false
            _screenType.value = GLScreenState.NEED_APP_STORAGE
            return
        }

        if (loadingStarted && _screenType.value != GLScreenState.NEED_APP_STORAGE) return

        loadingStarted = true
        _screenType.value = GLScreenState.LOADING
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val currentPackages = adbRepository.getNativeLauncherApps()
                val installedApps = systemApps
                    .getAllApps(APP_ICON_ROUND, true, APP_ICON_QUALITY)
                    .filter { !it.isSystem }
                    .distinctBy { it.packageName }
                    .map {
                        GeelyLauncherApp(
                            packageName = it.packageName,
                            appName = it.appName,
                            iconRef = it.iconRef.toDisplayIcon()
                        )
                    }
                //.sortedBy { it.appName.lowercase() }

                val installedByPackage = installedApps.associateBy { it.packageName }
                val currentApps = currentPackages
                    .distinct()
                    .mapNotNull { installedByPackage[it] }

                // TODO init loading system apps
                withContext(Dispatchers.Main) {
                    _availableApps.value = installedApps
                    initialLauncherPackages = currentApps.map { it.packageName }
                    _launcherApps.value = currentApps
                    _hasChanges.value = false
                    _screenType.value = GLScreenState.BUILDER
                }
            }.onFailure { e ->
                Timber.e(e)
                withContext(Dispatchers.Main) {
                    loadingStarted = false
                    _screenType.value = GLScreenState.NEED_APP_STORAGE
                }
            }
        }
    }

    fun removeApp(packageName: String) {
        if (_isApplying.value) return
        _launcherApps.value = _launcherApps.value.filterNot { it.packageName == packageName }
        updateChangesState()
    }

    fun moveApp(fromIndex: Int, toIndex: Int) {
        if (_isApplying.value) return
        val source = _launcherApps.value
        if (fromIndex !in source.indices || toIndex !in source.indices || fromIndex == toIndex) return
        _launcherApps.value = source.toMutableList().apply {
            add(toIndex, removeAt(fromIndex))
        }
        updateChangesState()
    }

    fun setSelectedApps(packageNames: Set<String>) {
        if (_isApplying.value) return
        val byPackage = _availableApps.value.associateBy { it.packageName }
        val currentPackages = _launcherApps.value.map { it.packageName }
        val availablePackages = _availableApps.value.map { it.packageName }
        val orderedPackages = buildList {
            currentPackages.forEach { packageName ->
                if (packageName in packageNames && packageName !in this) add(packageName)
            }
            availablePackages.forEach { packageName ->
                if (packageName in packageNames && packageName !in this) add(packageName)
            }
        }
        _launcherApps.value = orderedPackages.mapNotNull { byPackage[it] }
        updateChangesState()
    }

    fun applyChanges() {
        if (_isApplying.value || !_hasChanges.value) return
        val snapshot = _launcherApps.value
        _isApplying.value = true
        _applyProgress.value = 0f
        _applyProgressAnimationDurationMs.value = APPLY_PROGRESS_ANIMATION_MS
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val finalApps = snapshot
                    .distinctBy { it.packageName }
                    .map { it.packageName to it.appName }
                val finalPackages = finalApps.map { it.first }.toSet()
                val currentPackages = adbRepository.getNativeLauncherApps()
                val currentPackagesSet = currentPackages.toSet()
                val appsToAdd = finalApps.filter { it.first !in currentPackagesSet }
                val packagesToRemove = currentPackages.filter { it !in finalPackages }
                val changeTicks = if (finalApps.isEmpty()) {
                    1
                } else {
                    getBatchTicks(packagesToRemove.size) + getBatchTicks(appsToAdd.size)
                }
                val totalTicks = GET_APPS_TICKS + changeTicks + RESTART_LAUNCHER_TICKS
                var completedTicks = GET_APPS_TICKS
                _applyProgress.value = completedTicks.toFloat() / totalTicks
                val updateApplyProgress: suspend () -> Unit = {
                    completedTicks += 1
                    _applyProgress.value = completedTicks.toFloat() / totalTicks
                }

                if (finalApps.isEmpty()) {
                    adbRepository.clearCarLauncherApps()
                    updateApplyProgress()
                } else {
                    if (packagesToRemove.isNotEmpty()) {
                        adbRepository.removeAppsFromCarLauncher(
                            packagesToRemove,
                            updateApplyProgress
                        )
                    }
                    if (appsToAdd.isNotEmpty()) {
                        adbRepository.addAppsToCarLauncher(
                            appsToAdd,
                            onProgressTick = updateApplyProgress
                        )
                    }
                }
                adbRepository.restartLauncher3()
                _applyProgressAnimationDurationMs.value = APPLY_FINISH_DELAY_MS.toInt()
                updateApplyProgress()
                delay(APPLY_FINISH_DELAY_MS)
                withContext(Dispatchers.Main) {
                    initialLauncherPackages = snapshot.map { it.packageName }
                    _hasChanges.value = false
                    _toastMessage.emit(R.string.geely_launcher_apply_success)
                }
            }.onFailure { e ->
                Timber.e(e)
                withContext(Dispatchers.Main) {
                    _toastMessage.emit(R.string.geely_launcher_apply_error)
                }
            }
            withContext(Dispatchers.Main) {
                _isApplying.value = false
                _applyProgress.value = 0f
                _applyProgressAnimationDurationMs.value = APPLY_PROGRESS_ANIMATION_MS
            }
        }
    }

    fun restartLauncher() = viewModelScope.launch(Dispatchers.IO) {
        adbRepository.restartLauncher3()
        Timber.d("Restart launcher")
    }

    private fun updateChangesState() {
        _hasChanges.value =
            _launcherApps.value.map { it.packageName }.toSet() != initialLauncherPackages.toSet()
    }

    private fun getBatchTicks(size: Int): Int {
        return if (size == 0) 0 else (size + NATIVE_LAUNCHER_BATCH_SIZE - 1) / NATIVE_LAUNCHER_BATCH_SIZE
    }

    private companion object {
        const val GET_APPS_TICKS = 1
        const val RESTART_LAUNCHER_TICKS = 1
        const val APPLY_FINISH_DELAY_MS = 3500L
        const val APPLY_PROGRESS_ANIMATION_MS = 180
    }
}
