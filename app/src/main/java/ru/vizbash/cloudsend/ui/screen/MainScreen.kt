package ru.vizbash.cloudsend.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import kotlinx.serialization.Serializable
import ru.vizbash.cloudsend.R
import ru.vizbash.cloudsend.ui.screen.send.SendScreen

@Serializable
private sealed class Pages {
    @Serializable
    data object Receive : Pages()

    @Serializable
    data object Send : Pages()

    @Serializable
    data object History : Pages()
}

private enum class Page { Receive, Send, History }

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
) {
    var currentPage by rememberSaveable { mutableStateOf(Page.Receive) }

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
                    selected = currentPage == Page.History,
                    onClick = {
                        currentPage = Page.History
                    },
                    icon = {
                        Icon(painterResource(R.drawable.ic_history), null)
                    }
                )
            }
        }
    ) { contentPadding ->
        when (currentPage) {
            Page.Receive -> {
                Text("receive")
            }
            Page.Send -> {
                SendScreen(
                    modifier = Modifier.padding(contentPadding),
                )
            }
            Page.History -> {
                Text("history")
            }
        }
    }
}
