package ru.vizbash.cloudsend.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.vizbash.cloudsend.R
import ru.vizbash.cloudsend.presentation.SetupViewModel
import ru.vizbash.cloudsend.presentation.SetupViewModel.FormData
import ru.vizbash.cloudsend.presentation.SetupViewModel.State
import ru.vizbash.cloudsend.ui.theme.CloudSendTheme

private val passwordTransformation = PasswordVisualTransformation()

@Composable
fun SetupScreen(
    modifier: Modifier = Modifier,
    viewModel: SetupViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()

    SetupScreen(
        modifier = modifier,
        state = state,
        onFormDataChange = viewModel::onFormChange,
        onConnectClick = viewModel::onLoginClick,
    )
}

@Composable
private fun SetupScreen(
    state: State,
    onFormDataChange: (FormData) -> Unit,
    onConnectClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarState = remember { SnackbarHostState() }

    val loginError =
        stringResource(R.string.screen_setup__connect_error_general).takeIf { state.loginError }

    LaunchedEffect(loginError) {
        if (loginError != null) {
            snackbarState.showSnackbar(
                message = loginError,
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.TopStart,
    ) {
        Card(
            modifier = Modifier.padding(top = 150.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(R.string.screen_setup__title),
                    style = MaterialTheme.typography.titleMedium
                )

                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(stringResource(R.string.screen_setup__label_base_url))
                    },
                    value = state.formData.baseUrl,
                    onValueChange = {
                        onFormDataChange(state.formData.copy(baseUrl = it))
                    },
                )
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(stringResource(R.string.screen_setup__label_login))
                    },
                    value = state.formData.login,
                    onValueChange = {
                        onFormDataChange(state.formData.copy(login = it))
                    }
                )
                TextField(
                    modifier = Modifier.fillMaxWidth(),

                    label = {
                        Text(stringResource(R.string.screen_setup__label_password))
                    },
                    value = state.formData.password,
                    onValueChange = {
                        onFormDataChange(state.formData.copy(password = it))
                    },
                    visualTransformation = passwordTransformation,
                )
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(stringResource(R.string.screen_setup__label_device_name))
                    },
                    value = state.formData.deviceName,
                    onValueChange = {
                        onFormDataChange(state.formData.copy(deviceName = it))
                    },
                )

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onConnectClick,
                ) {
                    Text(stringResource(R.string.screen_setup__button_connect))
                }
            }
        }

        SnackbarHost(
            modifier = Modifier
                .align(Alignment.BottomCenter),
            hostState = snackbarState,
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SetupScreenPreview() {
    CloudSendTheme {
        SetupScreen(
            state = State(
                formData = FormData(
                    baseUrl = "http://localhost:3000",
                    login = "vlad",
                    password = "password",
                    deviceName = "Pixel 9a"
                ),
            ),
            onFormDataChange = {},
            onConnectClick = {},
        )
    }
}