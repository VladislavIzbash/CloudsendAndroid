package ru.vizbash.cloudsend

import androidx.compose.runtime.Composable
import dagger.hilt.android.AndroidEntryPoint
import ru.vizbash.cloudsend.domain.CheckSetupDoneInteractor
import ru.vizbash.cloudsend.ui.screen.MainScreen
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
