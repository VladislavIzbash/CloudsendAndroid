package ru.vizbash.cloudsend

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.runBlocking
import ru.vizbash.cloudsend.domain.UpdateDirectShareShortcutsInteractor
import javax.inject.Inject

const val DIRECT_SHARE_CATEGORY = "ru.vizbash.cloudsend.category.SEND_TARGET"

@HiltAndroidApp
class CloudsendApplication : Application() {

    @Inject
    lateinit var updateDirectShareShortcutsInteractor: UpdateDirectShareShortcutsInteractor

    override fun onCreate() {
        super.onCreate()

        runBlocking {
            updateDirectShareShortcutsInteractor()
        }
    }
}