package ru.vizbash.cloudsend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ru.vizbash.cloudsend.domain.CheckSetupDoneInteractor
import ru.vizbash.cloudsend.ui.screen.SetupScreen
import ru.vizbash.cloudsend.ui.theme.CloudSendTheme

abstract class BaseActivity : ComponentActivity() {
    abstract val checkSetupDoneInteractor: CheckSetupDoneInteractor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        var showSetup by mutableStateOf(!checkSetupDoneInteractor())

        setContent {
            CloudSendTheme {
                if (showSetup) {
                    SetupScreen(
                        viewModel = hiltViewModel(),
                        navigateToMain = {
                            showSetup = false
                        }
                    )
                } else {
                    ActivityScreen()
                }
            }
        }
    }

    @Composable
    abstract fun ActivityScreen()
}