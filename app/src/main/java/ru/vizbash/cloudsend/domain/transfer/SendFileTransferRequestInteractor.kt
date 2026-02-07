package ru.vizbash.cloudsend.domain.transfer

import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode
import ru.vizbash.cloudsend.data.CloudsendClient
import ru.vizbash.cloudsend.data.ConnectionParamsRepository
import ru.vizbash.cloudsend.data.document.DocumentResolver
import ru.vizbash.cloudsend.data.dto.SendRequest
import ru.vizbash.cloudsend.domain.AppError
import ru.vizbash.cloudsend.domain.handleCommonExceptions
import javax.inject.Inject
import kotlin.uuid.Uuid

private const val TAG = "SendFileTransferRequestInteractor"

class SendFileTransferRequestInteractor @Inject constructor(
    private val documentResolver: DocumentResolver,
    private val cloudsendClient: CloudsendClient,
    private val connectionParamsRepository: ConnectionParamsRepository,
) {
    suspend operator fun invoke(fileUri: String, targetUuid: Uuid): Result<Uuid> {
        return handleCommonExceptions(TAG) {
            val (name, size) = documentResolver.resolve(fileUri)
                ?: return@handleCommonExceptions Result.failure(AppError.DocumentRead)
            try {
                val resp = cloudsendClient.requestTransfer(
                    senderUuid = connectionParamsRepository.get().deviceUuid,
                    targetUuid = targetUuid,
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