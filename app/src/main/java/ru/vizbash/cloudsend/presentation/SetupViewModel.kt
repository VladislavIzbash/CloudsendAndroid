package ru.vizbash.cloudsend.presentation

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.vizbash.cloudsend.domain.LoginInteractor
import javax.inject.Inject

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val loginInteractor: LoginInteractor,
) : ViewModel() {
    data class State(
        val formData: FormData = FormData(),
        val loginError: Boolean = false,
    )

    data class FormData(
        val baseUrl: String = "",
        val login: String = "",
        val password: String = "",
        val deviceName: String = "${Build.BRAND} ${Build.MODEL}",
    )

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    fun onFormChange(formData: FormData) {
        _state.update {
            it.copy(formData = formData)
        }
    }

    fun onLoginClick(
        navigateToMain: () -> Unit,
    ) {
        val formData = state.value.formData

        viewModelScope.launch {
            val success = loginInteractor(
                formData.baseUrl,
                formData.login,
                formData.password,
                formData.deviceName,
            )
            _state.update { it.copy(loginError = !success) }

            if (success) {
                navigateToMain()
            }
        }
    }
}