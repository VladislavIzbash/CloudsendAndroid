package ru.vizbash.cloudsend.presentation

import androidx.lifecycle.ViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel(assistedFactory = TransferViewModel.Factory::class)
class TransferViewModel @AssistedInject constructor(
    @Assisted private val fileUri: String,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(fileUri: String): TransferViewModel
    }

    data class State(
        val filename: String,
        val targetDevice: String,
        val transferState: TransferState,
    )

    sealed class TransferState {
        data object Initializing : TransferState()

        data class InProgress(
            val progress: Float,
            val transferredBytes: Long,
            val totalBytes: Long,
        ) : TransferState()

        data object Error : TransferState()

        data object Done : TransferState()
    }

    private val _state = MutableStateFlow(
        State(
            filename = "",
            targetDevice = "",
            transferState = TransferState.InProgress(
                progress = 0f,
                transferredBytes = 0,
                totalBytes = 0,
            )
        )
    )
    val state = _state.asStateFlow()
}