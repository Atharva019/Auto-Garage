package com.autogarage.presentation.ui.customer

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.autogarage.R
import com.autogarage.domain.model.Customer
import com.autogarage.domain.model.JobCard
import com.autogarage.domain.model.Vehicle
import com.autogarage.presentation.ui.components.*
import com.autogarage.presentation.ui.icons.GarageMasterIcons
import com.autogarage.presentation.ui.theme.Spacing
import com.autogarage.viewmodel.CustomerDetailUiEvent
import com.autogarage.viewmodel.CustomerDetailViewModel
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(
    viewModel: CustomerDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToAddVehicle: (Long) -> Unit,
    onNavigateToVehicleDetail: (Long) -> Unit,
    onNavigateToJobCardDetail: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is CustomerDetailUiEvent.NavigateToAddVehicle ->
                    onNavigateToAddVehicle(event.customerId)
                is CustomerDetailUiEvent.NavigateToVehicleDetail ->
                    onNavigateToVehicleDetail(event.vehicleId)
                is CustomerDetailUiEvent.NavigateToJobCardDetail ->
                    onNavigateToJobCardDetail(event.jobCardId)
                is CustomerDetailUiEvent.NavigateToEditCustomer -> {
                    // Navigate to edit screen (to be implemented)
                }
                is CustomerDetailUiEvent.MakePhoneCall -> {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:${event.phoneNumber}")
                    }
                    context.startActivity(intent)
                }
                is CustomerDetailUiEvent.SendEmail -> {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:${event.email}")
                    }
                    context.startActivity(intent)
                }
                is CustomerDetailUiEvent.ShowMessage ->
                    snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customer Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onRefresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = { viewModel.onEditCustomerClick() }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when {
            uiState.isLoading -> {
                LoadingIndicator(
                    message = "Loading customer details...",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }
            uiState.error != null -> {
                EmptyState(
                    icon = GarageMasterIcons.Status.Error,
                    title = "Error",
                    description = uiState.error ?: "Unknown error",
                    actionText = "Retry",
                    onActionClick = { viewModel.onRefresh() },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }
            uiState.customer != null -> {
                CustomerDetailContent(
                    customer = uiState.customer!!,
                    vehicles = uiState.vehicles,
                    serviceHistory = uiState.serviceHistory,
                    selectedTab = selectedTab,
                    onTabSelected = viewModel::onTabSelected,
                    onAddVehicleClick = viewModel::onAddVehicleClick,
                    onVehicleClick = viewModel::onVehicleClick,
                    onJobCardClick = viewModel::onJobCardClick,
                    onCallClick = viewModel::onCallClick,
                    onEmailClick = viewModel::onEmailClick,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
private fun CustomerDetailContent(
    customer: Customer,
    vehicles: List<Vehicle>,
    serviceHistory: List<JobCard>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onAddVehicleClick: () -> Unit,
    onVehicleClick: (Long) -> Unit,
    onJobCardClick: (Long) -> Unit,
    onCallClick: () -> Unit,
    onEmailClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Customer Header Card
        CustomerHeaderCard(
            customer = customer,
            onCallClick = onCallClick,
            onEmailClick = onEmailClick
        )

        // Tabs
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { onTabSelected(0) },
                text = { Text("Vehicles (${vehicles.size})") },
                icon = { Icon(GarageMasterIcons.Vehicle.Car, contentDescription = null) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { onTabSelected(1) },
                text = { Text("Service History (${serviceHistory.size})") },
                icon = { Icon(GarageMasterIcons.Service.ServiceHistory, contentDescription = null) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { onTabSelected(2) },
                text = { Text("Info") },
                icon = { Icon(GarageMasterIcons.Status.Info, contentDescription = null) }
            )
        }

        // Tab Content
        when (selectedTab) {
            0 -> VehiclesTab(
                vehicles = vehicles,
                onAddVehicleClick = onAddVehicleClick,
                onVehicleClick = onVehicleClick
            )
            1 -> ServiceHistoryTab(
                serviceHistory = serviceHistory,
                onJobCardClick = onJobCardClick
            )
            2 -> CustomerInfoTab(customer = customer)
        }
    }
}

@Composable
private fun CustomerHeaderCard(
    customer: Customer,
    onCallClick: () -> Unit,
    onEmailClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.medium),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = customer.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(Spacing.small))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = customer.phone,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    if (customer.email != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = customer.email,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                // Action Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
                    IconButton(
                        onClick = onCallClick,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            Icons.Default.Call,
                            contentDescription = "Call",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    if (customer.email != null) {
                        IconButton(
                            onClick = onEmailClick,
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = "Email",
                                tint = MaterialTheme.colorScheme.onSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.medium))
            Divider()
            Spacer(modifier = Modifier.height(Spacing.medium))

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "Total Spent",
                    value = "₹${String.format("%.0f", customer.totalSpent)}",
                    icon = GarageMasterIcons.Customer.AccountBalanceWallet//AccountBalanceWallet
                )
                StatItem(
                    label = "Loyalty Points",
                    value = customer.loyaltyPoints.toString(),
                    icon = GarageMasterIcons.Customer.loyaltyPoints
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    icon: ImageVector
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun VehiclesTab(
    vehicles: List<Vehicle>,
    onAddVehicleClick: () -> Unit,
    onVehicleClick: (Long) -> Unit
) {
    if (vehicles.isEmpty()) {
        EmptyState(
            icon = Icons.Default.DirectionsCar,
            title = "No Vehicles",
            description = "Add a vehicle to get started",
            actionText = "Add Vehicle",
            onActionClick = onAddVehicleClick
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            item {
                GMButton(
                    text = "Add Vehicle",
                    onClick = onAddVehicleClick,
                    icon = Icons.Default.Add,
                    type = ButtonType.OUTLINED,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            items(vehicles) { vehicle ->
                VehicleCard(
                    vehicle = vehicle,
                    onClick = { onVehicleClick(vehicle.id) }
                )
            }
        }
    }
}

@Composable
private fun VehicleCard(
    vehicle: Vehicle,
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${vehicle.make} ${vehicle.model}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = vehicle.registrationNumber,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${vehicle.year} • ${vehicle.currentKilometers} km",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ServiceHistoryTab(
    serviceHistory: List<JobCard>,
    onJobCardClick: (Long) -> Unit
) {
    if (serviceHistory.isEmpty()) {
        EmptyState(
            icon = Icons.Default.History,
            title = "No Service History",
            description = "No services recorded yet"
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            items(serviceHistory) { jobCard ->
                ServiceHistoryCard(
                    jobCard = jobCard,
                    onClick = { onJobCardClick(jobCard.id) }
                )
            }
        }
    }
}

@Composable
private fun ServiceHistoryCard(
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = jobCard.jobCardNumber,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${jobCard.vehicle.make} ${jobCard.vehicle.model}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatDate(jobCard.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    StatusBadge(
                        status = when (jobCard.status) {
                            com.autogarage.domain.model.JobCardStatus.PENDING ->
                                JobStatus.PENDING
                            com.autogarage.domain.model.JobCardStatus.IN_PROGRESS ->
                                JobStatus.IN_PROGRESS
                            com.autogarage.domain.model.JobCardStatus.COMPLETED ->
                                JobStatus.COMPLETED
                            com.autogarage.domain.model.JobCardStatus.CANCELLED ->
                                JobStatus.CANCELLED
                            com.autogarage.domain.model.JobCardStatus.DELIVERED ->
                                JobStatus.DELIVERED
                        }
                    )
                    if (jobCard.finalAmount > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "₹${String.format("%.0f", jobCard.finalAmount)}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomerInfoTab(customer: Customer) {
    LazyColumn(
        contentPadding = PaddingValues(Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        if (customer.address != null) {
            item {
                InfoRow(
                    label = "Address",
                    value = customer.address,
                    icon = Icons.Default.LocationOn
                )
            }
        }
        if (customer.gstNumber != null) {
            item {
                InfoRow(
                    label = "GST Number",
                    value = customer.gstNumber,
                    icon = Icons.Default.Description
                )
            }
        }
        if (customer.notes != null) {
            item {
                InfoRow(
                    label = "Notes",
                    value = customer.notes,
                    icon = Icons.Default.Notes
                )
            }
        }
        item {
            InfoRow(
                label = "Member Since",
                value = formatDate(customer.createdAt),
                icon = Icons.Default.CalendarToday
            )
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    icon: ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
            horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
