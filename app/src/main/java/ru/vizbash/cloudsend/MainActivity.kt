package ru.vizbash.cloudsend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.Serializable
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

        val startDestination = if (checkSetupDoneInteractor()) MainDest else SetupDest

        setContent {
            val navController = rememberNavController()

            CloudSendTheme {
                NavHost(navController, startDestination) {
                    composable<SetupDest> {
                        SetupScreen(
                            viewModel = hiltViewModel(),
                            navigateToMain = {
                                navController.navigate(MainDest)
                            }
                        )
                    }

                    composable<MainDest> {
                        MainScreen()
                    }
                }
            }
        }
    }
}

@Serializable
object SetupDest

@Serializable
object MainDest
