package ru.vizbash.cloudsend.ui.screen.send

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
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.NavDisplay
import kotlinx.serialization.Serializable
import ru.vizbash.cloudsend.domain.Device
import ru.vizbash.cloudsend.presentation.DeviceSelectionViewModel
import ru.vizbash.cloudsend.presentation.TransferViewModel
import ru.vizbash.cloudsend.ui.screen.TransferScreen

@Serializable
sealed class SendScreenPage : NavKey {
    @Serializable
    data object DeviceSelection : SendScreenPage()

    @Serializable
    data class Transfer(val uri: String, val targetDevice: Device) : SendScreenPage()
}

@Composable
fun SendScreen(
    modifier: Modifier = Modifier,
    backStack: NavBackStack<NavKey> = rememberNavBackStack(SendScreenPage.DeviceSelection),
    showCloseButton: Boolean = false,
    exitAfterTransfer: Boolean = false,
    fileUri: String? = null,
    onClose: () -> Unit = {},
) {
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
            entry<SendScreenPage.DeviceSelection> {
                val viewModel = hiltViewModel<DeviceSelectionViewModel, DeviceSelectionViewModel.Factory> {
                    it.create(fileUri)
                }

                DeviceSelectionScreen(
                    viewModel = viewModel,
                    navigateToTransfer = { uri, targetDevice ->
                        backStack.add(SendScreenPage.Transfer(uri, targetDevice))
                    },
                    showClose = showCloseButton,
                    onCloseClick = onClose,
                )
            }

            entry<SendScreenPage.Transfer> { key ->
                val viewModel = hiltViewModel<TransferViewModel, TransferViewModel.Factory> {
                    it.create(key.uri, key.targetDevice)
                }
                TransferScreen(
                    viewModel = viewModel,
                    navigateBack = {
                        if (exitAfterTransfer) {
                            onClose()
                        } else {
                            backStack.removeLastOrNull()
                        }
                    },
                    showCloseAsBack = exitAfterTransfer,
                )
            }
        }
    )
}

private fun <T : Any> slideTransition(): AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform = {
    slideInHorizontally(initialOffsetX = { -it }) togetherWith
            slideOutHorizontally(targetOffsetX = { it })
}
