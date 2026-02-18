package com.autogarage.presentation.ui.customer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import com.autogarage.presentation.ui.components.*
import com.autogarage.presentation.ui.theme.Spacing
import com.autogarage.viewmodel.CreateCustomerUiEvent
import com.autogarage.viewmodel.CreateCustomerViewModel
import kotlinx.coroutines.flow.collectLatest
import com.autogarage.R
import com.autogarage.presentation.ui.icons.GarageMasterIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCustomerScreen(
    viewModel: CreateCustomerViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onCustomerCreated: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is CreateCustomerUiEvent.CustomerCreated -> {
                    snackbarHostState.showSnackbar("Customer created successfully")
                    onCustomerCreated(event.customerId)
                }
                is CreateCustomerUiEvent.NavigateBackInstantly -> {
                    // ✅ Navigate back immediately for instant feel
                    onNavigateBack()
                }
                is CreateCustomerUiEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Customer") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            // Basic Information Section
            SectionHeader(title = "Basic Information")

            GMTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                label = "Customer Name *",
                placeholder = "Enter full name",
                leadingIcon = Icons.Default.Person,
                isError = uiState.nameError != null,
                errorMessage = uiState.nameError,
                enabled = !uiState.isLoading
            )

            GMTextField(
                value = uiState.phone,
                onValueChange = viewModel::onPhoneChange,
                label = "Phone Number *",
                placeholder = "10 digit mobile number",
                leadingIcon = Icons.Default.Phone,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = uiState.phoneError != null,
                errorMessage = uiState.phoneError,
                enabled = !uiState.isLoading
            )

            GMTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                label = "Email Address",
                placeholder = "customer@example.com",
                leadingIcon = Icons.Default.Email,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = uiState.emailError != null,
                errorMessage = uiState.emailError,
                enabled = !uiState.isLoading
            )

            // Additional Information Section
            SectionHeader(title = "Additional Information")

            GMTextField(
                value = uiState.address,
                onValueChange = viewModel::onAddressChange,
                label = "Address",
                placeholder = "Enter complete address",
                leadingIcon = Icons.Default.LocationOn,
                singleLine = false,
                maxLines = 3,
                enabled = !uiState.isLoading
            )

            GMTextField(
                value = uiState.gstNumber,
                onValueChange = viewModel::onGstNumberChange,
                label = "GST Number",
                placeholder = "Enter GST number (optional)",
                leadingIcon = GarageMasterIcons.Customer.Gst,
                enabled = !uiState.isLoading
            )

            GMTextField(
                value = uiState.notes,
                onValueChange = viewModel::onNotesChange,
                label = "Notes",
                placeholder = "Any additional notes",
                leadingIcon = GarageMasterIcons.Customer.AddNote,
                singleLine = false,
                maxLines = 4,
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(Spacing.medium))

            // Save Button
            GMButton(
                text = "Save Customer",
                onClick = viewModel::onSaveClick,
                type = ButtonType.PRIMARY,
                size = ButtonSize.LARGE,
                icon = GarageMasterIcons.Action.Save,
                loading = uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            // Info card
            InfoCard(
                title = "Required Fields",
                description = "Fields marked with * are mandatory",
                type = InfoType.INFO,
                icon = Icons.Default.Info
            )
        }
    }
}
