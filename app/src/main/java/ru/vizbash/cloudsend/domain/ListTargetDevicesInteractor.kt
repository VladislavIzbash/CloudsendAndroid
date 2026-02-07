package ru.vizbash.cloudsend.domain

import android.util.Log
import ru.vizbash.cloudsend.data.CloudsendClient
import ru.vizbash.cloudsend.data.ConnectionParamsRepository
import javax.inject.Inject

private val TAG = "ListTargetDevicesInteractor"

class ListTargetDevicesInteractor @Inject constructor(
    private val cloudsendClient: CloudsendClient,
    private val connectionParamsRepository: ConnectionParamsRepository,
) {
    suspend operator fun invoke(): List<Device>? {
        return try {
            val selfUuid = connectionParamsRepository.get().deviceUuid
            cloudsendClient.listDevices()
                .filter { it.uuid != selfUuid }
                .map { resp ->
                    Device(
                        resp.name,
                        resp.uuid,
                        resp.available,
                    )
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading device list: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}