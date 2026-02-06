package ru.vizbash.cloudsend.ui.screen

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import ru.vizbash.cloudsend.R

@Serializable
private sealed class Pages {
    @Serializable
    data object Receive : Pages()

    @Serializable
    data object Send : Pages()

    @Serializable
    data object History : Pages()
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val currentDest = navController.currentBackStackEntryAsState().value?.destination

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                NavBarDestination(navController, currentDest, Pages.Receive) {
                    Icon(painterResource(R.drawable.ic_download), null)
                }
                NavBarDestination(navController, currentDest, Pages.Send) {
                    Icon(painterResource(R.drawable.ic_send), null)
                }
                NavBarDestination(navController, currentDest, Pages.History) {
                    Icon(painterResource(R.drawable.ic_history), null)
                }
//                NavigationBarItem(
//                    selected = currentDest?.hasRoute<Pages.Receive>() ?: false,
//                    onClick = {
//                        navController.navigate(Pages.Receive)
//                    },
//                    icon = {
//                        Icon(painterResource(R.drawable.ic_download), null)
//                    }
//                )
//                NavigationBarItem(
//                    selected = currentDest?.hasRoute<Pages.Send>() ?: false,
//                    onClick = {
//                        navController.navigate(Pages.Send)
//                    },
//                    icon = {
//                        Icon(painterResource(R.drawable.ic_send), null)
//                    }
//                )
//                NavigationBarItem(
//                    selected = currentDest?.hasRoute<Pages.History>() ?: false,
//                    onClick = {
//                        navController.navigate(Pages.History)
//                    },
//                    icon = {
//                        Icon(painterResource(R.drawable.ic_history), null)
//                    }
//                )
            }
        }
    ) { contentPadding ->
        NavHost(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            navController = navController,
            startDestination = Pages.Receive,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
        ) {
            composable<Pages.Receive> {
                Text("receive")
            }
            composable<Pages.Send> {
                SendScreen(hiltViewModel(it))
            }
            composable<Pages.History> {
                Text("history")
            }
        }
    }
}

@Composable
private inline fun <reified T: Any> RowScope.NavBarDestination(
    navController: NavController,
    currentDest: NavDestination?,
    route: T,
    noinline icon: @Composable () -> Unit,
) {
    NavigationBarItem(
        selected = currentDest?.hasRoute<T>() ?: false,
        onClick = {
            navController.navigate(route) {
                popUpTo(currentDest!!.route!!) {
                    inclusive = true
                    saveState = true
                }
                restoreState = true
            }
        },
        icon = icon,
    )
}