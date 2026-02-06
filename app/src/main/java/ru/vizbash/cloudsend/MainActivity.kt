package ru.vizbash.cloudsend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import ru.vizbash.cloudsend.domain.CheckSetupDoneInteractor
import ru.vizbash.cloudsend.ui.screen.MainScreen
import ru.vizbash.cloudsend.ui.screen.SetupScreen
import ru.vizbash.cloudsend.ui.theme.CloudSendTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var checkSetupDoneInteractor: CheckSetupDoneInteractor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        var showMainScreen by mutableStateOf(checkSetupDoneInteractor())

        setContent {
            CloudSendTheme {
                if (showMainScreen) {
                    MainScreen()
                } else {
                    SetupScreen(
                        viewModel = hiltViewModel(),
                        navigateToMain = {
                            showMainScreen = true
                        }
                    )
                }
            }
        }
    }
}