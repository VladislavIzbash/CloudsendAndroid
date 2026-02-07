package ru.vizbash.cloudsend.ui.screen.send

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.rememberNavBackStack
import ru.vizbash.cloudsend.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandaloneSendScreen(
    fileUri: String?,
    onCloseClick: () -> Unit,
) {
    val backStack = rememberNavBackStack(SendScreenPage.DeviceSelection)
    val showTopBar = backStack.lastOrNull() is SendScreenPage.DeviceSelection

    Scaffold(
        topBar = {
            if (showTopBar) {
                TopAppBar(
                    title = {
                        Text(stringResource(R.string.screen_device_list__title))
                    },
                    navigationIcon = {
                        IconButton(onClick = onCloseClick) {
                            Icon(painterResource(R.drawable.ic_close), null)
                        }
                    },
                )
            }
        }
    ) { contentPadding ->
        SendScreen(
            modifier = Modifier.padding(contentPadding),
            backStack = backStack,
            fileUri = fileUri,
        )
    }
}