package com.autogarage.presentation.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.autogarage.domain.usecase.reports.DashboardSummary
import com.autogarage.presentation.ui.components.*
import com.autogarage.presentation.ui.icons.GarageMasterIcons
import com.autogarage.presentation.ui.theme.*
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToJobCards: () -> Unit = {},
    onNavigateToCustomers: () -> Unit = {},
    onNavigateToInventory: () -> Unit = {},
    onNavigateToCreateJobCard: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    onJobCardClick: (Long) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle UI events
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is DashboardUiEvent.NavigateToMetric -> {
                    when (event.metric) {
                        DashboardMetric.PENDING_JOBS,
                        DashboardMetric.IN_PROGRESS_JOBS,
                        DashboardMetric.COMPLETED_JOBS -> onNavigateToJobCards()
                        DashboardMetric.TOTAL_CUSTOMERS -> onNavigateToCustomers()
                        DashboardMetric.LOW_STOCK_ITEMS -> onNavigateToInventory()
                    }
                }
                is DashboardUiEvent.NavigateToCreateJobCard -> onNavigateToCreateJobCard()
                is DashboardUiEvent.NavigateToReports -> onNavigateToReports()
                is DashboardUiEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "AutoNexa",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Dashboard",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onNavigateToReports() }) {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = "Reports"
                        )
                    }
//                    IconButton(onClick = { /* Navigate to settings */ }) {
//                        Icon(
//                            imageVector = Icons.Default.Settings,
//                            contentDescription = "Settings"
//                        )
//                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToCreateJobCard,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                },
                text = { Text("New Job Card") },
                containerColor = MaterialTheme.colorScheme.secondary
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when {
            uiState.isLoading && uiState.dashboardData == null -> {
                LoadingIndicator(
                    message = "Loading dashboard...",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
            uiState.error != null && uiState.dashboardData == null -> {
                EmptyState(
                    icon = Icons.Default.Warning,
                    title = "Error Loading Dashboard",
                    description = uiState.error ?: "Unknown error",
                    actionText = "Retry",
                    onActionClick = { viewModel.onRefresh() },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
            else -> {
                DashboardContent(
                    data = uiState.dashboardData ?: DashboardData(),
                    reportsSummary = uiState.reportsSummary,
                    onMetricClick = viewModel::onMetricClick,
                    onJobCardClick = onJobCardClick,
                    onNavigateToReports = viewModel::onNavigateToReports,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun DashboardContent(
    data: DashboardData,
    reportsSummary: DashboardSummary?,
    onMetricClick: (DashboardMetric) -> Unit,
    onJobCardClick: (Long) -> Unit,
    onNavigateToReports: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        // ✅ Revenue Overview Card (if reports available)
        reportsSummary?.let { summary ->
            item {
                RevenueOverviewCard(
                    summary = summary,
                    onViewReports = onNavigateToReports
                )
            }
        }

        // Quick Stats Section
        item {
            Text(
                text = "Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = Spacing.small)
            )
        }

        // ✅ Today's Revenue (if reports available)
        reportsSummary?.let { summary ->
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small)
                ) {
                    MetricCard(
                        title = "Today's Revenue",
                        value = "₹${String.format("%.0f", summary.todayRevenue)}",
                        icon = Icons.Default.TrendingUp,
                        backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                        iconTint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToReports
                    )

                    MetricCard(
                        title = "This Month",
                        value = "₹${String.format("%.0f", summary.monthRevenue)}",
                        icon = Icons.Default.AttachMoney,
                        backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                        iconTint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.weight(1f),
                        subtitle = if (summary.revenueGrowth >= 0)
                            "+${String.format("%.1f", summary.revenueGrowth)}%"
                        else
                            "${String.format("%.1f", summary.revenueGrowth)}%",
                        trend = if (summary.revenueGrowth >= 0) "+" else "-",
                        trendIsPositive = summary.revenueGrowth >= 0,
                        onClick = onNavigateToReports
                    )
                }
            }
        }

        // Metrics Cards Row 1
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                MetricCard(
                    title = "Pending",
                    value = data.pendingJobCards.toString(),
                    icon = GarageMasterIcons.Service.Pending,
                    backgroundColor = MaterialTheme.customColors.warningContainer,
                    iconTint = MaterialTheme.customColors.warning,
                    modifier = Modifier.weight(1f),
                    onClick = { onMetricClick(DashboardMetric.PENDING_JOBS) }
                )
                MetricCard(
                    title = "In Progress",
                    value = data.inProgressJobCards.toString(),
                    icon = Icons.Default.Build,
                    backgroundColor = MaterialTheme.customColors.infoContainer,
                    iconTint = MaterialTheme.customColors.info,
                    modifier = Modifier.weight(1f),
                    onClick = { onMetricClick(DashboardMetric.IN_PROGRESS_JOBS) }
                )
            }
        }

        // Metrics Cards Row 2
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                MetricCard(
                    title = "Finished",
                    value = data.completedJobCards.toString(),
                    icon = Icons.Default.CheckCircle,
                    backgroundColor = MaterialTheme.customColors.successContainer,
                    iconTint = MaterialTheme.customColors.success,
                    modifier = Modifier.weight(1f),
                    subtitle = "Today",
                    onClick = { onMetricClick(DashboardMetric.COMPLETED_JOBS) }
                )
                MetricCard(
                    title = "Customers",
                    value = data.totalCustomers.toString(),
                    icon = Icons.Default.Person,
                    backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                    iconTint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f),
                    onClick = { onMetricClick(DashboardMetric.TOTAL_CUSTOMERS) }
                )
            }
        }
// ✅ Quick Stats from Reports
        reportsSummary?.let { summary ->
            item {
                QuickStatsCard(
                    summary = summary,
                    onViewReports = onNavigateToReports
                )
            }
        }

        // Low Stock Alert
        if (data.lowStockCount > 0) {
            item {
                InfoCard(
                    title = "Low Stock Alert",
                    description = "${data.lowStockCount} items are running low on stock",
                    type = InfoType.WARNING,
                    icon = Icons.Default.Warning,
                    actionText = "View Items",
                    onActionClick = { onMetricClick(DashboardMetric.LOW_STOCK_ITEMS) }
                )
            }
        }
        // ✅ Pending Invoices Alert
        reportsSummary?.let { summary ->
            if (summary.pendingInvoices > 0) {
                item {
                    InfoCard(
                        title = "Pending Invoices",
                        description = "${summary.pendingInvoices} unpaid invoices requiring attention",
                        type = InfoType.ERROR,
                        icon = Icons.Default.Receipt,
                        actionText = "View Reports",
                        onActionClick = onNavigateToReports
                    )
                }
            }
        }

        // Recent Pending Jobs
        item {
            SectionHeader(
                title = "Recent Pending Jobs",
                actionText = if (data.recentPendingJobs.isNotEmpty()) "View All" else null,
                onActionClick = if (data.recentPendingJobs.isNotEmpty()) {
                    { onMetricClick(DashboardMetric.PENDING_JOBS) }
                } else null,
                modifier = Modifier.padding(top = Spacing.small)
            )
        }

        if (data.recentPendingJobs.isEmpty()) {
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
                            .padding(Spacing.large),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.customColors.success
                        )
                        Spacer(modifier = Modifier.height(Spacing.small))
                        Text(
                            text = "No Pending Jobs",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "All caught up!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(data.recentPendingJobs) { jobCard ->
                JobCardItem(
                    jobCard = jobCard,
                    onClick = { onJobCardClick(jobCard.id) }
                )
            }
        }

        // Bottom spacing for FAB
        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

// ✅ Revenue Overview Card
@Composable
private fun RevenueOverviewCard(
    summary: com.autogarage.domain.usecase.reports.DashboardSummary,
    onViewReports: () -> Unit
) {
    Card(
        onClick = onViewReports,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "This Month's Revenue",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "₹${String.format("%.2f", summary.monthRevenue)}",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Growth Indicator
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = if (summary.revenueGrowth >= 0)
                        MaterialTheme.colorScheme.tertiaryContainer
                    else
                        MaterialTheme.colorScheme.errorContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (summary.revenueGrowth >= 0)
                                Icons.Default.TrendingUp
                            else
                                Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = if (summary.revenueGrowth >= 0)
                                MaterialTheme.colorScheme.tertiary
                            else
                                MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "${String.format("%.1f", kotlin.math.abs(summary.revenueGrowth))}%",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (summary.revenueGrowth >= 0)
                                MaterialTheme.colorScheme.onTertiaryContainer
                            else
                                MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.small))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                RevenueStatItem(
                    label = "Today",
                    value = "₹${String.format("%.0f", summary.todayRevenue)}"
                )
                RevenueStatItem(
                    label = "Pending",
                    value = "${summary.pendingInvoices}"
                )
                RevenueStatItem(
                    label = "New Customers",
                    value = "${summary.newCustomersThisMonth}"
                )
            }
        }
    }
}

@Composable
private fun RevenueStatItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
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

// ✅ Quick Stats Card
@Composable
private fun QuickStatsCard(
    summary: com.autogarage.domain.usecase.reports.DashboardSummary,
    onViewReports: () -> Unit
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quick Stats",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = onViewReports) {
                    Text("View Reports")
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            HorizontalDivider()

            QuickStatRow(
                label = "Active Jobs",
                value = "${summary.activeJobCards}",
                icon = Icons.Default.Build
            )
            QuickStatRow(
                label = "Pending Jobs",
                value = "${summary.pendingJobCards}",
                icon = Icons.Default.PendingActions
            )
            QuickStatRow(
                label = "Low Stock Items",
                value = "${summary.lowStockItems}",
                icon = Icons.Default.Warning,
                isAlert = summary.lowStockItems > 0
            )
        }
    }
}

@Composable
private fun QuickStatRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isAlert: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isAlert) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (isAlert) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.primary
        )
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
                    text = jobCard.jobCardNumber,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${jobCard.vehicle.make} ${jobCard.vehicle.model}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = jobCard.vehicle.registrationNumber,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (jobCard.customerComplaints != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = jobCard.customerComplaints.take(50) +
                                if (jobCard.customerComplaints.length > 50) "..." else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                StatusBadge(
                    status = when (jobCard.status) {
                        com.autogarage.domain.model.JobCardStatus.PENDING ->
                            com.autogarage.presentation.ui.components.JobStatus.PENDING
                        com.autogarage.domain.model.JobCardStatus.IN_PROGRESS ->
                            com.autogarage.presentation.ui.components.JobStatus.IN_PROGRESS
                        com.autogarage.domain.model.JobCardStatus.COMPLETED ->
                            com.autogarage.presentation.ui.components.JobStatus.COMPLETED
                        com.autogarage.domain.model.JobCardStatus.CANCELLED ->
                            com.autogarage.presentation.ui.components.JobStatus.CANCELLED
                        com.autogarage.domain.model.JobCardStatus.DELIVERED ->
                            com.autogarage.presentation.ui.components.JobStatus.DELIVERED
                    }
                )
                if (jobCard.priority == com.autogarage.domain.model.Priority.HIGH ||
                    jobCard.priority == com.autogarage.domain.model.Priority.URGENT) {
                    Icon(
                        imageVector = GarageMasterIcons.Service.HighPriority,
                        contentDescription = "High Priority",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
