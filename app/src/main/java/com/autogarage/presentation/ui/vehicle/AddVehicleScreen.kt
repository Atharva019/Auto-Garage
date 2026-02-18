package com.autogarage.presentation.ui.vehicle

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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import com.autogarage.domain.model.FuelType
import com.autogarage.domain.model.TransmissionType
import com.autogarage.presentation.ui.components.*
import com.autogarage.presentation.ui.theme.Spacing
import com.autogarage.viewmodel.AddVehicleUiEvent
import com.autogarage.viewmodel.AddVehicleViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVehicleScreen(
    viewModel: AddVehicleViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onVehicleAdded: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showFuelTypeDialog by remember { mutableStateOf(false) }
    var showTransmissionDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is AddVehicleUiEvent.VehicleAdded -> {
                    snackbarHostState.showSnackbar("Vehicle added successfully")
                    onVehicleAdded(event.vehicleId)
                }
                is AddVehicleUiEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Vehicle") },
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
                value = uiState.registrationNumber,
                onValueChange = viewModel::onRegistrationNumberChange,
                label = "Registration Number *",
                placeholder = "e.g., MH12AB1234",
                leadingIcon = Icons.Default.DirectionsCar,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters
                ),
                isError = uiState.registrationNumberError != null,
                errorMessage = uiState.registrationNumberError,
                supportingText = "Enter without spaces or hyphens",
                enabled = !uiState.isSaving
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                GMTextField(
                    value = uiState.make,
                    onValueChange = viewModel::onMakeChange,
                    label = "Make *",
                    placeholder = "e.g., Honda",
                    leadingIcon = Icons.Default.DirectionsCar,
                    isError = uiState.makeError != null,
                    errorMessage = uiState.makeError,
                    enabled = !uiState.isSaving,
                    modifier = Modifier.weight(1f)
                )

                GMTextField(
                    value = uiState.model,
                    onValueChange = viewModel::onModelChange,
                    label = "Model *",
                    placeholder = "e.g., City",
                    isError = uiState.modelError != null,
                    errorMessage = uiState.modelError,
                    enabled = !uiState.isSaving,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                GMTextField(
                    value = uiState.year,
                    onValueChange = viewModel::onYearChange,
                    label = "Year *",
                    placeholder = "2020",
                    leadingIcon = Icons.Default.CalendarToday,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = uiState.yearError != null,
                    errorMessage = uiState.yearError,
                    enabled = !uiState.isSaving,
                    modifier = Modifier.weight(1f)
                )

                GMTextField(
                    value = uiState.color,
                    onValueChange = viewModel::onColorChange,
                    label = "Color",
                    placeholder = "e.g., Red",
                    leadingIcon = Icons.Default.Palette,
                    enabled = !uiState.isSaving,
                    modifier = Modifier.weight(1f)
                )
            }

            // Vehicle Specifications
            SectionHeader(title = "Vehicle Specifications")

            // Fuel Type Selection
            OutlinedCard(
                onClick = { showFuelTypeDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                ListItem(
                    headlineContent = {
                        Text(uiState.fuelType?.name ?: "Select Fuel Type (Optional)")
                    },
                    leadingContent = {
                        Icon(Icons.Default.LocalGasStation, contentDescription = null)//LocalGasStation
                    },
                    trailingContent = {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                )
            }

            // Transmission Selection
            OutlinedCard(
                onClick = { showTransmissionDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                ListItem(
                    headlineContent = {
                        Text(uiState.transmission?.name ?: "Select Transmission (Optional)")
                    },
                    leadingContent = {
                        Icon(Icons.Default.Settings, contentDescription = null)
                    },
                    trailingContent = {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                )
            }

            GMTextField(
                value = uiState.currentKilometers,
                onValueChange = viewModel::onCurrentKilometersChange,
                label = "Current Kilometers",
                placeholder = "0",
                leadingIcon = Icons.Default.Speed,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                enabled = !uiState.isSaving
            )

            // Additional Details
            SectionHeader(title = "Additional Details")

            GMTextField(
                value = uiState.engineNumber,
                onValueChange = viewModel::onEngineNumberChange,
                label = "Engine Number",
                placeholder = "Optional",
                leadingIcon = Icons.Default.Engineering,
                enabled = !uiState.isSaving
            )

            GMTextField(
                value = uiState.chassisNumber,
                onValueChange = viewModel::onChassisNumberChange,
                label = "Chassis Number",
                placeholder = "Optional",
                leadingIcon = Icons.Default.Fingerprint,
                enabled = !uiState.isSaving
            )

            // Documents
            SectionHeader(title = "Document Expiry Dates")

            GMTextField(
                value = uiState.insuranceExpiryDate,
                onValueChange = viewModel::onInsuranceExpiryChange,
                label = "Insurance Expiry",
                placeholder = "YYYY-MM-DD (Optional)",
                leadingIcon = Icons.Default.Shield,
                enabled = !uiState.isSaving
            )

            GMTextField(
                value = uiState.pucExpiryDate,
                onValueChange = viewModel::onPucExpiryChange,
                label = "PUC Expiry",
                placeholder = "YYYY-MM-DD (Optional)",
                leadingIcon = Icons.Default.VerifiedUser,
                enabled = !uiState.isSaving
            )

            GMTextField(
                value = uiState.notes,
                onValueChange = viewModel::onNotesChange,
                label = "Notes",
                placeholder = "Any additional notes (Optional)",
                leadingIcon = Icons.Default.Notes,
                singleLine = false,
                maxLines = 3,
                enabled = !uiState.isSaving
            )

            Spacer(modifier = Modifier.height(Spacing.medium))

            // Save Button
            GMButton(
                text = "Add Vehicle",
                onClick = viewModel::onSaveClick,
                type = ButtonType.PRIMARY,
                size = ButtonSize.LARGE,
                icon = Icons.Default.Save,
                loading = uiState.isSaving,
                modifier = Modifier.fillMaxWidth()
            )

            // Info Card
            InfoCard(
                title = "Required Fields",
                description = "Registration Number, Make, Model and Year are mandatory",
                type = InfoType.INFO,
                icon = Icons.Default.Info
            )
        }
    }

    // Fuel Type Dialog
    if (showFuelTypeDialog) {
        SelectFuelTypeDialog(
            selectedFuelType = uiState.fuelType,
            onFuelTypeSelected = { fuelType ->
                viewModel.onFuelTypeSelected(fuelType)
                showFuelTypeDialog = false
            },
            onDismiss = { showFuelTypeDialog = false }
        )
    }

    // Transmission Dialog
    if (showTransmissionDialog) {
        SelectTransmissionDialog(
            selectedTransmission = uiState.transmission,
            onTransmissionSelected = { transmission ->
                viewModel.onTransmissionSelected(transmission)
                showTransmissionDialog = false
            },
            onDismiss = { showTransmissionDialog = false }
        )
    }
}

// ===========================================================================
// Selection Dialogs
// ===========================================================================

@Composable
private fun SelectFuelTypeDialog(
    selectedFuelType: FuelType?,
    onFuelTypeSelected: (FuelType?) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Fuel Type") },
        text = {
            Column {
                ListItem(
                    headlineContent = { Text("Not Specified") },
                    modifier = Modifier.clickable { onFuelTypeSelected(null) },
                    leadingContent = {
                        RadioButton(
                            selected = selectedFuelType == null,
                            onClick = { onFuelTypeSelected(null) }
                        )
                    }
                )
                Divider()

                FuelType.values().forEach { fuelType ->
                    ListItem(
                        headlineContent = { Text(fuelType.name) },
                        modifier = Modifier.clickable { onFuelTypeSelected(fuelType) },
                        leadingContent = {
                            RadioButton(
                                selected = selectedFuelType == fuelType,
                                onClick = { onFuelTypeSelected(fuelType) }
                            )
                        }
                    )
                    Divider()
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun SelectTransmissionDialog(
    selectedTransmission: TransmissionType?,
    onTransmissionSelected: (TransmissionType?) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Transmission") },
        text = {
            Column {
                ListItem(
                    headlineContent = { Text("Not Specified") },
                    modifier = Modifier.clickable { onTransmissionSelected(null) },
                    leadingContent = {
                        RadioButton(
                            selected = selectedTransmission == null,
                            onClick = { onTransmissionSelected(null) }
                        )
                    }
                )
                Divider()

                TransmissionType.values().forEach { transmission ->
                    ListItem(
                        headlineContent = { Text(transmission.name) },
                        modifier = Modifier.clickable { onTransmissionSelected(transmission) },
                        leadingContent = {
                            RadioButton(
                                selected = selectedTransmission == transmission,
                                onClick = { onTransmissionSelected(transmission) }
                            )
                        }
                    )
                    Divider()
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
