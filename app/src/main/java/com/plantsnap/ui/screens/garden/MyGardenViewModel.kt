package com.plantsnap.ui.screens.garden

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plantsnap.data.storage.PlantImageUrlResolver
import com.plantsnap.data.sync.SavedPlantSyncManager
import com.plantsnap.domain.repository.CareTaskRepository
import com.plantsnap.domain.repository.SavedPlantRepository
import com.plantsnap.ui.screens.identify.camera.CapturedPhotosHolder
import com.plantsnap.ui.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

data class SavedPlantUi(
    val plant: com.plantsnap.domain.models.SavedPlant,
    val displayImageUrl: String?,
)

@HiltViewModel
class MyGardenViewModel @Inject constructor(
    private val repo: SavedPlantRepository,
    private val careTaskRepository: CareTaskRepository,
    private val imageUrlResolver: PlantImageUrlResolver,
    private val photosHolder: CapturedPhotosHolder,
    private val syncManager: SavedPlantSyncManager,
) : ViewModel() {

    private companion object {
        const val TAG = "MyGardenVM"
    }

    private val _isWateringAll = MutableStateFlow(false)
    val isWateringAll: StateFlow<Boolean> = _isWateringAll.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val plants: StateFlow<UiState<List<SavedPlantUi>>> =
        repo.observeAll()
            .mapLatest { saved ->
                val resolved = imageUrlResolver.resolveAll(saved.map { it.plant.imageUrl })
                UiState.Success(saved.map { sp ->
                    SavedPlantUi(
                        plant = sp,
                        displayImageUrl = sp.plant.imageUrl?.let { resolved[it] },
                    )
                })
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    @OptIn(ExperimentalCoroutinesApi::class)
    val dueTasks: StateFlow<List<CareTaskUi>> =
        careTaskRepository.observeDueBy(endOfTodayLocalMillis())
            .mapLatest { rows ->
                val resolved = imageUrlResolver.resolveAll(rows.map { it.plantImageUrl })
                rows.map { it.toUi(resolvedImageUrl = it.plantImageUrl?.let { url -> resolved[url] }) }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun resetIdentifyFlow() {
        photosHolder.clear()
    }

    fun markTaskDone(taskId: String) {
        viewModelScope.launch {
            try {
                careTaskRepository.markCompleted(taskId)
            } catch (e: Exception) {
                Log.w(TAG, "markTaskDone failed for $taskId", e)
            }
        }
    }

    fun setWateredToday(savedPlantId: String, watered: Boolean) {
        viewModelScope.launch {
            try {
                val ts = if (watered) System.currentTimeMillis() else null
                repo.updateLastWatered(savedPlantId, ts)
                runCatching { syncManager.syncPending() }
                    .onFailure { Log.w(TAG, "syncPending failed", it) }
            } catch (e: Exception) {
                Log.w(TAG, "setWateredToday failed", e)
            }
        }
    }

    fun setAllWateredToday(savedPlantIds: List<String>, watered: Boolean) {
        if (savedPlantIds.isEmpty()) return
        if (!_isWateringAll.compareAndSet(expect = false, update = true)) return
        viewModelScope.launch {
            try {
                val ts: Long? = if (watered) System.currentTimeMillis() else null
                repo.updateLastWateredBulk(savedPlantIds, ts)
                runCatching { syncManager.syncPending() }
                    .onFailure { Log.w(TAG, "setAllWateredToday syncPending failed", it) }
            } catch (e: Exception) {
                Log.w(TAG, "setAllWateredToday failed", e)
            } finally {
                _isWateringAll.value = false
            }
        }
    }

    private fun endOfTodayLocalMillis(zoneId: ZoneId = ZoneId.systemDefault()): Long =
        LocalDate.now(zoneId)
            .atTime(LocalTime.MAX)
            .atZone(zoneId)
            .toInstant()
            .toEpochMilli()
}
