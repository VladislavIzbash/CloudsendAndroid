package ru.vizbash.cloudsend.domain

import android.content.Context
import android.util.Log
import androidx.core.content.pm.ShortcutManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import ru.vizbash.cloudsend.data.persistence.db.DeviceDao
import ru.vizbash.cloudsend.util.createDeviceShortcut
import javax.inject.Inject

private const val TAG = "UpdateDirectShareShortcutsInteractor"

class UpdateDirectShareShortcutsInteractor @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val deviceDao: DeviceDao,
) {
    suspend operator fun invoke() {
        try {
            val shortcuts = deviceDao.getAll()
                .map { device ->
                    createDeviceShortcut(context, device.uuid, device.name)
                }

            if (shortcuts.isNotEmpty()) {
                ShortcutManagerCompat.setDynamicShortcuts(context, shortcuts)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Cannot update direct share shortcuts: ${e.message}")
            e.printStackTrace()
        }
    }
}