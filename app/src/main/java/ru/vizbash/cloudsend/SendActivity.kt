package ru.vizbash.cloudsend

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
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
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import dagger.hilt.android.AndroidEntryPoint
import ru.vizbash.cloudsend.domain.CheckSetupDoneInteractor
import ru.vizbash.cloudsend.ui.screen.send.SendScreen
import ru.vizbash.cloudsend.ui.screen.send.SendScreenPage
import javax.inject.Inject
import kotlin.uuid.Uuid

private const val TAG = "SendActivity"

@AndroidEntryPoint
class SendActivity : BaseActivity() {
    @Inject
    override lateinit var checkSetupDoneInteractor: CheckSetupDoneInteractor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        val shortCuts = listOf(
//            ShortcutInfoCompat.Builder(this, "dasd")
//                .setC
//        )
//        ShortcutManagerCompat.setDynamicShortcuts(this, )
    }

    @Composable
    override fun ActivityScreen() {
        val (fileUri, targetUuid) = remember { handleSendIntent(intent) }

//        val backStack = if (targetUuid != null) {
//            rememberNavBackStack(
//                SendScreenPage.DeviceSelection(fileUri),
//                SendScreenPage.Transfer(fileUri, targetUuid)
//            )
//        } else {
//            rememberNavBackStack(SendScreenPage.DeviceSelection(fileUri))
//        }

        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
            SendScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .safeDrawingPadding(),
                showCloseButton = true,
                exitAfterTransfer = true,
                onClose = {
                    finish()
                },
            )
        }

    }

}

@Suppress("DEPRECATION")
private fun handleSendIntent(intent: Intent): Pair<String?, Uuid?> {
    if (intent.action != Intent.ACTION_SEND) {
        Log.e(TAG, "Cannot handle intent action ${intent.action}")
        return Pair(null, null)
    }

    val uri = intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri
    if (uri == null) {
        Log.e(TAG, "No uri provided in send intent")
    }

    val targetUuid = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        intent.getStringExtra(Intent.EXTRA_SHORTCUT_ID)?.let(Uuid::parse)
    } else {
        null
    }

    return Pair(uri.toString(), targetUuid)
}