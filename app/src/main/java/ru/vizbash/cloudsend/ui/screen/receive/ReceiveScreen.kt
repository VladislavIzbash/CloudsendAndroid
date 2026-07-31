@file:OptIn(ExperimentalTime::class)

package ru.vizbash.cloudsend.ui.screen.receive

import android.text.format.DateFormat
import android.text.format.Formatter
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import ru.vizbash.cloudsend.R
import ru.vizbash.cloudsend.domain.AppError
import ru.vizbash.cloudsend.presentation.receive.ActiveTransfer
import ru.vizbash.cloudsend.presentation.receive.CompletedTransfer
import ru.vizbash.cloudsend.presentation.receive.ReceiveViewModel
import ru.vizbash.cloudsend.presentation.receive.ReceiveViewModel.State
import ru.vizbash.cloudsend.ui.component.CenteredColumnLayout
import ru.vizbash.cloudsend.ui.component.CenteredColumnLayoutArt
import ru.vizbash.cloudsend.ui.component.ListHeader
import ru.vizbash.cloudsend.ui.theme.CloudSendTheme
import ru.vizbash.cloudsend.ui.util.FileIconResolver
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import kotlin.time.ExperimentalTime

@Composable
fun ReceiveScreen(
    viewModel: ReceiveViewModel,
    modifier: Modifier = Modifier,
) {
    LifecycleResumeEffect(viewModel) {
        viewModel.onResume()

        onPauseOrDispose {
            viewModel.onPause()
        }
    }

    ReceiveScreen(
        modifier = modifier,
        state = viewModel.state.collectAsState().value,
        onOpenClick = viewModel::onOpenClick,
        onCancelClick = viewModel::onCancelClick,
        onAcceptClick = viewModel::onAcceptClick,
        onRejectClick = viewModel::onRejectClick,
    )
}

@Composable
private fun ReceiveScreen(
    modifier: Modifier = Modifier,
    state: State,
    onOpenClick: (CompletedTransfer) -> Unit,
    onCancelClick: (ActiveTransfer) -> Unit,
    onAcceptClick: () -> Unit,
    onRejectClick: () -> Unit,
) {
    if (state.activeTransfer != null || state.completedTransfers.isNotEmpty()) {
        ReceiveScreenContent(
            modifier,
            state,
            onOpenClick,
            onCancelClick,
            onAcceptClick,
            onRejectClick,
        )
    } else {
        CenteredColumnLayout(
            modifier = modifier,
            image = {
                CenteredColumnLayoutArt(painterResource(R.drawable.list_alt))
            },
            title = stringResource(R.string.screen_receive__empty_title),
            text = stringResource(R.string.screen_receive__empty_text)
        )
    }
}

@Composable
private fun ReceiveScreenContent(
    modifier: Modifier = Modifier,
    state: State,
    onOpenClick: (CompletedTransfer) -> Unit,
    onCancelClick: (ActiveTransfer) -> Unit,
    onAcceptClick: () -> Unit,
    onRejectClick: () -> Unit,
) {
    LazyColumn(modifier) {
        state.pendingConfirmation?.let { request ->
            item(key = "banner") {
                ConfirmationBanner(
                    sender = request.sender,
                    filename = request.filename,
                    fileSize = request.fileSize,
                    onAcceptClick = onAcceptClick,
                    onRejectClick = onRejectClick,
                )
            }
        }

        state.activeTransfer?.let { transfer ->
            item(key = "header1") {
                ListHeader(
                    modifier = Modifier.height(32.dp),
                    text = stringResource(R.string.screen_receive__header_active_transfer),
                )
            }

            item(key = state.activeTransfer.transferUuid) {
                TransferItem(
                    modifier = Modifier.animateItem(),
                    progress = { transfer.progress },
                    filename = transfer.filename,
                    actionText = stringResource(R.string.screen_receive__transfer_action_cancel),
                    onActionClick = { onCancelClick(transfer) },
                    date = transfer.date,
                    error = null,
                )
            }
        }

        if (state.completedTransfers.isNotEmpty()) {
            item(key = "header2") {
                ListHeader(
                    modifier = Modifier.height(32.dp),
                    text = stringResource(R.string.screen_receive__header_completed_transfers),
                )
            }

            items(
                items = state.completedTransfers,
                key = CompletedTransfer::transferUuid,
            ) { transfer ->
                val actionText = if (transfer.error != null) {
                    stringResource(R.string.screen_receive__transfer_action_dismiss)
                } else {
                    stringResource(R.string.screen_receive__transfer_action_open)
                }

                TransferItem(
                    modifier = Modifier.animateItem(),
                    progress = { 1f },
                    filename = transfer.filename,
                    actionText = actionText,
                    onActionClick = { onOpenClick(transfer) },
                    date = transfer.date,
                    error = transfer.error,
                )
            }
        }
    }
}

@Composable
private fun TransferItem(
    modifier: Modifier,
    progress: () -> Float,
    filename: String,
    actionText: String,
    date: LocalDateTime,
    error: AppError?,
    onActionClick: () -> Unit,
) {
    val progressColor = MaterialTheme.colorScheme.secondaryContainer
    val iconRes = remember { FileIconResolver.resolve(filename) }

    val context = LocalContext.current
    val formattedDate = remember(date, context) {
        val compatDate = Date.from(date.atZone(ZoneId.systemDefault()).toInstant())
        val dateStr = DateFormat.getDateFormat(context).format(compatDate)
        val timeStr = DateFormat.getTimeFormat(context).format(compatDate)
        "$dateStr $timeStr"
    }

    ListItem(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind() {
                if (progress() < 1f) {
                    drawRect(
                        color = progressColor,
                        size = Size(size.width * progress(), size.height),
                    )
                }
            },
        colors = ListItemDefaults.colors(
            containerColor = if (error != null) MaterialTheme.colorScheme.errorContainer else Color.Transparent
        ),
        leadingContent = {
            Image(
                modifier = Modifier.size(24.dp),
                painter = painterResource(iconRes),
                contentDescription = null
            )
        },
        headlineContent = {
            Text(filename, maxLines = 2)
        },
        supportingContent = {
            if (error != null) {
                Text(error.message(LocalContext.current))
            } else {
                Text(formattedDate)
            }
        },
        trailingContent = {
            Row {
                TextButton(onClick = onActionClick) {
                    Text(actionText)
                }
            }
        }
    )
}

@Composable
private fun ConfirmationBanner(
    sender: String,
    filename: String,
    fileSize: Long,
    onAcceptClick: () -> Unit,
    onRejectClick: () -> Unit,
) {
    val context = LocalContext.current
    val fileSizeStr = remember(fileSize, context) {
        Formatter.formatShortFileSize(context, fileSize)
    }

    ElevatedCard(
        modifier = Modifier.padding(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {

            Text(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .paddingFromBaseline(top = 36.dp),
                text = "Device $sender wants to send $filename ($fileSizeStr)",
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.End),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                TextButton(
                    onClick = onRejectClick,
                ) {
                    Text("Reject")
                }
                TextButton(
                    onClick = onAcceptClick,
                ) {
                    Text("Accept")
                }
            }
        }
    }
}

@Preview
@Composable
private fun ReceiveScreenPreview() {
    CloudSendTheme {
        ReceiveScreen(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            state = State(
                completedTransfers = listOf(
                    CompletedTransfer(
                        id = 1,
                        transferUuid = "1",
                        filename = "dada.txt",
                        date = LocalDateTime.now(),
                        error = null,
                    ),
                    CompletedTransfer(
                        id = 2,
                        transferUuid = "2",
                        filename = "gaiki.pdf",
                        date = LocalDateTime.now(),
                        error = AppError.Network(),
                    )
                ),
                activeTransfer = ActiveTransfer(
                    transferUuid = "3",
                    filename = "slimy_dogs.png",
                    progress = 0.3f,
                    date = LocalDateTime.now(),
                ),
                pendingConfirmation = ReceiveViewModel.TransferConfirmation(
                    filename = "incomindsadwg.jpg",
                    fileSize = 23240512,
                    sender = "Joki Joki"
                )
            ),
            onOpenClick = {},
            onCancelClick = {},
            onAcceptClick = {},
            onRejectClick = {},
        )
    }
}

@Preview
@Composable
private fun ReceiveScreenEmptyPreview() {
    CloudSendTheme {
        ReceiveScreen(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            state = State(),
            onOpenClick = {},
            onCancelClick = {},
            onAcceptClick = {},
            onRejectClick = {},
        )
    }
}