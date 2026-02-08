package ru.vizbash.cloudsend.domain

import ru.vizbash.cloudsend.data.network.CloudsendClient
import ru.vizbash.cloudsend.data.persistence.ConnectionParamsRepository
import ru.vizbash.cloudsend.data.persistence.db.DeviceDao
import ru.vizbash.cloudsend.data.persistence.db.DeviceEntity
import javax.inject.Inject

private const val TAG = "ListTargetDevicesInteractor"

class ListTargetDevicesInteractor @Inject constructor(
    private val cloudsendClient: CloudsendClient,
    private val connectionParamsRepository: ConnectionParamsRepository,
    private val deviceDao: DeviceDao,
) {
    suspend operator fun invoke(): Result<List<Device>> {
        return handleCommonExceptions(TAG) {
            val selfUuid = connectionParamsRepository.get().deviceUuid
            val devices = cloudsendClient.listDevices()
                .filter { it.uuid != selfUuid }
                .map { resp ->
                    Device(
                        resp.name,
                        resp.uuid,
                        resp.available,
                    )
                }

            deviceDao.updateAll(
                devices.map {
                    DeviceEntity(it.uuid, it.name)
                }
            )
            Result.success(devices)
        }
    }
}