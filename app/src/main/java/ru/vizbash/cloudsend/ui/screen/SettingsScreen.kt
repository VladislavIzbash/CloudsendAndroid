package ru.vizbash.cloudsend.ui.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.vizbash.cloudsend.R
import ru.vizbash.cloudsend.data.persistence.settings.AppSettings
import ru.vizbash.cloudsend.data.persistence.settings.Setting
import ru.vizbash.cloudsend.presentation.SettingsViewModel
import ru.vizbash.cloudsend.presentation.SettingsViewModel.State
import ru.vizbash.cloudsend.ui.component.ListHeader
import ru.vizbash.cloudsend.ui.theme.CloudSendTheme
import ru.vizbash.cloudsend.util.takePersistablePermission

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val openDirectoryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            takePersistablePermission(context, uri)
            viewModel.onDownloadDirPicked(uri.toString())
        }
    }

    val state by viewModel.state.collectAsState()

    SettingsScreen(
        modifier = modifier,
        state = state,
        onBooleanToggle = viewModel::onBooleanToggle,
        onDownloadDirClick = {
            openDirectoryLauncher.launch(null)
        }
    )
}

@Composable
private fun SettingsScreen(
    state: State,
    onDownloadDirClick: () -> Unit,
    onBooleanToggle: (Setting.Bool) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(vertical = 16.dp)
    ) {
        ListHeader(stringResource(R.string.screen_settings__receive__header))
        DirectorySetting(
            title = stringResource(R.string.screen_settings__receive__download_dir),
            supportingText = state.downloadDir
                ?: stringResource(R.string.screen_settings__receive__download_dir_hint),
            onClick = onDownloadDirClick,
        )
        SwitchSetting(
            title = stringResource(R.string.screen_settings__receive__auto_accept),
            enabled = state.autoAccept,
            onToggle = {
                onBooleanToggle(AppSettings.AutoAcceptTransfers)
            }
        )
    }
}

@Composable
private fun SwitchSetting(
    title: String,
    enabled: Boolean,
    onToggle: () -> Unit,
) {
    ListItem(
        modifier = Modifier
            .clickable(onClick = onToggle),
        headlineContent = {
            Text(title)
        },
        trailingContent = {
            Switch(
                checked = enabled,
                onCheckedChange = {
                    onToggle()
                },
            )
        }
    )
}

@Composable
private fun DirectorySetting(
    title: String,
    supportingText: String,
    onClick: () -> Unit,
) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = {
            Text(title)
        },
        supportingContent = {
            Text(
                text = supportingText,
                overflow = TextOverflow.Ellipsis,
            )
        },
        trailingContent = {
            IconButton(onClick = onClick) {
                Icon(painterResource(R.drawable.ic_open_in_new), null)
            }
        }
    )
}



@Preview
@Composable
fun SettingsScreenPreview() {
    CloudSendTheme {
        SettingsScreen(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            onBooleanToggle = {},
            onDownloadDirClick = {},
            state = State(
                downloadDir = null,
                autoAccept = false,
            )
        )
    }
}