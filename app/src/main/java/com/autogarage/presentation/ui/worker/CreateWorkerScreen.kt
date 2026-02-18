package com.autogarage.presentation.ui.worker

import androidx.compose.foundation.clickable
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
import androidx.navigation.NavController
import com.autogarage.domain.model.WorkerRole
import com.autogarage.presentation.ui.components.*
import com.autogarage.presentation.ui.theme.Spacing
import com.autogarage.viewmodel.CreateWorkerUiEvent
import com.autogarage.viewmodel.CreateWorkerViewModel
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateWorkerScreen(
    viewModel: CreateWorkerViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onWorkerCreated: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showRoleDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is CreateWorkerUiEvent.WorkerCreated -> {
                    snackbarHostState.showSnackbar("Worker created successfully")
                    onWorkerCreated(event.workerId)
                }
                is CreateWorkerUiEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }

                CreateWorkerUiEvent.NavigateBackInstantly -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Worker") },
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
            // Basic Information
            SectionHeader(title = "Basic Information")

            GMTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                label = "Worker Name *",
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
                placeholder = "worker@example.com",
                leadingIcon = Icons.Default.Email,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = uiState.emailError != null,
                errorMessage = uiState.emailError,
                enabled = !uiState.isLoading
            )

            // Role Selection
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !uiState.isLoading) {
                        showRoleDialog = true
                    }
            ) {
                OutlinedTextField(
                    value = uiState.role?.name?.replace("_", " ") ?: "",
                    onValueChange = {},
                    label = { Text("Role *") },
                    placeholder = { Text("Select role") },
                    leadingIcon = {
                        Icon(Icons.Default.Work, contentDescription = null)//Work
                    },
                    trailingIcon = {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    },
                    readOnly = true,
                    enabled = false,
                    isError = uiState.roleError != null,
                    supportingText = if (uiState.roleError != null) {
                        { Text(uiState.roleError!!) }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            GMTextField(
                value = uiState.specialization,
                onValueChange = viewModel::onSpecializationChange,
                label = "Specialization",
                placeholder = "e.g., Engine Specialist, Body Work",
                leadingIcon = Icons.Default.Workspaces,
                enabled = !uiState.isLoading
            )

            // Employment Details
            SectionHeader(title = "Employment Details")

            // Date Picker Field
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !uiState.isLoading) {
                        showDatePicker = true
                    }
            ) {
                OutlinedTextField(
                    value = uiState.dateOfJoining?.format(DateTimeFormatter.ofPattern("dd MMM yyyy")) ?: "",
                    onValueChange = {},
                    label = { Text("Date of Joining *") },
                    placeholder = { Text("Select date") },
                    leadingIcon = {
                        Icon(Icons.Default.CalendarToday, contentDescription = null)//CalendarToday
                    },
                    readOnly = true,
                    enabled = false,
                    isError = uiState.dateError != null,
                    supportingText = if (uiState.dateError != null) {
                        { Text(uiState.dateError!!) }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            GMTextField(
                value = uiState.salary,
                onValueChange = viewModel::onSalaryChange,
                label = "Monthly Salary *",
                placeholder = "Enter salary amount",
                leadingIcon = Icons.Default.CurrencyRupee,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = uiState.salaryError != null,
                errorMessage = uiState.salaryError,
                enabled = !uiState.isLoading
            )

            // Additional Information
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
                value = uiState.emergencyContact,
                onValueChange = viewModel::onEmergencyContactChange,
                label = "Emergency Contact",
                placeholder = "10 digit number",
                leadingIcon = Icons.Default.ContactPhone,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(Spacing.medium))

            // Save Button
            GMButton(
                text = "Save Worker",
                onClick = viewModel::onSaveClick,
                type = ButtonType.PRIMARY,
                size = ButtonSize.LARGE,
                icon = Icons.Default.Save,
                loading = uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            // Info card
            InfoCard(
                title = "Required Fields",
                description = "Fields marked with * are mandatory. Please ensure all required information is filled correctly.",
                type = InfoType.INFO,
                icon = Icons.Default.Info
            )
        }
    }

    // Role Selection Dialog
    if (showRoleDialog) {
        AlertDialog(
            onDismissRequest = { showRoleDialog = false },
            title = { Text("Select Role") },
            text = {
                Column {
                    WorkerRole.entries.forEach { role ->
                        ListItem(
                            headlineContent = { Text(role.name.replace("_", " ")) },
                            leadingContent = {
                                RadioButton(
                                    selected = uiState.role == role,
                                    onClick = null
                                )
                            },
                            modifier = Modifier.clickable {
                                viewModel.onRoleChange(role)
                                showRoleDialog = false
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRoleDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.dateOfJoining?.toEpochDay()?.times(86400000)
                ?: System.currentTimeMillis()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = LocalDate.ofEpochDay(millis / 86400000)
                            viewModel.onDateOfJoiningChange(date)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}