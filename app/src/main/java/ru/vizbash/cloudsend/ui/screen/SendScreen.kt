package ru.vizbash.cloudsend.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.util.fastForEach
import ru.vizbash.cloudsend.R
import ru.vizbash.cloudsend.domain.Device
import ru.vizbash.cloudsend.presentation.SendViewModel
import ru.vizbash.cloudsend.presentation.SendViewModel.State
import ru.vizbash.cloudsend.ui.theme.CloudSendTheme
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
fun SendScreen(
    viewModel: SendViewModel,
    navigateToTransfer: (Uri) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()

    val openDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            navigateToTransfer(uri)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.onResume()
    }

    SendScreen(
        modifier = modifier,
        state = state,
        onDeviceClick = {
            viewModel.onDeviceClick(
                device = it,
                openFilePicker = {
                    openDocumentLauncher.launch(arrayOf("*/*"))
                }
            )
        },
    )
}

@Composable
private fun SendScreen(
    state: State,
    onDeviceClick: (Device) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        State.Error -> {
            Centered {
                Text(
                    text = stringResource(R.string.screen_send__loading_error),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        State.NoDevices -> {
            Centered {
                Text(
                    text = stringResource(R.string.screen_send__no_devices),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        State.Loading -> {
            Centered {
                CircularProgressIndicator()
            }
        }
        is State.Loaded -> {
            Column(
                modifier = modifier.fillMaxSize()
            ) {
                state.targetDevices.fastForEach { device ->
                    SendTarget(
                        device = device,
                        onClick = {
                            onDeviceClick(device)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun Centered(
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
        content = content,
    )
}

@Composable
private fun SendTarget(
    device: Device,
    onClick: () -> Unit,
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        headlineContent = {
            Text(device.name)
        },
        trailingContent = {
            val textRes = if (device.isOnline) {
                R.string.screen_send__device_online
            } else {
                R.string.screen_send__device_offline
            }
            Text(
                text = stringResource(textRes).uppercase(),
                color = if (device.isOnline) Color.Green else MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
    )
}

@OptIn(ExperimentalUuidApi::class)
@Preview(showBackground = true)
@Composable
private fun SendScreenPreview() {
    CloudSendTheme {
        SendScreen(
            state = State.Loaded(
                targetDevices = listOf(
                    Device("Device 1", Uuid.NIL, true),
                    Device("Device 2", Uuid.NIL, true),
                    Device("Device 3", Uuid.NIL, true),
                    Device("Device 4", Uuid.NIL, false),
                ),
            ),
            onDeviceClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SendScreenLoadingPreview() {
    CloudSendTheme {
        SendScreen(
            state = State.Loading,
            onDeviceClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SendScreenErrorPreview() {
    CloudSendTheme {
        SendScreen(
            state = State.Error,
            onDeviceClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SendScreenEmptyPreview() {
    CloudSendTheme {
        SendScreen(
            state = State.NoDevices,
            onDeviceClick = {},
        )
    }
}