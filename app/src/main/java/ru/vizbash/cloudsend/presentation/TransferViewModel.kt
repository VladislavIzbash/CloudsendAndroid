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
import ru.vizbash.cloudsend.data.document.DocumentResolver
import ru.vizbash.cloudsend.domain.AppError
import ru.vizbash.cloudsend.domain.Device
import ru.vizbash.cloudsend.domain.transfer.SendFileInteractor
import ru.vizbash.cloudsend.domain.transfer.SendFileTransferRequestInteractor

@HiltViewModel(assistedFactory = TransferViewModel.Factory::class)
class TransferViewModel @AssistedInject constructor(
    @Assisted private val fileUri: String,
    @Assisted private val targetDevice: Device,
    documentResolver: DocumentResolver,
    private val sendFileTransferRequestInteractor: SendFileTransferRequestInteractor,
    private val sendFileInteractor: SendFileInteractor,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(fileUri: String, targetDevice: Device): TransferViewModel
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

        data class Error(
            val error: AppError,
        ) : TransferState()

        data object Done : TransferState()
    }

    private val _state = MutableStateFlow(
        State(
            filename = documentResolver.resolve(fileUri)?.name ?: "?",
            targetDevice = targetDevice.name,
            transferState = TransferState.Initializing,
        )
    )
    val state = _state.asStateFlow()

    private var uploadJob: Job

    init {
        uploadJob = viewModelScope.launch {
            uploadFile()
        }
        uploadJob.invokeOnCompletion {
            println("job completed")
        }
    }

    fun onCancelClick(navigateBack: () -> Unit) {
        uploadJob.cancel()
        navigateBack()
    }

    fun onRetryClick() {
        if (uploadJob.isActive) {
            return
        }

        uploadJob = viewModelScope.launch {
            uploadFile()
        }
    }

    private suspend fun uploadFile() {
        _state.update {
            it.copy(transferState = TransferState.Initializing)
        }

        val transferUuid = sendFileTransferRequestInteractor(
            fileUri = fileUri,
            targetDevice = targetDevice,
        ).getOrElse { e ->
            _state.update {
                it.copy(transferState = TransferState.Error(e as AppError))
            }
            return
        }

        sendFileInteractor(
            fileUri = fileUri,
            transferUuid = transferUuid,
            onUploadProgress = { transferred, total ->
                _state.update {
                    it.copy(
                        transferState = TransferState.InProgress(
                            progress = transferred.toFloat() / total.toFloat(),
                            transferredBytes = transferred,
                            totalBytes = total,
                        )
                    )
                }
            }
        ).getOrElse { e ->
            _state.update {
                it.copy(transferState = TransferState.Error(e as AppError))
            }
            return
        }

        _state.update {
            it.copy(transferState = TransferState.Done)
        }
    }
}