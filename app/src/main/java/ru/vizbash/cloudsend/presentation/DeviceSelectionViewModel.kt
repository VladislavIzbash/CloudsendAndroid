package ru.vizbash.cloudsend.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.vizbash.cloudsend.domain.AppError
import ru.vizbash.cloudsend.domain.Device
import ru.vizbash.cloudsend.domain.ListTargetDevicesInteractor

@HiltViewModel(assistedFactory = DeviceSelectionViewModel.Factory::class)
class DeviceSelectionViewModel @AssistedInject constructor(
    @Assisted private val fileUri: String?,
    private val listTargetDevicesInteractor: ListTargetDevicesInteractor,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(fileUri: String?): DeviceSelectionViewModel
    }

    sealed class State {
        data object Loading : State()

        data class Loaded(
            val targetDevices: List<Device>,
            val pendingDevice: Device? = null, // TODO: persist this value across process deaths
        ) : State()

        data class Error(
            val error: AppError,
        ) : State()

        data object NoDevices : State()
    }

    private val _state = MutableStateFlow<State>(State.Loading)
    val state = _state.asStateFlow()

    private var refreshJob: Job? = null

    fun onResume() {
        refreshDevices()
    }

    fun onDeviceClick(
        device: Device,
        openFilePicker: () -> Unit,
        navigateToTransfer: (String, Device) -> Unit,
    ) {
        val state = state.value as? State.Loaded ?: return

        if (fileUri != null) {
            // file already picked before this screen was opened
            navigateToTransfer(fileUri, device)
        } else {
            _state.update {
                state.copy(pendingDevice = device)
            }
            openFilePicker()
        }
    }

    fun onRefreshClick() {
        refreshDevices()
    }

    private fun refreshDevices() {
        if (refreshJob?.isActive == true) {
            return
        }

        refreshJob = viewModelScope.launch {
            val devices = listTargetDevicesInteractor().getOrElse {
                _state.value = State.Error(it as AppError)
                return@launch
            }
            if (devices.isEmpty()) {
                _state.value = State.NoDevices
            }

            _state.value = State.Loaded(
                targetDevices = devices.sortedByDescending(Device::isOnline)
            )
        }
    }
}