package ru.vizbash.cloudsend.ui.screen

import android.text.format.Formatter
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.vizbash.cloudsend.R
import ru.vizbash.cloudsend.presentation.TransferViewModel
import ru.vizbash.cloudsend.presentation.TransferViewModel.State
import ru.vizbash.cloudsend.presentation.TransferViewModel.TransferState
import ru.vizbash.cloudsend.ui.theme.CloudSendTheme

private val ImageSize = 120.dp

@Composable
fun TransferScreen(
    viewModel: TransferViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()

    TransferScreen(
        modifier = modifier,
        state = state,
    )
}

@Composable
private fun TransferScreen(
    state: State,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.height(300.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when (state.transferState) {
                is TransferState.Done -> {
                    TransferScreenDone(state)
                }
                TransferState.Error -> {
                    TransferScreenError(state)
                }
                is TransferState.InProgress -> {
                    TransferScreenInProgress(state, state.transferState)
                }
                TransferState.Initializing -> {
                    TransferScreenInitializing(state)
                }
            }
        }
    }
}

@Composable
private fun TransferScreenInProgress(
    state: State,
    transferState: TransferState.InProgress,
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
            state.filename,
            state.targetDevice,
        ),
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

    OutlinedButton(
        onClick = {}
    ) {
        Text(stringResource(R.string.screen_transfer__progress_cancel_button))
    }
}

@Composable
private fun TransferScreenInitializing(state: State) {
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
    OutlinedButton(
        onClick = {}
    ) {
        Text(stringResource(R.string.screen_transfer__progress_cancel_button))
    }
}

@Composable
private fun TransferScreenError(state: State) {
    Image(
        modifier = Modifier.size(ImageSize),
        painter = painterResource(R.drawable.warning),
        contentDescription = null,
        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.outlineVariant)
    )
    Text(
        text = stringResource(
            R.string.screen_transfer__error_title,
            state.filename,
            state.targetDevice,
        ),
        style = MaterialTheme.typography.titleMedium,
    )
    Text(
        text = stringResource(R.string.screen_transfer__error_text),
        style = MaterialTheme.typography.bodyMedium,
    )

    OutlinedButton(
        onClick = {}
    ) {
        Text(stringResource(R.string.screen_transfer__error_retry_button))
    }
    TextButton(
        onClick = {}
    ) {
        Text(stringResource(R.string.screen_transfer__back_button))
    }
}

@Composable
private fun TransferScreenDone(state: State) {
    Image(
        modifier = Modifier.size(ImageSize),
        painter = painterResource(R.drawable.check_circle),
        contentDescription = null,
        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.outlineVariant)
    )
    Text(
        text = stringResource(
            R.string.screen_transfer__done_title,
            state.filename,
            state.targetDevice,
        ),
        style = MaterialTheme.typography.titleMedium,
    )

    TextButton(
        onClick = {}
    ) {
        Text(stringResource(R.string.screen_transfer__back_button))
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
            )
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
            )
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
                transferState = TransferState.Error,
            )
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
            )
        )
    }
}