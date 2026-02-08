package ru.vizbash.cloudsend.presentation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import ru.vizbash.cloudsend.data.document.DocumentResolver
import ru.vizbash.cloudsend.data.persistence.settings.AppSettings
import ru.vizbash.cloudsend.data.persistence.settings.PreferencesSettingsRepository
import ru.vizbash.cloudsend.data.persistence.settings.Setting
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: PreferencesSettingsRepository,
    private val documentResolver: DocumentResolver,
) : ViewModel() {
    data class State(
        val downloadDir: String?,
        val autoAccept: Boolean,
    )

    private val _state = MutableStateFlow(
        State(
            downloadDir = settingsRepository.get(AppSettings.SaveDirectoryUri)?.let { uri ->
                documentResolver.resolveName(uri)
            },
            autoAccept = settingsRepository.get(AppSettings.AutoAcceptTransfers),
        )
    )
    val state = _state.asStateFlow()

    fun onBooleanToggle(setting: Setting.Bool) {
        when (setting) {
            AppSettings.AutoAcceptTransfers -> {
                _state.update {
                    it.copy(autoAccept = !it.autoAccept)
                }
                settingsRepository.save(setting, state.value.autoAccept)
            }
            else -> {}
        }
    }

    fun onDownloadDirPicked(uri: String) {
        _state.update {
            it.copy(downloadDir = uri)
        }
        settingsRepository.save(AppSettings.SaveDirectoryUri, uri)
    }
}