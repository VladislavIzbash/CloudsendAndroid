package ru.vizbash.cloudsend.domain.transfer

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.vizbash.cloudsend.data.network.CloudsendClient
import ru.vizbash.cloudsend.data.network.dto.SendMessage
import ru.vizbash.cloudsend.data.persistence.db.CompletedTransferDao
import ru.vizbash.cloudsend.data.persistence.db.CompletedTransferEntity
import ru.vizbash.cloudsend.data.persistence.settings.AppSettings
import ru.vizbash.cloudsend.data.persistence.settings.PreferencesSettingsRepository
import ru.vizbash.cloudsend.domain.AppError
import ru.vizbash.cloudsend.domain.handleCommonExceptions
import java.io.File
import java.time.Instant
import javax.inject.Inject

private const val TAG = "ReceiveFileInteractor"

class ReceiveFileInteractor @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val cloudsendClient: CloudsendClient,
    private val settingsRepository: PreferencesSettingsRepository,
    private val completedTransferDao: CompletedTransferDao,
) {
    suspend operator fun invoke(
        request: SendMessage.File,
        onDownloadProgress: (transferred: Long, total: Long) -> Unit,
    ): Result<Unit> {
        val fileUri = downloadFile(request, onDownloadProgress).getOrElse {
            return Result.failure(it)
        }

        completedTransferDao.insert(
            CompletedTransferEntity(
                filename = request.filename,
                timestamp = Instant.now().toEpochMilli() / 1000,
                fileUri = fileUri?.toString(),
                transferUuid = request.transferUuid,
            )
        )
        return Result.success(Unit)
    }

    private suspend fun downloadFile(
        request: SendMessage.File,
        onDownloadProgress: (transferred: Long, total: Long) -> Unit,
    ): Result<Uri?> = withContext(Dispatchers.IO) {
        val (saveUri, saveFile) = try {
            val fileExt = request.filename.substringBeforeLast(".", "")
            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExt.lowercase())
                ?: "application/octet-stream"

            val saveDirUri = settingsRepository.get(AppSettings.SaveDirectoryUri)?.let(Uri::parse)
            if (saveDirUri != null) {
                Pair(createFileInProvidedDirUri(saveDirUri, request.filename, mimeType), null)
            } else {
                createFileInDownloads(request.filename, mimeType)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create output file: ${e.message}")
            e.printStackTrace()
            return@withContext Result.failure(AppError.DocumentWrite())
        }

        if (saveUri == null && saveFile == null) {
            return@withContext Result.failure(AppError.DocumentWrite())
        }

        return@withContext handleCommonExceptions {
            try {
                cloudsendClient.downloadFile(
                    transferUuid = request.transferUuid,
                    onDownloadProgress = { onDownloadProgress(it, request.fileSize) },
                ).use { inputStream ->
                    when {
                        saveUri != null -> {
                            context.contentResolver.openOutputStream(saveUri)?.use {
                                inputStream.copyTo(it)
                            }
                        }

                        saveFile != null -> {
                            saveFile.outputStream().use {
                                inputStream.copyTo(it)
                            }
                        }
                    }
                }
                Result.success(saveUri)
            } catch (e: Exception) {
                Log.i(TAG, "Caught exception while download, removing output file")
                saveUri?.let {
                    context.contentResolver.delete(saveUri, null, null)
                }
                saveFile?.delete()
                throw e
            }
        }
    }

    private fun createFileInProvidedDirUri(
        dirUri: Uri,
        filename: String,
        mimeType: String,
    ): Uri? {
        val fileUri = DocumentsContract.createDocument(
            context.contentResolver,
            dirUri,
            mimeType,
            filename.substringBeforeLast("."),
        )
        if (fileUri == null) {
            Log.e(TAG, "Failed to create file in provided uri: $dirUri")
        }
        return fileUri
    }

    private fun createFileInDownloads(filename: String, mimeType: String): Pair<Uri?, File?> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ (API 29+) - Use MediaStore
            Pair(createViaMediaStore(filename, mimeType), null)
        } else {
            // Android 9 and below - Direct file access
            Pair(null, createViaLegacyStorage(filename))
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun createViaMediaStore(filename: String, mimeType: String): Uri? {
        val resolver = context.contentResolver

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename.substringBeforeLast("."))
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
        return resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
    }

    private fun createViaLegacyStorage(fileName: String): File {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs()
        }

        return File(downloadsDir, fileName)
    }
}