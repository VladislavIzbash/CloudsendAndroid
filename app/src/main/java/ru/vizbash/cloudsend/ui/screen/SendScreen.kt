package ru.vizbash.cloudsend.ui.screen

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.NavDisplay
import kotlinx.serialization.Serializable
import ru.vizbash.cloudsend.presentation.TransferViewModel

@Composable
fun SendScreen(
    modifier: Modifier = Modifier,
) {
    val backStack = rememberNavBackStack(Screen.DeviceSelection)

    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        onBack = {
            backStack.removeLastOrNull()
        },
        popTransitionSpec = slideTransition(),
        transitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<Screen.DeviceSelection> {
                DeviceSelectionScreen(
                    viewModel = hiltViewModel(),
                    navigateToTransfer = { uri ->
                        backStack.add(Screen.TransferScreen(uri.toString()))
                    }
                )
            }

            entry<Screen.TransferScreen> { key ->
                val viewModel = hiltViewModel<TransferViewModel, TransferViewModel.Factory> {
                    it.create(key.uri)
                }
                TransferScreen(
                    viewModel = viewModel,
                )
            }
        }
    )
}

private fun <T : Any> slideTransition(): AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform = {
    slideInHorizontally(initialOffsetX = { -it }) togetherWith
            slideOutHorizontally(targetOffsetX = { it })
}

@Serializable
private sealed class Screen : NavKey {
    @Serializable
    data object DeviceSelection : Screen()

    @Serializable
    data class TransferScreen(val uri: String) : Screen()
}