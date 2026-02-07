package ru.vizbash.cloudsend

import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import ru.vizbash.cloudsend.domain.CheckSetupDoneInteractor
import ru.vizbash.cloudsend.ui.screen.send.SendScreen
import javax.inject.Inject

private const val TAG = "SendActivity"

@AndroidEntryPoint
class SendActivity : BaseActivity() {
    @Inject
    override lateinit var checkSetupDoneInteractor: CheckSetupDoneInteractor

    @Composable
    override fun ActivityScreen() {
        val fileUri = remember { handleSendIntent(intent)?.toString() }

//        StandaloneSendScreen(
//            fileUri = fileUri,
//            onCloseClick = {
//                finish()
//            }
//        )

        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
            SendScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .safeDrawingPadding(),
                showClose = true,
                onCloseClick = {
                    finish()
                },
                fileUri = fileUri,
            )
        }

    }

}

@Suppress("DEPRECATION")
private fun handleSendIntent(intent: Intent): Uri? {
    if (intent.action != Intent.ACTION_SEND) {
        Log.e(TAG, "Cannot handle intent action ${intent.action}")
        return null
    }

    val uri = intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri
    if (uri == null) {
        Log.e(TAG, "No uri provided in send intent")
    }

    return uri
}