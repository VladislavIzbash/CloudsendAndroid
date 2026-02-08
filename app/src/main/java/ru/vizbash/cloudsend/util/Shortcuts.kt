package ru.vizbash.cloudsend.util

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import ru.vizbash.cloudsend.DIRECT_SHARE_CATEGORY
import ru.vizbash.cloudsend.SendActivity

const val SEND_CAPABILITY = "actions.intent.SEND_MESSAGE"
const val RECEIVE_CAPABILITY = "actions.intent.RECEIVE_MESSAGE"

fun createDeviceShortcut(
    context: Context,
    deviceUuid: String,
    deviceName: String,
    capabilityBinding: String? = null,
): ShortcutInfoCompat {
    return ShortcutInfoCompat.Builder(context, deviceUuid)
        .setIntent(
            Intent(context, SendActivity::class.java)
                .setAction(Intent.ACTION_SEND)
        )
        .setShortLabel(deviceName)
        .setCategories(setOf(DIRECT_SHARE_CATEGORY))
        .apply {
            if (capabilityBinding != null) {
                addCapabilityBinding(capabilityBinding)
            }
        }
        .build()
}