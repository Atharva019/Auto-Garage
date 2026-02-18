package com.autogarage.presentation.ui.jobcard

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.autogarage.domain.model.JobCard
import com.autogarage.domain.model.JobCardStatus
import com.autogarage.presentation.ui.components.*
import com.autogarage.presentation.ui.theme.Spacing
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*
import com.autogarage.viewmodel.JobCardUiEvent
import com.autogarage.viewmodel.JobCardsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobCardsScreen(
    viewModel: JobCardsViewModel = hiltViewModel(),
    onJobCardClick: (Long) -> Unit,
    onCreateJobCard: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedStatus by viewModel.selectedStatus.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is JobCardUiEvent.NavigateToJobCardDetail -> onJobCardClick(event.jobCardId)
                is JobCardUiEvent.NavigateToCreateJobCard -> onCreateJobCard()
                is JobCardUiEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Job Cards") },
                actions = {
                    IconButton(onClick = { viewModel.onRefresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onCreateJobCardClick() },
                containerColor = MaterialTheme.colorScheme.secondary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Job Card")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Status Filter Tabs
            ScrollableTabRow(
                selectedTabIndex = if (selectedStatus == null) 0 else JobCardStatus.values().indexOf(selectedStatus) + 1,
                modifier = Modifier.fillMaxWidth(),
                edgePadding = Spacing.medium
            ) {
                Tab(
                    selected = selectedStatus == null,
                    onClick = { viewModel.onStatusFilterChange(null) },
                    text = { Text("All") }
                )
                JobCardStatus.values().forEach { status ->
                    Tab(
                        selected = selectedStatus == status,
                        onClick = { viewModel.onStatusFilterChange(status) },
                        text = { Text(status.name.replace("_", " ")) }
                    )
                }
            }

            when {
                uiState.isLoading -> {
                    LoadingIndicator(message = "Loading job cards...")
                }
                uiState.error != null -> {
                    EmptyState(
                        icon = Icons.Default.Error,  // ✅ Use Material Icon
                        title = "Error Loading Job Cards",
                        description = uiState.error ?: "Unknown error",
                        actionText = "Retry",
                        onActionClick = { viewModel.onRefresh() }
                    )
                }
                uiState.jobCards.isEmpty() -> {
                    EmptyState(
                        icon = Icons.Default.AssignmentLate,  // ✅ Use Material Icon
                        title = "No Job Cards Yet",
                        description = "Create your first job card to get started",
                        actionText = "Create Job Card",
                        onActionClick = { viewModel.onCreateJobCardClick() }
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(Spacing.medium),
                        verticalArrangement = Arrangement.spacedBy(Spacing.small)
                    ) {
                        items(
                            items = uiState.jobCards,
                            key = { it.id }
                        ) { jobCard ->
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
}

@Composable
private fun JobCardItem(
    jobCard: JobCard,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Spacing.cardCornerRadiusSmall),
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
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = jobCard.vehicle.registrationNumber,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${jobCard.vehicle.make} ${jobCard.vehicle.model}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                StatusBadge(status = jobCard.status)
            }

            Spacer(modifier = Modifier.height(Spacing.small))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Created:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatDate(jobCard.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (jobCard.assignedTechnician != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Technician:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = jobCard.assignedTechnician.name,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            if (jobCard.finalAmount > 0) {
                Spacer(modifier = Modifier.height(Spacing.small))
                Text(
                    text = "Amount: ₹${String.format("%.2f", jobCard.finalAmount)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(status: JobCardStatus) {
    val (backgroundColor, textColor, statusText) = when (status) {
        JobCardStatus.PENDING -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            "Pending"
        )
        JobCardStatus.IN_PROGRESS -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
            "In Progress"
        )
        JobCardStatus.COMPLETED -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            "Completed"
        )
        JobCardStatus.CANCELLED -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            "Cancelled"
        )
        JobCardStatus.DELIVERED -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            "Delivered"
        )
    }

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = backgroundColor
    ) {
        Text(
            text = statusText,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

private fun formatDate(timestamp: Long): String {
    val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return format.format(Date(timestamp))
}

//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Job Cards") },
//                actions = {
//                    IconButton(onClick = { viewModel.onRefresh() }) {
//                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
//                    }
//                }
//            )
//        },
//        floatingActionButton = {
//            FloatingActionButton(
//                onClick = { viewModel.onCreateJobCardClick() },
//                containerColor = MaterialTheme.colorScheme.secondary
//            ) {
//                Icon(Icons.Default.Add, contentDescription = "Create Job Card")
//            }
//        },
//        snackbarHost = { SnackbarHost(snackbarHostState) }
//    ) { padding ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(padding)
//        ) {
//            // Search Bar
//            GMSearchBar(
//                query = searchQuery,
//                onQueryChange = viewModel::onSearchQueryChange,
//                placeholder = "Search job cards, vehicles...",
//                modifier = Modifier.padding(Spacing.medium)
//            )
//
//            // Filter Chips
//            LazyRow(
//                contentPadding = PaddingValues(horizontal = Spacing.medium),
//                horizontalArrangement = Arrangement.spacedBy(Spacing.small)
//            ) {
//                items(JobCardFilter.values()) { filter ->
//                    FilterChip(
//                        selected = selectedFilter == filter,
//                        onClick = { viewModel.onFilterSelected(filter) },
//                        label = { Text(filter.displayName) },
//                        leadingIcon = if (selectedFilter == filter) {
//                            {
//                                Icon(
//                                    Icons.Default.Check,
//                                    contentDescription = null,
//                                    modifier = Modifier.size(18.dp)
//                                )
//                            }
//                        } else null
//                    )
//                }
//            }
//
//            Spacer(modifier = Modifier.height(Spacing.small))
//
//            // Job Cards List
//            when {
//                uiState.isLoading -> {
//                    LoadingIndicator(message = "Loading job cards...")
//                }
//                uiState.error != null -> {
//                    EmptyState(
//                        icon = Icons.Default.Error,
//                        title = "Error Loading Job Cards",
//                        description = uiState.error ?: "Unknown error",
//                        actionText = "Retry",
//                        onActionClick = { viewModel.onRefresh() }
//                    )
//                }
//                uiState.jobCards.isEmpty() -> {
//                    EmptyState(
//                        icon = Icons.Default.Assignment,
//                        title = "No Job Cards",
//                        description = "Create your first job card to get started",
//                        actionText = "Create Job Card",
//                        onActionClick = { viewModel.onCreateJobCardClick() }
//                    )
//                }
//                else -> {
//                    LazyColumn(
//                        contentPadding = PaddingValues(Spacing.medium),
//                        verticalArrangement = Arrangement.spacedBy(Spacing.small)
//                    ) {
//                        items(uiState.jobCards) { jobCard ->
//                            JobCardItem(
//                                jobCard = jobCard,
//                                onClick = { viewModel.onJobCardClick(jobCard.id) }
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    }
//}

//@Composable
//private fun JobCardItem(
//    jobCard: JobCard,
//    onClick: () -> Unit,
//    modifier: Modifier = Modifier
//) {
//    Card(
//        onClick = onClick,
//        modifier = modifier.fillMaxWidth(),
//        colors = CardDefaults.cardColors(
//            containerColor = MaterialTheme.colorScheme.surface
//        ),
//        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(Spacing.medium)
//        ) {
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.Top
//            ) {
//                Column(modifier = Modifier.weight(1f)) {
//                    Text(
//                        text = jobCard.jobCardNumber,
//                        style = MaterialTheme.typography.titleMedium,
//                        fontWeight = FontWeight.Bold
//                    )
//                    Spacer(modifier = Modifier.height(4.dp))
//
//                    Row(
//                        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.DirectionsCar,
//                            contentDescription = null,
//                            modifier = Modifier.size(16.dp),
//                            tint = MaterialTheme.colorScheme.onSurfaceVariant
//                        )
//                        Text(
//                            text = "${jobCard.vehicle.make} ${jobCard.vehicle.model}",
//                            style = MaterialTheme.typography.bodyMedium,
//                            color = MaterialTheme.colorScheme.onSurfaceVariant
//                        )
//                    }
//
//                    Text(
//                        text = jobCard.vehicle.registrationNumber,
//                        style = MaterialTheme.typography.bodyMedium,
//                        fontWeight = FontWeight.Medium,
//                        color = MaterialTheme.colorScheme.primary
//                    )
//
//                    Spacer(modifier = Modifier.height(4.dp))
//
//                    Row(
//                        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.CalendarToday,
//                            contentDescription = null,
//                            modifier = Modifier.size(14.dp),
//                            tint = MaterialTheme.colorScheme.onSurfaceVariant
//                        )
//                        Text(
//                            text = formatDate(jobCard.createdAt),
//                            style = MaterialTheme.typography.bodySmall,
//                            color = MaterialTheme.colorScheme.onSurfaceVariant
//                        )
//                    }
//                }
//
//                Column(
//                    horizontalAlignment = Alignment.End,
//                    verticalArrangement = Arrangement.spacedBy(Spacing.small)
//                ) {
//                    StatusBadge(
//                        status = when (jobCard.status) {
//                            com.autogarage.domain.model.JobCardStatus.PENDING ->
//                                JobStatus.PENDING
//                            com.autogarage.domain.model.JobCardStatus.IN_PROGRESS ->
//                                JobStatus.IN_PROGRESS
//                            com.autogarage.domain.model.JobCardStatus.COMPLETED ->
//                                JobStatus.COMPLETED
//                            com.autogarage.domain.model.JobCardStatus.CANCELLED ->
//                                JobStatus.CANCELLED
//                            com.autogarage.domain.model.JobCardStatus.DELIVERED ->
//                                JobStatus.DELIVERED
//                        }
//                    )
//
//                    if (jobCard.priority == com.autogarage.domain.model.Priority.HIGH ||
//                        jobCard.priority == com.autogarage.domain.model.Priority.URGENT) {
//                        Surface(
//                            color = MaterialTheme.colorScheme.errorContainer,
//                            shape = MaterialTheme.shapes.small
//                        ) {
//                            Row(
//                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
//                                horizontalArrangement = Arrangement.spacedBy(2.dp),
//                                verticalAlignment = Alignment.CenterVertically
//                            ) {
//                                Icon(
//                                    imageVector = Icons.Default.PriorityHigh,
//                                    contentDescription = "High Priority",
//                                    tint = MaterialTheme.colorScheme.error,
//                                    modifier = Modifier.size(14.dp)
//                                )
//                                Text(
//                                    text = jobCard.priority.name,
//                                    style = MaterialTheme.typography.labelSmall,
//                                    color = MaterialTheme.colorScheme.error,
//                                    fontWeight = FontWeight.Bold
//                                )
//                            }
//                        }
//                    }
//
//                    if (jobCard.finalAmount > 0) {
//                        Text(
//                            text = "₹${String.format("%.0f", jobCard.finalAmount)}",
//                            style = MaterialTheme.typography.titleSmall,
//                            fontWeight = FontWeight.Bold,
//                            color = MaterialTheme.colorScheme.primary
//                        )
//                    }
//                }
//            }
//
//            if (jobCard.customerComplaints != null) {
//                Spacer(modifier = Modifier.height(Spacing.small))
//                Text(
//                    text = jobCard.customerComplaints.take(80) +
//                            if (jobCard.customerComplaints.length > 80) "..." else "",
//                    style = MaterialTheme.typography.bodySmall,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                )
//            }
//
//            if (jobCard.assignedTechnician != null) {
//                Spacer(modifier = Modifier.height(Spacing.small))
//                Row(
//                    horizontalArrangement = Arrangement.spacedBy(Spacing.small),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.Person,
//                        contentDescription = null,
//                        modifier = Modifier.size(14.dp),
//                        tint = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                    Text(
//                        text = "Assigned to: ${jobCard.assignedTechnician.name}",
//                        style = MaterialTheme.typography.bodySmall,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                }
//            }
//        }
//    }
//}

