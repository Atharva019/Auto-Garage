package com.autogarage.presentation.ui.worker

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.autogarage.domain.model.Worker
import com.autogarage.domain.model.WorkerRole
import com.autogarage.domain.model.WorkerStatus
import com.autogarage.presentation.ui.components.*
import com.autogarage.presentation.ui.icons.GarageMasterIcons
import com.autogarage.presentation.ui.theme.Spacing
import com.autogarage.viewmodel.WorkerDetailUiEvent
import com.autogarage.viewmodel.WorkerDetailViewModel
import kotlinx.coroutines.flow.collectLatest
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerDetailScreen(
    viewModel: WorkerDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is WorkerDetailUiEvent.NavigateToEditWorker -> {
                    onNavigateToEdit(event.workerId)
                }
                is WorkerDetailUiEvent.WorkerDeleted -> {
                    snackbarHostState.showSnackbar("Worker deleted")
                    onNavigateBack()
                }
                is WorkerDetailUiEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is WorkerDetailUiEvent.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Long
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Worker Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onRefresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = { viewModel.onEditWorkerClick() }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }

                    var showMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Delete Worker") },
                            onClick = {
                                showMenu = false
                                viewModel.onDeleteWorkerClick()
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = MaterialTheme.colorScheme.error
                            )
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when {
            uiState.isLoading -> {
                LoadingIndicator(message = "Loading worker details...")
            }
            uiState.error != null -> {
                EmptyState(
                    icon = Icons.Default.Error,
                    title = "Error",
                    description = uiState.error ?: "Unknown error",
                    actionText = "Retry",
                    onActionClick = { viewModel.onRefresh() }
                )
            }
            uiState.worker != null -> {
                WorkerDetailContent(
                    worker = uiState.worker!!,
                    onStatusChange = viewModel::onStatusChange,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }

    // Delete Confirmation Dialog
    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onDeleteDialogDismiss() },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Delete Worker?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                    Text("Are you sure you want to delete ${uiState.worker?.name}?")
                    Text(
                        "This action cannot be undone.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.onConfirmDelete() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    enabled = !uiState.isDeleting
                ) {
                    if (uiState.isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onError
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.onDeleteDialogDismiss() },
                    enabled = !uiState.isDeleting
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun WorkerDetailContent(
    worker: Worker,
    onStatusChange: (WorkerStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        // Header Card with Avatar
        WorkerHeaderCard(worker = worker)

        // Status Management
        SectionHeader(title = "Status Management")
        WorkerStatusCard(
            worker = worker,
            onStatusChange = onStatusChange
        )

        // Performance Stats
        SectionHeader(title = "Performance")
        WorkerStatsCard(worker = worker)

        // Personal Information
        SectionHeader(title = "Personal Information")
        WorkerPersonalInfoCard(worker = worker)

        // Employment Details
        SectionHeader(title = "Employment Details")
        WorkerEmploymentCard(worker = worker)

        // Documents (if available)
//        if (worker.aadharNumber != null || worker.panNumber != null) {
//            SectionHeader(title = "Documents")
//            WorkerDocumentsCard(worker = worker)
//        }

        Spacer(modifier = Modifier.height(Spacing.large))
    }
}

@Composable
private fun WorkerHeaderCard(worker: Worker) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Spacing.cardCornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = when (worker.role) {
                WorkerRole.MECHANIC -> MaterialTheme.colorScheme.primaryContainer
                WorkerRole.ELECTRICIAN -> MaterialTheme.colorScheme.secondaryContainer
                WorkerRole.PAINTER -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.large),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            // Avatar with role icon
            Surface(
                modifier = Modifier.size(80.dp),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = getRoleIcon(worker.role),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Worker Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = worker.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = worker.role.name.replace("_", " "),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (worker.specialization != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = worker.specialization,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkerStatusCard(
    worker: Worker,
    onStatusChange: (WorkerStatus) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            // Current Status Badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = getStatusColor(worker.status) as Color
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = getStatusIcon(worker.status),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Current Status: ${worker.status.name.replace("_", " ")}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = Spacing.small))

            Text(
                text = "Change Status",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Status change buttons
            WorkerStatus.entries.forEach { status ->
                if (status != worker.status) {
                    GMButton(
                        text = status.name.replace("_", " "),
                        onClick = { onStatusChange(status) },
                        type = ButtonType.OUTLINED,
                        size = ButtonSize.MEDIUM,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkerStatsCard(worker: Worker) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium)
        ) {
            Text(
                text = "Performance Metrics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(bottom = Spacing.small)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = Icons.Default.CheckCircle,
                    label = "Completed",
                    value = worker.completedJobs.toString()
                )
                StatItem(
                    icon = Icons.Default.Assistant,
                    label = "Active Jobs",
                    value = worker.activeJobs.toString()
                )
                if (worker.rating > 0) {
                    StatItem(
                        icon = Icons.Default.StarRate,
                        label = "Rating",
                        value = String.format("%.1f ⭐", worker.rating)
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkerPersonalInfoCard(worker: Worker) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            InfoRow(
                icon = Icons.Default.Phone,
                label = "Phone",
                value = worker.phone
            )

            if (worker.email != null) {
                InfoRow(
                    icon = Icons.Default.Email,
                    label = "Email",
                    value = worker.email
                )
            }

            if (worker.address != null) {
                InfoRow(
                    icon = Icons.Default.LocationOn,
                    label = "Address",
                    value = worker.address
                )
            }

            if (worker.emergencyContact != null) {
                InfoRow(
                    icon = Icons.Default.ContactPhone,
                    label = "Emergency Contact",
                    value = worker.emergencyContact
                )
            }
        }
    }
}

@Composable
private fun WorkerEmploymentCard(worker: Worker) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            InfoRow(
                icon = Icons.Default.CalendarToday,
                label = "Date of Joining",
                value = worker.dateOfJoining.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
            )

            InfoRow(
                icon = Icons.Default.Money,
                label = "Monthly Salary",
                value = "₹${String.format("%,.2f", worker.salary)}"
            )

            InfoRow(
                icon = Icons.Default.Work,
                label = "Role",
                value = worker.role.name.replace("_", " ")
            )
        }
    }
}

@Composable
private fun WorkerDocumentsCard(worker: Worker) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
//            if (worker.aadharNumber != null) {
//                InfoRow(
//                    icon = Icons.Default.CreditCard,
//                    label = "Aadhar Number",
//                    value = formatAadhar(worker.aadharNumber)
//                )
//            }
//
//            if (worker.panNumber != null) {
//                InfoRow(
//                    icon = Icons.Default.CreditCard,
//                    label = "PAN Number",
//                    value = worker.panNumber.uppercase()
//                )
//            }
        }
    }
}

// Helper Composables
@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

// Helper Functions
private fun getRoleIcon(role: WorkerRole) = when (role) {
    WorkerRole.MECHANIC -> GarageMasterIcons.Worker.Mechanic
    WorkerRole.ELECTRICIAN -> GarageMasterIcons.Worker.Electrician
    WorkerRole.PAINTER -> GarageMasterIcons.Worker.Painter
    WorkerRole.SUPERVISOR -> GarageMasterIcons.Worker.Supervisor
    WorkerRole.MANAGER -> GarageMasterIcons.Worker.Manager
    WorkerRole.HELPER -> GarageMasterIcons.Worker.Helper
    WorkerRole.WELDER -> GarageMasterIcons.Worker.Painter
}

private fun getStatusIcon(status: WorkerStatus) = when (status) {
    WorkerStatus.ACTIVE -> GarageMasterIcons.Status.Active
    WorkerStatus.ON_LEAVE -> GarageMasterIcons.Status.OnLeave
    WorkerStatus.INACTIVE -> GarageMasterIcons.Status.Inactive
    WorkerStatus.TERMINATED -> GarageMasterIcons.Status.Error
    WorkerStatus.RESIGNED -> GarageMasterIcons.Status.OnLeave
}

@Composable
private fun getStatusColor(status: WorkerStatus) = when (status) {
    WorkerStatus.ACTIVE -> MaterialTheme.colorScheme.primaryContainer
    WorkerStatus.ON_LEAVE -> MaterialTheme.colorScheme.secondaryContainer
    WorkerStatus.INACTIVE -> MaterialTheme.colorScheme.surfaceVariant
    WorkerStatus.TERMINATED -> MaterialTheme.colorScheme.errorContainer
    WorkerStatus.RESIGNED -> MaterialTheme.colorScheme.tertiary
}

private fun formatAadhar(aadhar: String): String {
    return if (aadhar.length == 12) {
        "${aadhar.substring(0, 4)} ${aadhar.substring(4, 8)} ${aadhar.substring(8)}"
    } else {
        aadhar
    }
}