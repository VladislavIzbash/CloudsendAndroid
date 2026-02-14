package ru.vizbash.cloudsend.ui.screen.send

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import ru.vizbash.cloudsend.R
import ru.vizbash.cloudsend.domain.AppError
import ru.vizbash.cloudsend.domain.Device
import ru.vizbash.cloudsend.presentation.DeviceSelectionViewModel
import ru.vizbash.cloudsend.presentation.DeviceSelectionViewModel.State
import ru.vizbash.cloudsend.ui.component.CenteredColumnLayout
import ru.vizbash.cloudsend.ui.component.CenteredColumnLayoutArt
import ru.vizbash.cloudsend.ui.theme.CloudSendTheme

@Composable
fun DeviceSelectionScreen(
    viewModel: DeviceSelectionViewModel,
    navigateToTransfer: (String, Device) -> Unit,
    modifier: Modifier = Modifier,
    showClose: Boolean = false,
    onCloseClick: () -> Unit = {},
) {
    val state by viewModel.state.collectAsState()

    val openDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            (state as? State.Loaded)?.pendingDevice?.let {
                navigateToTransfer(uri.toString(), it)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.onResume()
    }

    DeviceSelectionScreen(
        modifier = modifier,
        state = state,
        showClose = showClose,
        onCloseClick = onCloseClick,
        onDeviceClick = {
            viewModel.onDeviceClick(
                device = it,
                openFilePicker = {
                    openDocumentLauncher.launch(arrayOf("*/*"))
                },
                navigateToTransfer = navigateToTransfer,
            )
        },
        onRefreshClick = viewModel::onRefreshClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeviceSelectionScreen(
    state: State,
    onDeviceClick: (Device) -> Unit,
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier,
    onCloseClick: () -> Unit = {},
    showClose: Boolean = false,
) {
    Column(modifier) {
        TopAppBar(
            modifier = Modifier.fillMaxWidth(),
            windowInsets = WindowInsets(),
            title = {
                Text(stringResource(R.string.screen_device_list__title))
            },
            navigationIcon = {
                if (showClose) {
                    IconButton(onClick = onCloseClick) {
                        Icon(painterResource(R.drawable.ic_close), null)
                    }
                }
            },
        )
        DeviceSelectionScreenContent(
            state,
            onDeviceClick,
            onRefreshClick,
        )
    }
}

@Composable
private fun DeviceSelectionScreenContent(
    state: State,
    onDeviceClick: (Device) -> Unit,
    onRefreshClick: () -> Unit,
) {
    when (state) {
        is State.Error -> {
            CenteredColumnLayout(
                image = {
                    CenteredColumnLayoutArt(painterResource(R.drawable.warning))
                },
                title = stringResource(R.string.screen_device_list__loading_error),
                text = state.error.message(LocalContext.current),
            ) {
                OutlinedButton(onClick = onRefreshClick) {
                    Text(stringResource(R.string.screen_device_list__refresh_button))
                }
            }
        }

        State.NoDevices -> {
            CenteredColumnLayout(
                image = {
                    CenteredColumnLayoutArt(painterResource(R.drawable.devices))
                },
                title = stringResource(R.string.screen_device_list__empty_title),
                text = stringResource(R.string.screen_device_list__empty_text)
            ) {
                OutlinedButton(onClick = onRefreshClick) {
                    Text(stringResource(R.string.screen_device_list__refresh_button))
                }
            }
        }

        State.Loading -> {
            CenteredColumnLayout(
                image = {
                    CircularProgressIndicator(
                        modifier = Modifier.size(56.dp)
                    )
                },
                title = stringResource(R.string.screen_device_list__loading),
            )
        }

        is State.Loaded -> {
            Column(
                modifier = Modifier.fillMaxSize()
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
                R.string.screen_device_list__device_online
            } else {
                R.string.screen_device_list__device_offline
            }
            Text(
                text = stringResource(textRes).uppercase(),
                color = if (device.isOnline) Color.Green else MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun SendScreenPreview() {
    CloudSendTheme {
        DeviceSelectionScreen(
            state = State.Loaded(
                targetDevices = listOf(
                    Device("Device 1", "", true),
                    Device("Device 2", "", true),
                    Device("Device 3", "", true),
                    Device("Device 4", "", false),
                ),
            ),
            onDeviceClick = {},
            onRefreshClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SendScreenLoadingPreview() {
    CloudSendTheme {
        DeviceSelectionScreen(
            state = State.Loading,
            onDeviceClick = {},
            onRefreshClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SendScreenErrorPreview() {
    CloudSendTheme {
        DeviceSelectionScreen(
            state = State.Error(AppError.General()),
            onDeviceClick = {},
            onRefreshClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SendScreenEmptyPreview() {
    CloudSendTheme {
        DeviceSelectionScreen(
            state = State.NoDevices,
            onDeviceClick = {},
            onRefreshClick = {},
        )
    }
}