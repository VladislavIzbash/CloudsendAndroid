package ru.vizbash.cloudsend.presentation.receive

import ru.vizbash.cloudsend.domain.AppError
import java.time.LocalDateTime

data class ActiveTransfer(
    val transferUuid: String,
    val filename: String,
    val progress: Float,
    val date: LocalDateTime,
)

data class CompletedTransfer(
    val id: Int,
    val transferUuid: String,
    val filename: String,
    val date: LocalDateTime,
    val error: AppError?,
)