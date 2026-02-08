package ru.vizbash.cloudsend.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class SendRequest {
    @Serializable
    @SerialName("text")
    data class Text(
        @SerialName("content")
        val content: String,
    ) : SendRequest()

    @Serializable
    @SerialName("file")
    data class File(
        @SerialName("filename")
        val filename: String,
        @SerialName("file_size")
        val fileSize: Long,
    ) : SendRequest()
}

@Serializable
data class SendResponse(
    @SerialName("transfer_uuid")
    val transferUuid: String? = null,
)

@Serializable
sealed class SendMessage {
    @Serializable
    @SerialName("text")
    data class Text(
        @SerialName("sender")
        val sender: String,
        @SerialName("content")
        val content: String,
    ) : SendMessage()

    @Serializable
    @SerialName("file")
    data class File(
        @SerialName("sender")
        val sender: String,
        @SerialName("transfer_uuid")
        val transferUuid: String,
        @SerialName("filename")
        val filename: String,
        @SerialName("file_size")
        val fileSize: Long,
    ) : SendMessage()
}
