package com.autogarage.presentation.ui.jobcard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.autogarage.domain.model.JobCard
import com.autogarage.domain.model.JobCardPart
import com.autogarage.domain.model.JobCardService
import com.autogarage.domain.model.JobCardStatus
import com.autogarage.domain.model.Priority
import com.autogarage.presentation.ui.components.*
import com.autogarage.presentation.ui.theme.Spacing
import com.autogarage.viewmodel.JobCardDetailUiEvent
import com.autogarage.viewmodel.JobCardDetailViewModel
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobCardDetailScreen(
    viewModel: JobCardDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToInvoice: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val availableParts by viewModel.availableParts.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val searchQuery by viewModel.partSearchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showStatusDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is JobCardDetailUiEvent.NavigateToInvoice ->
                    onNavigateToInvoice(event.invoiceId)
                is JobCardDetailUiEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is JobCardDetailUiEvent.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Long
                    )
                }
                else -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Job Card Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // ✅ Show subtle loading indicator in app bar
                    AnimatedVisibility(
                        visible = uiState.isUpdatingStatus ||
                                uiState.isAddingService ||
                                uiState.isAddingPart,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 8.dp),
                            strokeWidth = 2.dp
                        )
                    }

                    IconButton(onClick = { viewModel.onEditJobCard() }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when {
            uiState.isLoading && uiState.jobCard == null -> {
                LoadingIndicator(message = "Loading job card...")
            }
            uiState.error != null && uiState.jobCard == null -> {
                EmptyState(
                    icon = Icons.Default.Error,
                    title = "Error",
                    description = uiState.error ?: "Unknown error",
                    actionText = "Retry",
                    onActionClick = { viewModel.onRefresh() }
                )
            }
            uiState.jobCard != null -> {
                JobCardContent(
                    jobCard = uiState.jobCard!!,
                    services = uiState.services,
                    parts = uiState.parts,
                    hasInvoice = uiState.hasInvoice,
                    isGeneratingInvoice = uiState.isGeneratingInvoice,
                    isUpdatingStatus = uiState.isUpdatingStatus,
                    isAddingService = uiState.isAddingService,
                    isAddingPart = uiState.isAddingPart,
                    onStatusChangeClick = { showStatusDialog = true },
                    onAddService = viewModel::onAddServiceClick,
                    onRemoveService = viewModel::onRemoveService,
                    onAddPart = viewModel::onAddPartClick,
                    onRemovePart = viewModel::onRemovePart,
                    onGenerateInvoice = viewModel::onGenerateInvoiceClick,
                    onViewInvoice = viewModel::onViewInvoiceClick,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }

    // ========== DIALOGS - PLACED OUTSIDE SCAFFOLD ==========

    // Generate Invoice Dialog
    if (uiState.showInvoiceDialog) {
        GenerateInvoiceDialog(
            jobCard = uiState.jobCard!!,
            discountPercentage = uiState.discountPercentage,
            notes = uiState.invoiceNotes,
            onDiscountChange = viewModel::onDiscountPercentageChange,
            onNotesChange = viewModel::onInvoiceNotesChange,
            onConfirm = viewModel::onConfirmGenerateInvoice,
            onDismiss = viewModel::onInvoiceDialogDismiss
        )
    }

    // Add Service Dialog
    if (uiState.showAddServiceDialog) {
        AddServiceDialog(
            onDismiss = viewModel::onServiceDialogDismiss,
            onConfirm = viewModel::onAddServiceConfirm
        )
    }

    // Add Part Dialog
    if (uiState.showAddPartDialog) {
        AddPartDialog(
            onDismiss = viewModel::onPartDialogDismiss,
            availableParts = availableParts,
            categories = categories,
            searchQuery = searchQuery,
            selectedCategory = selectedCategory,
            onSearchQueryChange = viewModel::onPartSearchQueryChange,
            onCategorySelected = viewModel::onCategorySelected,
            onPartSelected = viewModel::onPartSelected
        )
    }

    // JobCard Status Change
    if (showStatusDialog) {
        StatusChangeDialog(
            currentStatus = uiState.jobCard?.status ?: JobCardStatus.PENDING,
            onStatusSelected = { newStatus ->
                viewModel.onStatusChange(newStatus)
                showStatusDialog = false
            },
            onDismiss = { showStatusDialog = false }
        )
    }

    // Enhanced Add Part Dialog
    if (uiState.showAddPartDialog) {
        AddPartDialog(
            availableParts = availableParts,
            categories = categories,
            searchQuery = searchQuery,
            selectedCategory = selectedCategory,
            onSearchQueryChange = viewModel::onPartSearchQueryChange,
            onCategorySelected = viewModel::onCategorySelected,
            onPartSelected = viewModel::onPartSelected,
            onDismiss = viewModel::onPartDialogDismiss
        )
    }
}

// ===========================================================================
// Job Card Content - Main Scrollable Content
// ===========================================================================
@Composable
private fun JobCardContent(
    jobCard: JobCard,
    services: List<JobCardService>,
    parts: List<JobCardPart>,
    hasInvoice: Boolean,
    isGeneratingInvoice: Boolean,
    isUpdatingStatus: Boolean,
    isAddingService: Boolean,
    isAddingPart: Boolean,
    onStatusChangeClick: () -> Unit,
    onAddService: () -> Unit,
    onRemoveService: (JobCardService) -> Unit,
    onAddPart: () -> Unit,
    onRemovePart: (JobCardPart) -> Unit,
    onGenerateInvoice: () -> Unit,
    onViewInvoice: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        // Job Card Header
        JobCardHeaderCard(
            jobCard = jobCard,
            onStatusChangeClick = onStatusChangeClick,
            isUpdatingStatus = isUpdatingStatus
        )

        // Services Section with loading indicator
        SectionHeader(
            title = "Services",
            actionText = "Add Service",
            onActionClick = onAddService
        )
        ServicesSectionWithLoading(
            services = services,
            isAdding = isAddingService,
            onRemove = onRemoveService
        )

        // Parts Section with loading indicator
        SectionHeader(
            title = "Parts",
            actionText = "Add Part",
            onActionClick = onAddPart
        )
        PartsSectionWithLoading(
            parts = parts,
            isAdding = isAddingPart,
            onRemove = onRemovePart
        )

        // Vehicle Details
        SectionHeader(title = "Vehicle Details")
        VehicleInfoCard(jobCard = jobCard)

        // Services & Parts Section
//        SectionHeader(title = "Services & Parts")
//        ServicesAndPartsSection(
//            services = services,
//            parts = parts,
//            onAddService = onAddService,
//            onAddPart = onAddPart,
//            onRemoveService = onRemoveService,
//            onRemovePart = onRemovePart
//        )

        // Cost Summary
        SectionHeader(title = "Cost Summary")
        CostSummaryCard(jobCard = jobCard)

        // Invoice Section
        SectionHeader(title = "Invoice")
        InvoiceActionCard(
            jobCard = jobCard,
            hasInvoice = hasInvoice,
            isGeneratingInvoice = isGeneratingInvoice,
            onGenerateInvoice = onGenerateInvoice,
            onViewInvoice = onViewInvoice
        )

        Spacer(modifier = Modifier.height(Spacing.large))
    }
}

@Composable
fun JobCardHeaderCard(
    jobCard: JobCard,
    onStatusChangeClick: () -> Unit = {},  // ✅ ADD: Default parameter
    isUpdatingStatus: Boolean = false       // ✅ ADD: Default parameter
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
                .padding(Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            // Job Card Number & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = jobCard.jobCardNumber,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                StatusBadge(
                    status = when (jobCard.status) {
                        JobCardStatus.PENDING -> JobStatus.PENDING
                        JobCardStatus.IN_PROGRESS -> JobStatus.IN_PROGRESS
                        JobCardStatus.COMPLETED -> JobStatus.COMPLETED
                        JobCardStatus.CANCELLED -> JobStatus.CANCELLED
                        JobCardStatus.DELIVERED -> JobStatus.DELIVERED
                    }
                )
            }

            Divider()

            // Vehicle Info
            Text(
                text = "${jobCard.vehicle.make} ${jobCard.vehicle.model}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = jobCard.vehicle.registrationNumber,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            // Status Change Button (only if not delivered or cancelled)
            if (jobCard.status != JobCardStatus.DELIVERED &&
                jobCard.status != JobCardStatus.CANCELLED) {
                Spacer(modifier = Modifier.height(Spacing.small))
                GMButton(
                    text = "Update Status",
                    onClick = onStatusChangeClick,
                    type = ButtonType.OUTLINED,
                    size = ButtonSize.MEDIUM,
                    icon = Icons.Default.Edit,
                    loading = isUpdatingStatus,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// ===========================================================================
// ✅ ADD: Status Change Dialog
// ===========================================================================

// ✅ Status Section with inline loading
@Composable
private fun StatusSectionWithLoading(
    jobCard: JobCard,
    isUpdating: Boolean,
    onStatusChange: (JobCardStatus) -> Unit
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
                .padding(Spacing.medium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Current Status: ${jobCard.status.name}",
                    style = MaterialTheme.typography.titleMedium
                )

                // ✅ Show inline loading indicator
                AnimatedVisibility(
                    visible = isUpdating,
                    enter = fadeIn() + expandHorizontally(),
                    exit = fadeOut() + shrinkHorizontally()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Updating...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Status change buttons (disabled while updating)
            // ... rest of status UI
        }
    }
}

// ✅ Services Section with inline loading
@Composable
private fun ServicesSectionWithLoading(
    services: List<JobCardService>,
    isAdding: Boolean,
    onRemove: (JobCardService) -> Unit
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
            // ✅ Show loading indicator while adding
            AnimatedVisibility(
                visible = isAdding,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (services.isEmpty() && !isAdding) {
                Text(
                    text = "No services added yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(Spacing.medium)
                )
            } else {
                services.forEach { service ->
                    ServiceItem(
                        service = service,
                        onRemove = { onRemove(service) }
                    )
                }
            }
        }
    }
}

// ✅ Parts Section with inline loading
@Composable
private fun PartsSectionWithLoading(
    parts: List<JobCardPart>,
    isAdding: Boolean,
    onRemove: (JobCardPart) -> Unit
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
            // ✅ Show loading indicator while adding
            AnimatedVisibility(
                visible = isAdding,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (parts.isEmpty() && !isAdding) {
                Text(
                    text = "No parts added yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(Spacing.medium)
                )
            } else {
                parts.forEach { part ->
                    PartItem(
                        part = part,
                        onRemove = { onRemove(part) }
                    )
                }
            }
        }
    }
}


@Composable
private fun StatusChangeDialog(
    currentStatus: JobCardStatus,
    onStatusSelected: (JobCardStatus) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Edit,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = { Text("Update Job Card Status") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                Text(
                    "Select new status:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(Spacing.small))

                JobCardStatus.values().forEach { status ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        onClick = { onStatusSelected(status) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (status == currentStatus)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Spacing.medium),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Text(
                                text = status.name.replace("_", " "),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (status == currentStatus)
                                    FontWeight.Bold
                                else
                                    FontWeight.Normal
                            )
                            if (status == currentStatus) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun InvoiceActionCard(
    jobCard: JobCard,
    hasInvoice: Boolean,
    isGeneratingInvoice: Boolean,
    onGenerateInvoice: () -> Unit,
    onViewInvoice: () -> Unit
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
            verticalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            when {
                hasInvoice -> {
                    // Invoice exists - show view button
                    InfoCard(
                        title = "Invoice Generated",
                        description = "Invoice has been generated for this job card. You can view and manage payment details.",
                        type = InfoType.SUCCESS,
                        icon = Icons.Default.CheckCircle,
                        actionText = "View Invoice",
                        onActionClick = onViewInvoice
                    )
                }
                jobCard.status == JobCardStatus.COMPLETED || jobCard.status == JobCardStatus.DELIVERED -> {
                    // Can generate invoice
                    InfoCard(
                        title = "Ready to Invoice",
                        description = "Job card is completed. Generate an invoice to record payment and complete the transaction.",
                        type = InfoType.INFO,
                        icon = Icons.Default.Receipt
                    )

                    GMButton(
                        text = "Generate Invoice",
                        onClick = onGenerateInvoice,
                        type = ButtonType.PRIMARY,
                        size = ButtonSize.LARGE,
                        icon = Icons.Default.Receipt,
                        loading = isGeneratingInvoice,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                else -> {
                    // Cannot generate invoice yet
                    InfoCard(
                        title = "Complete Job First",
                        description = "Invoice can only be generated after the job card is marked as completed.",
                        type = InfoType.WARNING,
                        icon = Icons.Default.Info
                    )
                }
            }
        }
    }
}

// ===========================================================================
// Cost Summary Card
// ===========================================================================
@Composable
private fun CostSummaryCard(jobCard: JobCard) {
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
            CostRow(label = "Labor Cost", amount = jobCard.laborCost)
            CostRow(label = "Parts Cost", amount = jobCard.partsCost)
            Divider()
            CostRow(
                label = "Total Cost",
                amount = jobCard.totalCost,
                isTotal = true
            )
            if (jobCard.discount > 0) {
                CostRow(
                    label = "Discount",
                    amount = -jobCard.discount,
                    color = MaterialTheme.colorScheme.error
                )
                Divider()
                CostRow(
                    label = "Final Amount",
                    amount = jobCard.finalAmount,
                    isTotal = true
                )
            }
        }
    }
}

@Composable
private fun CostRow(
    label: String,
    amount: Double,
    isTotal: Boolean = false,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = if (isTotal) MaterialTheme.typography.titleMedium
            else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isTotal) FontWeight.Bold
            else FontWeight.Normal,
            color = if (amount < 0) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "₹${String.format("%.2f", kotlin.math.abs(amount))}",
            style = if (isTotal) MaterialTheme.typography.titleMedium
            else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isTotal) androidx.compose.ui.text.font.FontWeight.Bold
            else androidx.compose.ui.text.font.FontWeight.Normal,
            color = color
        )
    }
}

// ===========================================================================
// Generate Invoice Dialog
// ===========================================================================
@Composable
private fun GenerateInvoiceDialog(
    jobCard: JobCard,
    discountPercentage: String,
    notes: String,
    onDiscountChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Receipt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = { Text("Generate Invoice") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.medium)
            ) {
                // Amount Summary
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.medium)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Total Amount:",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "₹${String.format("%.2f", jobCard.finalAmount)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Discount
                GMTextField(
                    value = discountPercentage,
                    onValueChange = onDiscountChange,
                    label = "Discount (%)",
                    placeholder = "0",
                    leadingIcon = Icons.Default.Percent,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                // Notes
                GMTextField(
                    value = notes,
                    onValueChange = onNotesChange,
                    label = "Notes (Optional)",
                    placeholder = "Add any notes for the invoice",
                    leadingIcon = Icons.Default.Notes,
                    singleLine = false,
                    maxLines = 3
                )

                // Info
                Text(
                    text = "Tax will be calculated based on settings.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Generate")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

//@Composable
//private fun JobCardHeaderCard(jobCard: JobCard) {
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        colors = CardDefaults.cardColors(
//            containerColor = MaterialTheme.colorScheme.primaryContainer
//        )
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(Spacing.medium)
//        ) {
//            Text(
//                text = jobCard.jobCardNumber,
//                style = MaterialTheme.typography.headlineSmall,
//                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
//            )
//            Text(
//                text = "Status: ${jobCard.status.name}",
//                style = MaterialTheme.typography.bodyLarge
//            )
//        }
//    }
//}

@Composable
private fun VehicleInfoCard(jobCard: JobCard) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium)
        ) {
            Text(
                text = jobCard.vehicle.registrationNumber,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            Text(
                text = "${jobCard.vehicle.make} ${jobCard.vehicle.model}",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

// ===========================================================================
// Services & Parts Section
// ===========================================================================
@Composable
private fun ServicesAndPartsSection(
    services: List<com.autogarage.domain.model.JobCardService>,
    parts: List<com.autogarage.domain.model.JobCardPart>,
    onAddService: () -> Unit,
    onAddPart: () -> Unit,
    onRemoveService: (com.autogarage.domain.model.JobCardService) -> Unit,
    onRemovePart: (com.autogarage.domain.model.JobCardPart) -> Unit
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
            verticalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            // Services Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    "Services",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                )
                TextButton(onClick = onAddService) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = androidx.compose.ui.Modifier.width(4.dp))
                    Text("Add")
                }
            }

            if (services.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        "No services added yet. Click 'Add' to add services.",
                        modifier = androidx.compose.ui.Modifier.padding(Spacing.medium),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                services.forEach { service ->
                    ServiceItem(
                        service = service,
                        onRemove = { onRemoveService(service) }
                    )
                }
            }

            Divider()

            // Parts Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    "Parts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                )
                TextButton(onClick = onAddPart) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = androidx.compose.ui.Modifier.width(4.dp))
                    Text("Add")
                }
            }

            if (parts.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        "No parts added yet. Click 'Add' to add parts.",
                        modifier = androidx.compose.ui.Modifier.padding(Spacing.medium),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                parts.forEach { part ->
                    PartItem(
                        part = part,
                        onRemove = { onRemovePart(part) }
                    )
                }
            }
        }
    }
}

// ===========================================================================
// Service Item Card
// ===========================================================================
@Composable
private fun ServiceItem(
    service: JobCardService,
    onRemove: () -> Unit
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
                .padding(Spacing.small),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Build,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = androidx.compose.ui.Modifier.size(20.dp)
                )
                Column {
                    Text(
                        service.serviceName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                    )
                    Text(
                        "₹${String.format("%.2f", service.laborCost)} × ${service.quantity}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    "₹${String.format("%.2f", service.totalCost)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(
                    onClick = onRemove,
                    modifier = androidx.compose.ui.Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = androidx.compose.ui.Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// ===========================================================================
// Part Item Card
// ===========================================================================
@Composable
fun PartItem(
    part: JobCardPart,
    onRemove: () -> Unit
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
                .padding(Spacing.small),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Inventory,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = androidx.compose.ui.Modifier.size(20.dp)
                )
                Column {
                    // Try to access partName, fallback to generic label
                    Text(
                        text = try {
                            // If your model has partName field
                            part.partName
                        } catch (e: Exception) {
                            // Fallback if partName doesn't exist
                            "Part Item"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                    )

                    // Build description based on available fields
                    val description = buildString {
                        // Try to add part number
                        try {
                            append(part.partNumber)
                            append(" • ")
                        } catch (e: Exception) {
                            // partNumber doesn't exist, skip it
                        }
                        append("₹${String.format("%.2f", part.unitPrice)}")
                        append(" × ${part.quantity}")
                    }

                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                // Use totalCost or totalPrice depending on your model
                val total = try {
                    part.totalCost
                } catch (e: Exception) {
                    try {
                        part.totalCost
                    } catch (e2: Exception) {
                        part.unitPrice * part.quantity
                    }
                }

                Text(
                    "₹${String.format("%.2f", total)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(
                    onClick = onRemove,
                    modifier = androidx.compose.ui.Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = androidx.compose.ui.Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun VehicleTab(jobCard: JobCard) {
    LazyColumn(
        contentPadding = PaddingValues(Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.medium)
                ) {
                    Text(
                        text = "${jobCard.vehicle.make} ${jobCard.vehicle.model}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = jobCard.vehicle.registrationNumber,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        item { DetailRow("Year", jobCard.vehicle.year.toString(), Icons.Default.CalendarToday) }//CalendarToday
        item { DetailRow("Current KM", "${jobCard.vehicle.currentKilometers} km", Icons.Default.Speed) }//Speed

        if (jobCard.vehicle.color != null) {
            item { DetailRow("Color", jobCard.vehicle.color, Icons.Default.Palette) }//Palette
        }

        if (jobCard.vehicle.fuelType != null) {
            item { DetailRow("Fuel Type", jobCard.vehicle.fuelType.name, Icons.Default.LocalGasStation) }//LocalGasStation
        }

        if (jobCard.vehicle.transmission != null) {
            item { DetailRow("Transmission", jobCard.vehicle.transmission.name, Icons.Default.Settings) }
        }

        if (jobCard.vehicle.engineNumber != null) {
            item { DetailRow("Engine Number", jobCard.vehicle.engineNumber, Icons.Default.Engineering) }//Engineering
        }

        if (jobCard.vehicle.chassisNumber != null) {
            item { DetailRow("Chassis Number", jobCard.vehicle.chassisNumber, Icons.Default.Code) }//Code
        }
    }
}

@Composable
private fun TimelineTab(jobCard: JobCard) {
    LazyColumn(
        contentPadding = PaddingValues(Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        item {
            TimelineItem(
                title = "Job Card Created",
                date = formatDateTime(jobCard.createdAt),
                icon = Icons.Default.Add,
                isCompleted = true
            )
        }

        if (jobCard.status == JobCardStatus.IN_PROGRESS ||
            jobCard.status == JobCardStatus.COMPLETED ||
            jobCard.status == JobCardStatus.DELIVERED) {
            item {
                TimelineItem(
                    title = "Work Started",
                    date = "In Progress",
                    icon = Icons.Default.Build,
                    isCompleted = true
                )
            }
        }

        if (jobCard.actualCompletionDate != null) {
            item {
                TimelineItem(
                    title = "Work Completed",
                    date = jobCard.actualCompletionDate,
                    icon = Icons.Default.CheckCircle,
                    isCompleted = true
                )
            }
        }

        if (jobCard.deliveryDate != null) {
            item {
                TimelineItem(
                    title = "Vehicle Delivered",
                    date = jobCard.deliveryDate,
                    icon = Icons.Default.LocalShipping,
                    isCompleted = true
                )
            }
        }

        if (jobCard.status == JobCardStatus.CANCELLED) {
            item {
                TimelineItem(
                    title = "Job Cancelled",
                    date = "Cancelled",
                    icon = Icons.Default.Cancel,
                    isCompleted = true,
                    isError = true
                )
            }
        }
    }
}

@Composable
private fun DetailRow(
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
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun PriceRow(
    label: String,
    amount: Double,
    isTotal: Boolean = false,
    isFinal: Boolean = false,
    isDiscount: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = if (isFinal) MaterialTheme.typography.titleMedium
            else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isTotal || isFinal) FontWeight.Bold else FontWeight.Normal,
            color = if (isDiscount) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "₹${String.format("%.0f", amount)}",
            style = if (isFinal) MaterialTheme.typography.titleMedium
            else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isTotal || isFinal) FontWeight.Bold else FontWeight.Normal,
            color = if (isDiscount) MaterialTheme.colorScheme.error
            else if (isFinal) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun TimelineItem(
    title: String,
    date: String,
    icon: ImageVector,
    isCompleted: Boolean,
    isError: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        Surface(
            color = if (isError) MaterialTheme.colorScheme.errorContainer
            else if (isCompleted) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isError) MaterialTheme.colorScheme.error
                    else if (isCompleted) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = date,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


private fun formatDateTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
