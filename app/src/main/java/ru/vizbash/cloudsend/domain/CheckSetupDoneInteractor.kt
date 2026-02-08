package ru.vizbash.cloudsend.domain

import ru.vizbash.cloudsend.data.persistence.ConnectionParamsRepository
import javax.inject.Inject

class CheckSetupDoneInteractor @Inject constructor(
    private val connectionParamsRepository: ConnectionParamsRepository,
) {
    operator fun invoke(): Boolean {
        return connectionParamsRepository.isInitialized
    }
}