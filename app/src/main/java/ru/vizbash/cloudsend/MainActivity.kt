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
import dagger.hilt.android.AndroidEntryPoint
import ru.vizbash.cloudsend.domain.CheckSetupDoneInteractor
import ru.vizbash.cloudsend.ui.screen.MainScreen
import ru.vizbash.cloudsend.ui.screen.SetupScreen
import ru.vizbash.cloudsend.ui.theme.CloudSendTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    @Inject
    override lateinit var checkSetupDoneInteractor: CheckSetupDoneInteractor

    @Composable
    override fun ActivityScreen() {
        MainScreen()
    }
}
