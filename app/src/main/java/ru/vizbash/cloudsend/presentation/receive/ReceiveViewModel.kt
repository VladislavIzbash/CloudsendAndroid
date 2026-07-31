package ru.vizbash.cloudsend.presentation.receive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.vizbash.cloudsend.data.network.CloudsendClient
import ru.vizbash.cloudsend.data.network.dto.SendMessage
import ru.vizbash.cloudsend.data.persistence.ConnectionParamsRepository
import ru.vizbash.cloudsend.data.persistence.db.CompletedTransferDao
import ru.vizbash.cloudsend.data.persistence.settings.AppSettings
import ru.vizbash.cloudsend.data.persistence.settings.PreferencesSettingsRepository
import ru.vizbash.cloudsend.domain.transfer.ReceiveFileInteractor
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class ReceiveViewModel @Inject constructor(
    private val cloudsendClient: CloudsendClient,
    private val connectionParamsRepository: ConnectionParamsRepository,
    private val completedTransferDao: CompletedTransferDao,
    private val receiveFileInteractor: ReceiveFileInteractor,
    private val settingsRepository: PreferencesSettingsRepository,
) : ViewModel() {
    data class TransferConfirmation(
        val filename: String,
        val fileSize: Long,
        val sender: String,
    )

    data class State(
        val completedTransfers: List<CompletedTransfer> = listOf(),
        val activeTransfer: ActiveTransfer? = null,
        val pendingConfirmation: TransferConfirmation? = null,
    )

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    private var listenJob: Job? = null

    private var pendingRequest: SendMessage.File? = null

    init {
        viewModelScope.launch {
            listenCompletedTransfers()
        }
    }

    fun onResume() {
        if (listenJob?.isActive == true) {
            return
        }

        listenJob = viewModelScope.launch {
            cloudsendClient.listenIncomingTransfers(connectionParamsRepository.get().deviceUuid)
                .collect {
                    handleTransferRequest(it)
                }
        }
    }

    fun onPause() {
        listenJob?.cancel()
    }

    fun onOpenClick(transfer: CompletedTransfer) {
        // TODO
    }

    fun onCancelClick(transfer: ActiveTransfer) {

    }

    fun onAcceptClick() {
        pendingRequest?.let { request ->
            pendingRequest = null
            _state.update {
                it.copy(pendingConfirmation = null)
            }
            viewModelScope.launch {
                acceptTransferRequest(request)
            }
        }
    }

    fun onRejectClick() {
        pendingRequest?.let { request ->
            pendingRequest = null
            _state.update {
                it.copy(pendingConfirmation = null)
            }
            viewModelScope.launch {
                cloudsendClient.rejectTransfer(request.transferUuid)
            }
        }
    }

    private suspend fun handleTransferRequest(request: SendMessage) {
        if (settingsRepository.get(AppSettings.AutoAcceptTransfers)) {
            acceptTransferRequest(request)
        } else if (request is SendMessage.File) {
            pendingRequest = request

            // TODO: cache names
            val senderName = cloudsendClient.listDevices().find { it.uuid == request.sender }?.name
            _state.update {
                it.copy(
                    pendingConfirmation = TransferConfirmation(
                        filename = request.filename,
                        fileSize = request.fileSize,
                        sender = senderName ?: request.sender
                    )
                )
            }
        }
    }

    private suspend fun acceptTransferRequest(request: SendMessage) {
        if (request is SendMessage.File) {
            val activeTransfer = ActiveTransfer(
                transferUuid = request.transferUuid,
                filename = request.filename,
                progress = 0f,
                date = LocalDateTime.now(),
            )
            _state.update {
                it.copy(activeTransfer = activeTransfer)
            }
            receiveFileInteractor(request) { transferred, total ->
                val progress = transferred.toFloat() / total.toFloat()
                _state.update {
                    it.copy(activeTransfer = activeTransfer.copy(progress = progress))
                }
            }
            _state.update {
                it.copy(activeTransfer = null)
            }
        }
    }

    private suspend fun listenCompletedTransfers() {
        completedTransferDao.getAllSortedByTime().collect { entities ->
            _state.update {
                it.copy(
                    completedTransfers = entities.map { entity ->
                        CompletedTransfer(
                            id = entity.id,
                            transferUuid = entity.transferUuid,
                            filename = entity.filename,
                            date = Instant.ofEpochSecond(entity.timestamp)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDateTime(),
                            error = null,
                        )
                    }
                )
            }
        }
    }
}