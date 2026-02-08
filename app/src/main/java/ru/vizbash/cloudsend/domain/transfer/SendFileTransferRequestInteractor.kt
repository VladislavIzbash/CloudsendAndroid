package ru.vizbash.cloudsend.domain.transfer

import android.content.Context
import androidx.core.content.pm.ShortcutManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode
import ru.vizbash.cloudsend.data.document.DocumentResolver
import ru.vizbash.cloudsend.data.network.CloudsendClient
import ru.vizbash.cloudsend.data.network.dto.SendRequest
import ru.vizbash.cloudsend.data.persistence.ConnectionParamsRepository
import ru.vizbash.cloudsend.domain.AppError
import ru.vizbash.cloudsend.domain.Device
import ru.vizbash.cloudsend.domain.handleCommonExceptions
import ru.vizbash.cloudsend.util.SEND_CAPABILITY
import ru.vizbash.cloudsend.util.createDeviceShortcut
import javax.inject.Inject

private const val TAG = "SendFileTransferRequestInteractor"

class SendFileTransferRequestInteractor @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val documentResolver: DocumentResolver,
    private val cloudsendClient: CloudsendClient,
    private val connectionParamsRepository: ConnectionParamsRepository,
) {
    suspend operator fun invoke(fileUri: String, targetDevice: Device): Result<String> {
        return handleCommonExceptions(TAG) {
            val (name, size) = documentResolver.resolve(fileUri)
                ?: return@handleCommonExceptions Result.failure(AppError.DocumentRead)
            try {
                ShortcutManagerCompat.pushDynamicShortcut(
                    context,
                    createDeviceShortcut(
                        context,
                        targetDevice.uuid,
                        targetDevice.name,
                        SEND_CAPABILITY,
                    )
                )

                val resp = cloudsendClient.requestTransfer(
                    senderUuid = connectionParamsRepository.get().deviceUuid,
                    targetUuid = targetDevice.uuid,
                    request = SendRequest.File(name, size),
                )

                Result.success(resp.transferUuid!!)
            } catch (e: ClientRequestException) {
                when (e.response.status) {
                    HttpStatusCode.Forbidden -> Result.failure(AppError.TransferRejected)
                    HttpStatusCode.Gone -> Result.failure(AppError.TargetOffline)
                    else -> throw e
                }
            }
        }
    }
}