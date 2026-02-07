package ru.vizbash.cloudsend.domain

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException
import ru.vizbash.cloudsend.R

@Suppress("JavaIoSerializableObjectMustHaveReadResolve")
sealed class AppError : Throwable() {

    abstract fun message(context: Context): String

    object General : AppError() {
        override fun message(context: Context) =
            context.getString(R.string.error_general)
    }

    object DocumentRead : AppError() {
        override fun message(context: Context) =
            context.getString(R.string.error_document_read)
    }

    object Network : AppError() {
        override fun message(context: Context) =
            context.getString(R.string.error_network)
    }

    object TransferRejected : AppError() {
        override fun message(context: Context) =
            context.getString(R.string.error_transfer_rejected)
    }

    object TargetOffline : AppError() {
        override fun message(context: Context) =
            context.getString(R.string.error_target_offline)
    }
}

inline fun <T> handleCommonExceptions(logTag: String? = null, block: () -> Result<T>): Result<T> {
    return try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: IOException) {
        Log.e(logTag, "Network error: ${e.message}")
        e.printStackTrace()
        Result.failure(AppError.Network)
    } catch (e: Exception) {
        Log.e(logTag, "Error: $e")
        e.printStackTrace()
        Result.failure(AppError.General)
    }
}
