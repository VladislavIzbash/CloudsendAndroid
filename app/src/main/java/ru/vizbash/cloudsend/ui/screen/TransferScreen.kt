package ru.vizbash.cloudsend.ui.screen

import android.text.format.Formatter
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.vizbash.cloudsend.R
import ru.vizbash.cloudsend.domain.AppError
import ru.vizbash.cloudsend.presentation.TransferViewModel
import ru.vizbash.cloudsend.presentation.TransferViewModel.State
import ru.vizbash.cloudsend.presentation.TransferViewModel.TransferState
import ru.vizbash.cloudsend.ui.theme.CloudSendTheme
import ru.vizbash.cloudsend.ui.util.ellipsize

private val ImageSize = 120.dp
private const val FilenameLimit = 20

@Composable
fun TransferScreen(
    viewModel: TransferViewModel,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    showCloseAsBack: Boolean = false,
) {
    val state by viewModel.state.collectAsState()

    var showLeaveConfirmation by remember { mutableStateOf(false) }

    val leaveConfirmationEnabled = state.transferState is TransferState.Initializing ||
            state.transferState is TransferState.InProgress
    BackHandler(enabled = leaveConfirmationEnabled) {
        showLeaveConfirmation = true
    }

    if (showLeaveConfirmation) {
        AlertDialog(
            onDismissRequest = {
                showLeaveConfirmation = false
            },
            title = {
                Text(stringResource(R.string.screen_transfer__leave_dialog_title))
            },
            text = {
                Text(stringResource(R.string.screen_transfer__leave_dialog_text))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLeaveConfirmation = false
                        navigateBack()
                    }
                ) {
                    Text(stringResource(R.string.screen_transfer__leave_dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showLeaveConfirmation = false
                    }
                ) {
                    Text(stringResource(R.string.screen_transfer__leave_dialog_cancel))
                }
            }
        )
    }

    TransferScreen(
        modifier = modifier,
        showCloseAsBack = showCloseAsBack,
        state = state,
        onBackClick = navigateBack,
        onRetryClick = viewModel::onRetryClick,
        onCancelClick = {
            viewModel.onCancelClick(navigateBack)
        },
    )
}

@Composable
private fun TransferScreen(
    state: State,
    showCloseAsBack: Boolean,
    onBackClick: () -> Unit,
    onCancelClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .height(300.dp)
                .widthIn(max = 300.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when (state.transferState) {
                is TransferState.Done -> {
                    TransferScreenDone(state, showCloseAsBack, onBackClick)
                }

                is TransferState.Error -> {
                    TransferScreenError(
                        state,
                        state.transferState.error,
                        showCloseAsBack,
                        onRetryClick,
                        onBackClick,
                    )
                }

                is TransferState.InProgress -> {
                    TransferScreenInProgress(state, state.transferState, onCancelClick)
                }

                TransferState.Initializing -> {
                    TransferScreenInitializing(state, onCancelClick)
                }
            }
        }
    }
}

@Composable
private fun TransferScreenInProgress(
    state: State,
    transferState: TransferState.InProgress,
    onCancelClick: () -> Unit,
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    val animatedProgress by animateFloatAsState(transferState.progress)

    Image(
        modifier =
            Modifier
                .size(120.dp)
                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                .drawWithContent {
                    drawContent()

                    val colorHeight = size.height * animatedProgress
                    drawRect(
                        color = primaryColor,
                        blendMode = BlendMode.SrcIn,
                        topLeft = Offset(0f, size.height - colorHeight),
                        size = Size(size.width, colorHeight)
                    )
                },
        painter = painterResource(R.drawable.arrow_circle_up),
        contentDescription = null,
        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.outlineVariant)
    )

    Text(
        text = stringResource(
            R.string.screen_transfer__progress_title,
            state.filename.ellipsize(FilenameLimit),
            state.targetDevice,
        ),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.titleMedium,
    )

    val context = LocalContext.current
    val transferred = remember(transferState.transferredBytes) {
        Formatter.formatShortFileSize(context, transferState.transferredBytes)
    }
    val total = remember(transferState.totalBytes) {
        Formatter.formatShortFileSize(context, transferState.totalBytes)
    }
    Text(
        text = "$transferred / $total",
        style = MaterialTheme.typography.bodyMedium,
    )

    OutlinedButton(onClick = onCancelClick) {
        Text(stringResource(R.string.screen_transfer__progress_cancel_button))
    }
}

@Composable
private fun TransferScreenInitializing(
    state: State,
    onCancelClick: () -> Unit,
) {
    Box(
        modifier = Modifier.size(ImageSize),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(56.dp)
        )
    }
    Text(
        text = stringResource(R.string.screen_transfer__initializing_title, state.targetDevice),
        style = MaterialTheme.typography.titleMedium,
    )
    OutlinedButton(onClick = onCancelClick) {
        Text(stringResource(R.string.screen_transfer__progress_cancel_button))
    }
}

@Composable
private fun TransferScreenError(
    state: State,
    error: AppError,
    showCloseAsBack: Boolean,
    onRetryClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    Image(
        modifier = Modifier.size(ImageSize),
        painter = painterResource(R.drawable.warning),
        contentDescription = null,
        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.outlineVariant)
    )
    Text(
        text = stringResource(
            R.string.screen_transfer__error_title,
            state.filename.ellipsize(FilenameLimit),
            state.targetDevice,
        ),
        style = MaterialTheme.typography.titleMedium,
        textAlign = TextAlign.Center,
    )
    Text(
        text = error.message(LocalContext.current),
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
    )

    OutlinedButton(onClick = onRetryClick) {
        Text(stringResource(R.string.screen_transfer__error_retry_button))
    }
    BackButton(onBackClick, showCloseAsBack)
}

@Composable
private fun TransferScreenDone(
    state: State,
    showCloseAsBack: Boolean,
    onBackClick: () -> Unit,
) {
    Image(
        modifier = Modifier.size(ImageSize),
        painter = painterResource(R.drawable.check_circle),
        contentDescription = null,
        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.outlineVariant)
    )
    Text(
        text = stringResource(
            R.string.screen_transfer__done_title,
            state.filename.ellipsize(FilenameLimit),
            state.targetDevice,
        ),
        style = MaterialTheme.typography.titleMedium,
        textAlign = TextAlign.Center,
    )

    BackButton(onBackClick, showCloseAsBack)
}

@Composable
private fun BackButton(
    onClick: () -> Unit,
    showCloseAsBack: Boolean,
) {
    TextButton(onClick = onClick) {
        if (showCloseAsBack) {
            Text(stringResource(R.string.screen_transfer__close_button))
        } else {
            Text(stringResource(R.string.screen_transfer__back_button))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TransferScreenInProgressPreview() {
    CloudSendTheme {
        TransferScreen(
            state = State(
                filename = "fishki.png",
                targetDevice = "Macbook Pro",
                transferState = TransferState.InProgress(
                    progress = 0.4f,
                    transferredBytes = 20000,
                    totalBytes = 124294,
                )
            ),
            showCloseAsBack = false,
            onCancelClick = {},
            onRetryClick = {},
            onBackClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TransferScreenInitializingPreview() {
    CloudSendTheme {
        TransferScreen(
            state = State(
                filename = "fishki.png",
                targetDevice = "Macbook Pro",
                transferState = TransferState.Initializing,
            ),
            showCloseAsBack = false,
            onCancelClick = {},
            onRetryClick = {},
            onBackClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TransferScreenErrorPreview() {
    CloudSendTheme {
        TransferScreen(
            state = State(
                filename = "fishki.png",
                targetDevice = "Macbook Pro",
                transferState = TransferState.Error(AppError.General),
            ),
            showCloseAsBack = false,
            onCancelClick = {},
            onRetryClick = {},
            onBackClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TransferScreenDonePreview() {
    CloudSendTheme {
        TransferScreen(
            state = State(
                filename = "fishki.png",
                targetDevice = "Macbook Pro",
                transferState = TransferState.Done,
            ),
            showCloseAsBack = false,
            onCancelClick = {},
            onRetryClick = {},
            onBackClick = {},
        )
    }
}