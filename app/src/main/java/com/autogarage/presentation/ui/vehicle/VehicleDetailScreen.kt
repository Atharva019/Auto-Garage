package com.autogarage.presentation.ui.vehicle

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.autogarage.domain.model.JobCard
import com.autogarage.domain.model.Vehicle
import com.autogarage.presentation.ui.components.*
import com.autogarage.presentation.ui.theme.Spacing
import com.autogarage.viewmodel.VehicleDetailUiEvent
import com.autogarage.viewmodel.VehicleDetailViewModel
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleDetailScreen(
    viewModel: VehicleDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    onNavigateToJobCard: (Long) -> Unit,
    onNavigateToCreateJobCard: (Long) -> Unit,
    onNavigateToCustomer: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is VehicleDetailUiEvent.NavigateToEdit -> onNavigateToEdit(event.vehicleId)
                is VehicleDetailUiEvent.NavigateToJobCard -> onNavigateToJobCard(event.jobCardId)
                is VehicleDetailUiEvent.NavigateToCreateJobCard -> onNavigateToCreateJobCard(event.vehicleId)
                is VehicleDetailUiEvent.NavigateToCustomer -> onNavigateToCustomer(event.customerId)
                is VehicleDetailUiEvent.VehicleDeleted -> {
                    snackbarHostState.showSnackbar("Vehicle deleted successfully")
                    onNavigateBack()
                }
                is VehicleDetailUiEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.vehicle?.let {
                            "${it.make} ${it.model}"
                        } ?: "Vehicle Details"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = viewModel::onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = viewModel::onDeleteClick) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::onAddJobCardClick,
                containerColor = MaterialTheme.colorScheme.secondary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Job Card")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when {
            uiState.isLoading -> {
                LoadingIndicator(message = "Loading vehicle details...")
            }
            uiState.error != null -> {
                EmptyState(
                    icon = Icons.Default.Error,
                    title = "Error",
                    description = uiState.error ?: "Unknown error",
                    actionText = "Retry",
                    onActionClick = viewModel::onRefresh
                )
            }
            uiState.vehicle != null -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(Spacing.medium),
                    verticalArrangement = Arrangement.spacedBy(Spacing.medium)
                ) {
                    // Vehicle Header Card
                    item {
                        VehicleHeaderCard(
                            vehicle = uiState.vehicle!!,
                            onCustomerClick = viewModel::onCustomerClick
                        )
                    }

                    // Vehicle Specifications
                    item {
                        SectionHeader(title = "Vehicle Specifications")
                    }

                    item {
                        VehicleSpecsCard(vehicle = uiState.vehicle!!)
                    }

                    // Documents & Expiry
                    item {
                        SectionHeader(title = "Documents & Expiry")
                    }

                    item {
                        DocumentsCard(vehicle = uiState.vehicle!!)
                    }

                    // Additional Info
                    if (uiState.vehicle!!.notes != null ||
                        uiState.vehicle!!.engineNumber != null ||
                        uiState.vehicle!!.chassisNumber != null) {
                        item {
                            SectionHeader(title = "Additional Information")
                        }

                        item {
                            AdditionalInfoCard(vehicle = uiState.vehicle!!)
                        }
                    }

                    // Service History
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SectionHeader(title = "Service History")
                            Text(
                                text = "${uiState.jobCards.size} jobs",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (uiState.isLoadingJobCards) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Spacing.large),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    } else if (uiState.jobCards.isEmpty()) {
                        item {
                            InfoCard(
                                title = "No Service History",
                                description = "No job cards found for this vehicle. Create one to start tracking services.",
                                type = InfoType.INFO,
                                icon = Icons.Default.Info,
                                actionText = "Create Job Card",
                                onActionClick = viewModel::onAddJobCardClick
                            )
                        }
                    } else {
                        items(uiState.jobCards, key = { it.id }) { jobCard ->
                            JobCardItem(
                                jobCard = jobCard,
                                onClick = { viewModel.onJobCardClick(jobCard.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onDeleteCancel,
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Delete Vehicle?") },
            text = {
                Text(
                    "Are you sure you want to delete this vehicle? This action cannot be undone and will also delete all associated service records."
                )
            },
            confirmButton = {
                Button(
                    onClick = viewModel::onDeleteConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDeleteCancel) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ===========================================================================
// Vehicle Header Card
// ===========================================================================
@Composable
private fun VehicleHeaderCard(
    vehicle: Vehicle,
    onCustomerClick: () -> Unit
) {
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
            // Registration Number - Prominent Display
            Surface(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primary
            ) {
                Text(
                    text = vehicle.registrationNumber,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(Spacing.medium))

            // Vehicle Name
            Text(
                text = "${vehicle.make} ${vehicle.model}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Text(
                text = "${vehicle.year}${vehicle.color?.let { " • $it" } ?: ""}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(Spacing.medium))

            // Customer Link
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onCustomerClick),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier.padding(Spacing.small),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "View Customer Details",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null
                    )
                }
            }
        }
    }
}

// ===========================================================================
// Vehicle Specs Card
// ===========================================================================
@Composable
private fun VehicleSpecsCard(vehicle: Vehicle) {
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
            vehicle.fuelType?.let {
                SpecRow(
                    icon = Icons.Default.LocalGasStation,
                    label = "Fuel Type",
                    value = it.name
                )
            }

            vehicle.transmission?.let {
                SpecRow(
                    icon = Icons.Default.Settings,
                    label = "Transmission",
                    value = it.name
                )
            }

            SpecRow(
                icon = Icons.Default.Speed,
                label = "Current Kilometers",
                value = "${vehicle.currentKilometers} km"
            )
        }
    }
}

// ===========================================================================
// Documents Card
// ===========================================================================
@Composable
private fun DocumentsCard(vehicle: Vehicle) {
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
            vehicle.insuranceExpiryDate?.let { date ->
                ExpiryRow(
                    icon = Icons.Default.Shield,
                    label = "Insurance Expiry",
                    date = date
                )
            } ?: run {
                Text(
                    text = "Insurance: Not specified",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            vehicle.pucExpiryDate?.let { date ->
                ExpiryRow(
                    icon = Icons.Default.VerifiedUser,
                    label = "PUC Expiry",
                    date = date
                )
            } ?: run {
                Text(
                    text = "PUC: Not specified",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ===========================================================================
// Additional Info Card
// ===========================================================================
@Composable
private fun AdditionalInfoCard(vehicle: Vehicle) {
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
            vehicle.engineNumber?.let {
                SpecRow(
                    icon = Icons.Default.Engineering,
                    label = "Engine Number",
                    value = it
                )
            }

            vehicle.chassisNumber?.let {
                SpecRow(
                    icon = Icons.Default.Fingerprint,
                    label = "Chassis Number",
                    value = it
                )
            }

            vehicle.notes?.let {
                Column(modifier = Modifier.padding(top = Spacing.small)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.Notes,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Notes",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

// ===========================================================================
// Spec Row Component
// ===========================================================================
@Composable
fun SpecRow(
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
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// ===========================================================================
// Expiry Row Component
// ===========================================================================
@Composable
fun ExpiryRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    date: String
) {
    val isExpired = isDateExpired(date)
    val isExpiringSoon = isDateExpiringSoon(date)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = when {
                isExpired -> MaterialTheme.colorScheme.error
                isExpiringSoon -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.primary
            },
            modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = formatDate(date),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = when {
                    isExpired -> MaterialTheme.colorScheme.error
                    isExpiringSoon -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        }

        // Status Badge
        Surface(
            shape = MaterialTheme.shapes.small,
            color = when {
                isExpired -> MaterialTheme.colorScheme.errorContainer
                isExpiringSoon -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.primaryContainer
            }
        ) {
            Text(
                text = when {
                    isExpired -> "Expired"
                    isExpiringSoon -> "Expiring Soon"
                    else -> "Valid"
                },
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = when {
                    isExpired -> MaterialTheme.colorScheme.onErrorContainer
                    isExpiringSoon -> MaterialTheme.colorScheme.onTertiaryContainer
                    else -> MaterialTheme.colorScheme.onPrimaryContainer
                },
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

// ===========================================================================
// Job Card Item Component
// ===========================================================================
@Composable
fun JobCardItem(
    jobCard: JobCard,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Job Card Number
                Text(
                    text = "Job #${jobCard.jobCardNumber}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                // Date
                Text(
                    text = formatTimestamp(jobCard.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Status Badge
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = getJobStatusColor(jobCard.status.name).containerColor
                ) {
                    Text(
                        text = jobCard.status.name,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = getJobStatusColor(jobCard.status.name).contentColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Amount (if available - adjust based on your JobCard model)
                getTotalAmount(jobCard)?.let { amount ->
                    Text(
                        text = "₹${String.format("%.2f", amount)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun getTotalAmount(jobCard: JobCard): Double? {
    return try {
        if (jobCard::class.java.declaredFields.any { it.name == "totalAmount" }) {
            val totalAmountField = jobCard::class.java.getDeclaredField("totalAmount")
            totalAmountField.isAccessible = true
            totalAmountField.getDouble(jobCard)
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}


// ===========================================================================
// Helper Functions
// ===========================================================================

private fun isDateExpired(dateStr: String): Boolean {
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = format.parse(dateStr)
        date?.before(Date()) ?: false
    } catch (e: Exception) {
        false
    }
}

private fun isDateExpiringSoon(dateStr: String): Boolean {
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = format.parse(dateStr)
        val today = Date()
        val thirtyDaysFromNow = Calendar.getInstance().apply {
            time = today
            add(Calendar.DAY_OF_YEAR, 30)
        }.time

        date != null && date.after(today) && date.before(thirtyDaysFromNow)
    } catch (e: Exception) {
        false
    }
}

private fun formatDate(dateStr: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateStr)
        date?.let { outputFormat.format(it) } ?: dateStr
    } catch (e: Exception) {
        dateStr
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val format = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return format.format(Date(timestamp))
}

@Composable
private fun getJobStatusColor(status: String): StatusColors {
    return when (status.uppercase()) {
        "PENDING" -> StatusColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
        "IN_PROGRESS", "IN PROGRESS" -> StatusColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        )
        "COMPLETED" -> StatusColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
        "CANCELLED" -> StatusColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
        else -> StatusColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private data class StatusColors(
    val containerColor: androidx.compose.ui.graphics.Color,
    val contentColor: androidx.compose.ui.graphics.Color
){}

