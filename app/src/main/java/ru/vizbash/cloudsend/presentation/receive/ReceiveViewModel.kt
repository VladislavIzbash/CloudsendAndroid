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
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class ReceiveViewModel @Inject constructor(
    private val cloudsendClient: CloudsendClient,
    private val connectionParamsRepository: ConnectionParamsRepository,
    private val completedTransferDao: CompletedTransferDao,
) : ViewModel() {
    data class State(
        val completedTransfers: List<CompletedTransfer> = listOf(),
        val activeTransfer: ActiveTransfer? = null,
    )

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    private var listenJob: Job? = null

    init {
        viewModelScope.launch {
            completedTransferDao.getAll().collect { entities ->
                _state.update {
                    it.copy(
                        completedTransfers = entities.map { entity ->
                            CompletedTransfer(
                                id = entity.id,
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

    fun onResume() {
        if (listenJob?.isActive == true) {
            return
        }

        listenJob = viewModelScope.launch {
            cloudsendClient.listenIncomingTransfers(connectionParamsRepository.get().deviceUuid).collect { request ->
                if (request is SendMessage.File) {
                }
            }
        }
    }

    fun onPause() {

    }
}