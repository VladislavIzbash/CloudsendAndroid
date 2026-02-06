package ru.vizbash.cloudsend.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.vizbash.cloudsend.domain.Device
import ru.vizbash.cloudsend.domain.ListTargetDevicesInteractor

@HiltViewModel
class SendViewModel @Inject constructor(
    private val listTargetDevicesInteractor: ListTargetDevicesInteractor,
) : ViewModel() {
    sealed class State {
        data object Loading : State()

        data class Loaded(
            val targetDevices: List<Device>,
            val pendingDevice: Device? = null,
        ) : State()

        data object Error : State()

        data object NoDevices : State()
    }

    private val _state = MutableStateFlow<State>(State.Loading)
    val state = _state.asStateFlow()

    private var refreshJob: Job? = null

    fun onResume() {
        if (refreshJob != null) {
            return
        }

        refreshJob = viewModelScope.launch {
            refreshDevices()
        }
    }

    fun onDeviceClick(
        device: Device,
        openFilePicker: () -> Unit,
    ) {
        val state = state.value as? State.Loaded ?: return

        _state.update {
            state.copy(pendingDevice = device)
        }
        openFilePicker()
    }

    private suspend fun refreshDevices() {
        val devices = listTargetDevicesInteractor()
        _state.value = when {
            devices == null -> State.Error
            devices.isEmpty() -> State.NoDevices
            else -> {
                State.Loaded(
                    targetDevices = devices.sortedByDescending(Device::isOnline)
                )
            }
        }
    }
}