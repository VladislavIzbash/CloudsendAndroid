package ru.vizbash.cloudsend.domain.transfer

import android.content.Context
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import ru.vizbash.cloudsend.data.network.CloudsendClient
import ru.vizbash.cloudsend.data.document.DocumentResolver
import ru.vizbash.cloudsend.domain.AppError
import ru.vizbash.cloudsend.domain.handleCommonExceptions
import javax.inject.Inject
import kotlin.uuid.Uuid

private const val TAG = "SendFileInteractor"

class SendFileInteractor @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val documentResolver: DocumentResolver,
    private val cloudsendClient: CloudsendClient,
) {
    suspend operator fun invoke(
        fileUri: String,
        transferUuid: Uuid,
        onUploadProgress: (transferred: Long, total: Long) -> Unit,
    ): Result<Unit> {
        return handleCommonExceptions(TAG) {
            val fileSize = documentResolver.resolve(fileUri)?.size
                ?: return@handleCommonExceptions Result.failure(AppError.DocumentRead)

            context.contentResolver.openInputStream(fileUri.toUri()).use { inputStream ->
                cloudsendClient.uploadFile(
                    transferUuid = transferUuid,
                    inputStream = inputStream!!.buffered(),
                    onUploadProgress = {
                        onUploadProgress(it, fileSize)
                    },
                )
            }
            Result.success(Unit)
        }
    }
}