package com.autogarage.presentation.ui.jobcard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.autogarage.domain.model.Priority
import com.autogarage.domain.model.Worker
import com.autogarage.presentation.ui.components.*
import com.autogarage.presentation.ui.theme.Spacing
import com.autogarage.viewmodel.CreateJobCardUiEvent
import com.autogarage.viewmodel.CreateJobCardViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateJobCardScreen(
    viewModel: CreateJobCardViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onJobCardCreated: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showCustomerDialog by remember { mutableStateOf(false) }
    var showVehicleDialog by remember { mutableStateOf(false) }
    var showTechnicianDialog by remember { mutableStateOf(false) }
    var showPriorityDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is CreateJobCardUiEvent.JobCardCreated -> {
                    snackbarHostState.showSnackbar("Job card created successfully")
                    onJobCardCreated(event.jobCardId)
                }
                is CreateJobCardUiEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Job Card") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (uiState.isLoadingData) {
            LoadingIndicator(
                message = "Loading data...",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(Spacing.medium),
                verticalArrangement = Arrangement.spacedBy(Spacing.medium)
            ) {
                // Customer & Vehicle Section
                SectionHeader(title = "Customer & Vehicle")

                // Customer Selection
                OutlinedCard(
                    onClick = { showCustomerDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ListItem(
                        headlineContent = {
                            Text(uiState.selectedCustomer?.name ?: "Select Customer")
                        },
                        supportingContent = if (uiState.selectedCustomer != null) {
                            { Text(uiState.selectedCustomer!!.phone) }
                        } else null,
                        leadingContent = {
                            Icon(Icons.Default.Person, contentDescription = null)
                        },
                        trailingContent = {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    )
                }
                if (uiState.customerError != null) {
                    Text(
                        text = uiState.customerError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = Spacing.medium)
                    )
                }

                // Vehicle Selection
                OutlinedCard(
                    onClick = {
                        if (uiState.selectedCustomer != null) showVehicleDialog = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.selectedCustomer != null
                ) {
                    ListItem(
                        headlineContent = {
                            Text(
                                if (uiState.selectedVehicle != null) {
                                    "${uiState.selectedVehicle!!.make} ${uiState.selectedVehicle!!.model}"
                                } else {
                                    "Select Vehicle"
                                }
                            )
                        },
                        supportingContent = if (uiState.selectedVehicle != null) {
                            { Text(uiState.selectedVehicle!!.registrationNumber) }
                        } else null,
                        leadingContent = {
                            Icon(Icons.Default.DirectionsCarFilled, contentDescription = null)
                        },
                        trailingContent = {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    )
                }
                if (uiState.vehicleError != null) {
                    Text(
                        text = uiState.vehicleError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = Spacing.medium)
                    )
                }

                // Job Details Section
                SectionHeader(title = "Job Details")

                GMTextField(
                    value = uiState.currentKilometers,
                    onValueChange = viewModel::onCurrentKilometersChange,
                    label = "Current Kilometers *",
                    placeholder = "Enter current odometer reading",
                    leadingIcon = Icons.Default.Speed,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = uiState.kilometersError != null,
                    errorMessage = uiState.kilometersError,
                    enabled = !uiState.isCreating
                )

                GMTextField(
                    value = uiState.customerComplaints,
                    onValueChange = viewModel::onComplaintsChange,
                    label = "Customer Complaints *",
                    placeholder = "Describe the issues reported by customer",
                    leadingIcon = Icons.Default.Report,
                    singleLine = false,
                    maxLines = 4,
                    isError = uiState.complaintsError != null,
                    errorMessage = uiState.complaintsError,
                    enabled = !uiState.isCreating
                )

                GMTextField(
                    value = uiState.mechanicObservations,
                    onValueChange = viewModel::onObservationsChange,
                    label = "Mechanic Observations",
                    placeholder = "Initial inspection notes (optional)",
                    leadingIcon = Icons.Default.Visibility,
                    singleLine = false,
                    maxLines = 3,
                    enabled = !uiState.isCreating
                )

                // Assignment Section
                SectionHeader(title = "Assignment & Priority")

                // Technician Selection
                OutlinedCard(
                    onClick = { showTechnicianDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ListItem(
                        headlineContent = {
                            Text(uiState.selectedTechnician?.name ?: "Assign Technician *")
                        },
                        supportingContent = if (uiState.selectedTechnician != null) {
                            { Text(uiState.selectedTechnician!!.role.name) }
                        } else null,
                        leadingContent = {
                            Icon(Icons.Default.Engineering, contentDescription = null)//Engineering
                        },
                        trailingContent = {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    )
                }

                // ✅ Show technician count
                if (uiState.technicians.isNotEmpty()) {
                    Text(
                        text = "${uiState.technicians.size} active technician(s) available",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = Spacing.medium)
                    )
                }

                // Priority Selection
                OutlinedCard(
                    onClick = { showPriorityDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ListItem(
                        headlineContent = {
                            Text("Priority: ${uiState.priority.name}")
                        },
                        leadingContent = {
                            Icon(Icons.Default.Flag, contentDescription = null)//Flag
                        },
                        trailingContent = {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    )
                }

                GMTextField(
                    value = uiState.estimatedCompletionDate,
                    onValueChange = viewModel::onEstimatedCompletionDateChange,
                    label = "Estimated Completion Date",
                    placeholder = "YYYY-MM-DD (optional)",
                    leadingIcon = Icons.Default.CalendarToday,
                    enabled = !uiState.isCreating
                )

                Spacer(modifier = Modifier.height(Spacing.medium))

                // Create Button
                GMButton(
                    text = "Create Job Card",
                    onClick = viewModel::onCreateJobCard,
                    type = ButtonType.PRIMARY,
                    size = ButtonSize.LARGE,
                    icon = Icons.Default.Add,
                    loading = uiState.isCreating,
                    modifier = Modifier.fillMaxWidth()
                )

                // Info Card
                InfoCard(
                    title = "Required Fields",
                    description = "Customer, Vehicle, Kilometers and Complaints are mandatory",
                    type = InfoType.INFO,
                    icon = Icons.Default.Info
                )
            }
        }
    }

    // Dialogs
    if (showCustomerDialog) {
        SelectCustomerDialog(
            customers = uiState.customers,
            onCustomerSelected = { customer ->
                viewModel.onCustomerSelected(customer)
                showCustomerDialog = false
            },
            onDismiss = { showCustomerDialog = false }
        )
    }

    if (showVehicleDialog && uiState.selectedCustomer != null) {
        SelectVehicleDialog(
            vehicles = uiState.customerVehicles,
            onVehicleSelected = { vehicle ->
                viewModel.onVehicleSelected(vehicle)
                showVehicleDialog = false
            },
            onDismiss = { showVehicleDialog = false }
        )
    }

    if (showTechnicianDialog) {
        SelectTechnicianDialog(
            technicians = uiState.technicians,
            selectedTechnician = uiState.selectedTechnician,
            onTechnicianSelected = { technician ->
                viewModel.onTechnicianSelected(technician)
                showTechnicianDialog = false
            },
            onDismiss = { showTechnicianDialog = false }
        )
    }

    if (showPriorityDialog) {
        SelectPriorityDialog(
            selectedPriority = uiState.priority,
            onPrioritySelected = { priority ->
                viewModel.onPrioritySelected(priority)
                showPriorityDialog = false
            },
            onDismiss = { showPriorityDialog = false }
        )
    }
}

// ===========================================================================
// Selection Dialogs
// ===========================================================================

@Composable
private fun SelectCustomerDialog(
    customers: List<com.autogarage.domain.model.Customer>,
    onCustomerSelected: (com.autogarage.domain.model.Customer) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Customer") },
        text = {
            LazyColumn {
                items(customers) { customer ->
                    ListItem(
                        headlineContent = { Text(customer.name) },
                        supportingContent = { Text(customer.phone) },
                        modifier = Modifier.clickable { onCustomerSelected(customer) }
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
private fun SelectVehicleDialog(
    vehicles: List<com.autogarage.domain.model.Vehicle>,
    onVehicleSelected: (com.autogarage.domain.model.Vehicle) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Vehicle") },
        text = {
            if (vehicles.isEmpty()) {
                Text("No vehicles found for this customer")
            } else {
                LazyColumn {
                    items(vehicles) { vehicle ->
                        ListItem(
                            headlineContent = { Text("${vehicle.make} ${vehicle.model}") },
                            supportingContent = { Text(vehicle.registrationNumber) },
                            modifier = Modifier.clickable { onVehicleSelected(vehicle) }
                        )
                        Divider()
                    }
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
private fun SelectTechnicianDialog(
    technicians: List<Worker>,
    selectedTechnician: Worker?,
    onTechnicianSelected: (Worker?) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assign Technician") },
        text = {
            if (technicians.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.medium),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Engineering,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(Spacing.small))
                    Text("No active technicians available")
                }
            } else {
                LazyColumn {
                    // Option to not assign technician
                    item {
                        ListItem(
                            headlineContent = { Text("No Technician (Assign Later)") },
                            supportingContent = { Text("Job card will be unassigned") },
                            modifier = Modifier.clickable {
                                onTechnicianSelected(null)
                            },
                            leadingContent = {
                                RadioButton(
                                    selected = selectedTechnician == null,
                                    onClick = { onTechnicianSelected(null) }
                                )
                            }
                        )
                        Divider()
                    }

                    // List of technicians
                    items(technicians) { technician ->
                        ListItem(
                            headlineContent = { Text(technician.name) },
                            supportingContent = {
                                Text("${technician.role.name} • ${technician.activeJobs} active jobs")
                            },
                            modifier = Modifier.clickable { onTechnicianSelected(technician) },
                            leadingContent = {
                                RadioButton(
                                    selected = selectedTechnician?.id == technician.id,
                                    onClick = { onTechnicianSelected(technician) }
                                )
                            }
                        )
                        Divider()
                    }
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
private fun SelectPriorityDialog(
    selectedPriority: Priority,
    onPrioritySelected: (Priority) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Priority") },
        text = {
            Column {
                Priority.values().forEach { priority ->
                    ListItem(
                        headlineContent = { Text(priority.name) },
                        modifier = Modifier.clickable { onPrioritySelected(priority) },
                        leadingContent = {
                            RadioButton(
                                selected = selectedPriority == priority,
                                onClick = { onPrioritySelected(priority) }
                            )
                        }
                    )
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