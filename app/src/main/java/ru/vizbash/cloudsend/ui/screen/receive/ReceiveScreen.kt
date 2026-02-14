@file:OptIn(ExperimentalTime::class)

package ru.vizbash.cloudsend.ui.screen.receive

import android.icu.text.DateFormat
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.vizbash.cloudsend.R
import ru.vizbash.cloudsend.domain.AppError
import ru.vizbash.cloudsend.presentation.receive.ActiveTransfer
import ru.vizbash.cloudsend.presentation.receive.CompletedTransfer
import ru.vizbash.cloudsend.presentation.receive.ReceiveViewModel
import ru.vizbash.cloudsend.presentation.receive.ReceiveViewModel.State
import ru.vizbash.cloudsend.ui.component.ListHeader
import ru.vizbash.cloudsend.ui.theme.CloudSendTheme
import ru.vizbash.cloudsend.ui.util.FileIconResolver
import java.time.LocalDateTime
import kotlin.time.ExperimentalTime

@Composable
fun ReceiveScreen(
    pm: ReceiveViewModel,
    modifier: Modifier = Modifier,
) {

}

@Composable
fun ReceiveScreen(
    modifier: Modifier = Modifier,
    state: State,
    onOpenClick: (CompletedTransfer) -> Unit,
    onCancelClick: () -> Unit,
) {
    LazyColumn(modifier) {
        item(key = "header1") {
            ListHeader(
                modifier = Modifier.height(32.dp),
                text = stringResource(R.string.screen_receive__header_active_transfer),
            )
        }

        item(key = "active_transfer") {
            state.activeTransfer?.let { transfer ->
                TransferItem(
                    modifier = Modifier.animateItem(),
                    progress = { transfer.progress },
                    filename = transfer.filename,
                    actionText = stringResource(R.string.screen_receive__transfer_action_cancel),
                    onActionClick = onCancelClick,
                    onRemoveClick = {},
                    date = transfer.date,
                    error = null,
                )
            }
        }

        item(key = "header2") {
            ListHeader(
                modifier = Modifier.height(32.dp),
                text = stringResource(R.string.screen_receive__header_completed_transfers),
            )
        }

        items(
            items = state.completedTransfers,
            key = CompletedTransfer::id,
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
                onRemoveClick = {},
                date = transfer.date,
                error = transfer.error,
            )
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
    onRemoveClick: () -> Unit,
    onActionClick: () -> Unit,
) {
    val progressColor = MaterialTheme.colorScheme.secondaryContainer
    val iconRes = remember { FileIconResolver.resolve(filename) }

    val formattedDate = remember(date) {
        val dateStr = DateFormat.getPatternInstance(DateFormat.ABBR_MONTH_DAY).format(date)
        val timeStr = DateFormat.getPatternInstance(DateFormat.HOUR24_MINUTE).format(date)
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
            Text(filename)
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
                    Text(actionText.uppercase())
                }
                IconButton(onClick = onRemoveClick) {
                    Icon(painterResource(R.drawable.ic_close), null)
                }
            }
        }
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@ExperimentalTime
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
                        filename = "dada.txt",
                        date = LocalDateTime.now(),
                        error = null,
                    ),
                    CompletedTransfer(
                        id = 2,
                        filename = "gaiki.pdf",
                        date = LocalDateTime.now(),
                        error = AppError.Network(),
                    )
                ),
                activeTransfer = ActiveTransfer(
                    filename = "slimy_dogs.png",
                    progress = 0.3f,
                    date = LocalDateTime.now(),
                )
            ),
            onOpenClick = {},
            onCancelClick = {},
        )
    }
}