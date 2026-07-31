package ru.vizbash.cloudsend.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.serialization.Serializable
import ru.vizbash.cloudsend.R
import ru.vizbash.cloudsend.ui.screen.receive.ReceiveScreen
import ru.vizbash.cloudsend.ui.screen.send.SendScreen
import ru.vizbash.cloudsend.ui.theme.CloudSendTheme

@Serializable
private sealed class Pages {
    @Serializable
    data object Receive : Pages()

    @Serializable
    data object Send : Pages()

    @Serializable
    data object History : Pages()
}

enum class Page { Receive, Send, Settings }

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    initialPage: Page = Page.Send,
) {
    var currentPage by rememberSaveable { mutableStateOf(initialPage) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentPage == Page.Receive,
                    onClick = {
                        currentPage = Page.Receive
                    },
                    icon = {
                        Icon(painterResource(R.drawable.ic_download), null)
                    }
                )
                NavigationBarItem(
                    selected = currentPage == Page.Send,
                    onClick = {
                        currentPage = Page.Send
                    },
                    icon = {
                        Icon(painterResource(R.drawable.ic_send), null)
                    }
                )
                NavigationBarItem(
                    selected = currentPage == Page.Settings,
                    onClick = {
                        currentPage = Page.Settings
                    },
                    icon = {
                        Icon(painterResource(R.drawable.ic_settings), null)
                    }
                )
            }
        }
    ) { contentPadding ->
        when (currentPage) {
            Page.Receive -> {
                ReceiveScreen(
                    modifier = Modifier.padding(contentPadding),
                    viewModel = hiltViewModel(),
                )
            }

            Page.Send -> {
                SendScreen(
                    modifier = Modifier.padding(contentPadding),
                )
            }

            Page.Settings -> {
                SettingsScreen(
                    modifier = Modifier.padding(contentPadding),
                    viewModel = hiltViewModel(),
                )
            }
        }
    }
}

@Composable
@Preview(device = "spec:parent=pixel_5,navigation=buttons")
fun MainScreenPreview() {
    CloudSendTheme {
        MainScreen(initialPage = Page.Send)
    }
}
