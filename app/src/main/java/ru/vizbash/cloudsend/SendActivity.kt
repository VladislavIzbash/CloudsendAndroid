package ru.vizbash.cloudsend

import android.content.Intent
import android.net.Uri
import android.os.Build
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
import androidx.navigation3.runtime.rememberNavBackStack
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import ru.vizbash.cloudsend.data.persistence.db.DeviceDao
import ru.vizbash.cloudsend.domain.CheckSetupDoneInteractor
import ru.vizbash.cloudsend.domain.Device
import ru.vizbash.cloudsend.ui.screen.send.SendScreen
import ru.vizbash.cloudsend.ui.screen.send.SendScreenPage
import javax.inject.Inject

private const val TAG = "SendActivity"

@AndroidEntryPoint
class SendActivity : BaseActivity() {
    @Inject
    override lateinit var checkSetupDoneInteractor: CheckSetupDoneInteractor

    @Inject
    lateinit var deviceDao: DeviceDao

    @Composable
    override fun ActivityScreen() {
        val (fileUri, targetUuid) = remember { resolveSendIntent(intent) }

        val targetDevice = remember(targetUuid) {
            targetUuid?.let { resolveDevice(targetUuid) }
        }

        val backStack = if (fileUri != null && targetDevice != null) {
            rememberNavBackStack(
                SendScreenPage.DeviceSelection(fileUri),
                SendScreenPage.Transfer(fileUri, targetDevice)
            )
        } else {
            rememberNavBackStack(SendScreenPage.DeviceSelection(fileUri))
        }

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
                backStack = backStack,
            )
        }

    }

    private fun resolveDevice(uuid: String): Device = runBlocking {
        val entity = deviceDao.getByUuid(uuid)
        Device(entity.name, entity.uuid, false)
    }
}

@Suppress("DEPRECATION")
private fun resolveSendIntent(intent: Intent): Pair<String?, String?> {
    if (intent.action != Intent.ACTION_SEND) {
        Log.e(TAG, "Cannot handle intent action ${intent.action}")
        return Pair(null, null)
    }

    val uri = intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri
    if (uri == null) {
        Log.e(TAG, "No uri provided in send intent")
    }

    val targetUuid = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        intent.getStringExtra(Intent.EXTRA_SHORTCUT_ID)
    } else {
        null
    }

    return Pair(uri.toString(), targetUuid)
}