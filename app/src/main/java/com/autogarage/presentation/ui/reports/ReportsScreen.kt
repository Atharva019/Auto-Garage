package com.autogarage.presentation.ui.reports

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.autogarage.domain.model.PaymentMode
import com.autogarage.domain.usecase.reports.StockAlert
import com.autogarage.domain.usecase.reports.TopCustomer
import com.autogarage.domain.usecase.reports.WorkerStats
import com.autogarage.presentation.ui.components.*
import com.autogarage.presentation.ui.theme.Spacing
import com.autogarage.viewmodel.DateRange
import com.autogarage.viewmodel.ReportType
import com.autogarage.viewmodel.ReportsUiEvent
import com.autogarage.viewmodel.ReportsUiState
import com.autogarage.viewmodel.ReportsViewModel
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import kotlin.text.format
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedDateRange by viewModel.selectedDateRange.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTab by remember { mutableStateOf(0) }
    var showDateRangePicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is ReportsUiEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is ReportsUiEvent.ShowError -> {
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
                title = { Text("Reports & Analytics") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Date Range Selector
                    IconButton(onClick = { showDateRangePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Select Date Range")
                    }

                    // Export Button
                    IconButton(onClick = { viewModel.onExportReport(ReportType.ALL) }) {
                        Icon(Icons.Default.Download, contentDescription = "Export")
                    }

                    // Refresh
                    IconButton(onClick = viewModel::onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
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
        ) {
            // Date Range Chips
            DateRangeChips(
                selectedDateRange = selectedDateRange,
                onDateRangeChange = viewModel::onDateRangeChange,
                modifier = Modifier.padding(horizontal = Spacing.medium, vertical = Spacing.small)
            )

            // Tab Layout
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Overview") },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Revenue") },
                    icon = { Icon(Icons.Default.AttachMoney, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Operations") },
                    icon = { Icon(Icons.Default.Build, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Team") },
                    icon = { Icon(Icons.Default.People, contentDescription = null) }
                )
            }

            // Content
            when {
                uiState.isLoading -> {
                    LoadingIndicator(message = "Loading reports...")
                }
                uiState.error != null -> {
                    EmptyState(
                        icon = Icons.Default.Error,
                        title = "Error Loading Reports",
                        description = uiState.error ?: "Unknown error",
                        actionText = "Retry",
                        onActionClick = viewModel::onRefresh
                    )
                }
                else -> {
                    when (selectedTab) {
                        0 -> OverviewTab(uiState)
                        1 -> RevenueTab(uiState)
                        2 -> OperationsTab(uiState)
                        3 -> TeamTab(uiState)
                    }
                }
            }
        }
    }
}

// ===========================================================================
// Date Range Chips
// ===========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangeChips(
    selectedDateRange: DateRange,
    onDateRangeChange: (DateRange) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(Spacing.small)
    ) {
        DateRange.values().filter { it != DateRange.CUSTOM }.forEach { range ->
            FilterChip(
                selected = selectedDateRange == range,
                onClick = { onDateRangeChange(range) },
                label = {
                    Text(
                        when (range) {
                            DateRange.TODAY -> "Today"
                            DateRange.THIS_WEEK -> "This Week"
                            DateRange.THIS_MONTH -> "This Month"
                            DateRange.THIS_QUARTER -> "This Quarter"
                            DateRange.THIS_YEAR -> "This Year"
                            DateRange.CUSTOM -> "Custom"
                        }
                    )
                }
            )
        }
    }
}

// ===========================================================================
// Overview Tab
// ===========================================================================
@Composable
private fun OverviewTab(uiState: ReportsUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        // Revenue Summary
        uiState.revenueReport?.let { revenue ->
            SectionHeader(title = "Revenue Summary")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                MetricCard(
                    title = "Total Revenue",
                    value = "₹${String.format("%.2f", revenue.totalRevenue)}",
                    icon = Icons.Default.AttachMoney,
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                    iconTint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "Paid",
                    value = "₹${String.format("%.2f", revenue.paidAmount)}",
                    icon = Icons.Default.CheckCircle,
                    backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                    iconTint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                MetricCard(
                    title = "Pending",
                    value = "₹${String.format("%.2f", revenue.pendingAmount)}",
                    icon = Icons.Default.PendingActions,
                    backgroundColor = MaterialTheme.colorScheme.errorContainer,
                    iconTint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "Avg Invoice",
                    value = "₹${String.format("%.0f", revenue.averageInvoiceValue)}",
                    icon = Icons.Default.Receipt,
                    backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Job Card Summary
        uiState.jobCardStats?.let { stats ->
            SectionHeader(title = "Job Card Statistics")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                MetricCard(
                    title = "Total Jobs",
                    value = "${stats.totalJobCards}",
                    icon = Icons.Default.Assignment,
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                    iconTint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "Completed",
                    value = "${stats.completedJobCards}",
                    icon = Icons.Default.CheckCircle,
                    backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                    iconTint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
            }

            MetricCard(
                title = "Avg Completion Time",
                value = "${String.format("%.1f", stats.averageCompletionTime)} hours",
                icon = Icons.Default.Timer,
                backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                iconTint = MaterialTheme.colorScheme.secondary
            )
        }

        // Customer & Inventory Summary
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            uiState.customerStats?.let { stats ->
                MetricCard(
                    title = "Total Customers",
                    value = "${stats.totalCustomers}",
                    subtitle = "${stats.newCustomers} new",
                    icon = Icons.Default.People,
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                    iconTint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }

            uiState.inventoryStats?.let { stats ->
                MetricCard(
                    title = "Stock Alerts",
                    value = "${stats.lowStockItems + stats.outOfStockItems}",
                    subtitle = "${stats.outOfStockItems} out of stock",
                    icon = Icons.Default.Warning,
                    backgroundColor = MaterialTheme.colorScheme.errorContainer,
                    iconTint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// ===========================================================================
// Revenue Tab
// ===========================================================================
@Composable
private fun RevenueTab(uiState: ReportsUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        uiState.revenueReport?.let { revenue ->
            // Revenue Overview
            SectionHeader(title = "Revenue Overview")

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
                    Text(
                        text = "Total Revenue",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "₹${String.format("%.2f", revenue.totalRevenue)}",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Divider(modifier = Modifier.padding(vertical = Spacing.small))

                    RevenueRow("Paid Amount", revenue.paidAmount)
                    RevenueRow("Pending Amount", revenue.pendingAmount)
                    RevenueRow("Total Invoices", revenue.totalInvoices.toDouble(), isCount = true)
                    RevenueRow("Average Invoice", revenue.averageInvoiceValue)
                }
            }

            // Payment Mode Breakdown
            if (revenue.paymentModeBreakdown.isNotEmpty()) {
                SectionHeader(title = "Payment Methods")

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
                        revenue.paymentModeBreakdown.forEach { (mode, amount) ->
                            PaymentModeRow(
                                mode = mode,
                                amount = amount,
                                percentage = (amount / revenue.paidAmount) * 100
                            )
                        }
                    }
                }
            }

            // Daily Revenue Trend (simplified - show last 7 days)
            if (revenue.dailyRevenue.isNotEmpty()) {
                SectionHeader(title = "Revenue Trend")

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
                        revenue.dailyRevenue.takeLast(7).forEach { daily ->
                            DailyRevenueRow(
                                date = daily.date,
                                revenue = daily.revenue,
                                invoiceCount = daily.invoiceCount
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RevenueRow(label: String, value: Double, isCount: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = if (isCount) "${value.toInt()}" else "₹${String.format("%.2f", value)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
        )
    }
}

@Composable
private fun PaymentModeRow(mode: PaymentMode, amount: Double, percentage: Double) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = mode.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
            )
            Text(
                text = "₹${String.format("%.2f", amount)} (${String.format("%.1f", percentage)}%)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        LinearProgressIndicator(
            progress = (percentage / 100).toFloat(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun formatDateShort(dateString: String): String {
    return try {
        // Try parsing as Long timestamp first (common in Room)
        val timestamp = dateString.toLongOrNull()
        if (timestamp != null) {
            val sdf = SimpleDateFormat("dd MMM", java.util.Locale.getDefault())
            // FIX: Use java.util.Date instead of com.google.type.Date
            return sdf.format(java.util.Date(timestamp))
        }

        // If it's an ISO string (e.g. "2023-10-05"), try parsing that
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM", java.util.Locale.getDefault())
        val date = inputFormat.parse(dateString)
        return date?.let { outputFormat.format(it) } ?: dateString

    } catch (e: Exception) {
        // Fallback if parsing fails
        dateString
    }
}

@Composable
private fun DailyRevenueRow(date: String, revenue: Double, invoiceCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = formatDateShort(date),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
            )
            Text(
                text = "$invoiceCount invoices",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = "₹${String.format("%.2f", revenue)}",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

// ===========================================================================
// Operations Tab
// ===========================================================================
@Composable
private fun OperationsTab(uiState: ReportsUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        // Job Card Status Distribution
        uiState.jobCardStats?.let { stats ->
            SectionHeader(title = "Job Card Status")

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
                    StatusRow("Pending", stats.pendingJobCards, MaterialTheme.colorScheme.secondary)
                    StatusRow("In Progress", stats.inProgressJobCards, MaterialTheme.colorScheme.tertiary)
                    StatusRow("Completed", stats.completedJobCards, MaterialTheme.colorScheme.primary)
                    StatusRow("Delivered", stats.deliveredJobCards, MaterialTheme.colorScheme.primary)
                    StatusRow("Cancelled", stats.cancelledJobCards, MaterialTheme.colorScheme.error)
                }
            }

            // Priority Distribution
            if (stats.priorityBreakdown.isNotEmpty()) {
                SectionHeader(title = "Priority Distribution")

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
                        stats.priorityBreakdown.forEach { (priority, count) ->
                            PriorityRow(priority, count)
                        }
                    }
                }
            }
        }

        // Inventory Status
        uiState.inventoryStats?.let { stats ->
            SectionHeader(title = "Inventory Status")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                MetricCard(
                    title = "In Stock",
                    value = "${stats.inStockItems}",
                    icon = Icons.Default.CheckCircle,
                    backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                    iconTint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "Low Stock",
                    value = "${stats.lowStockItems}",
                    icon = Icons.Default.Warning,
                    backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }

            // Stock Alerts
            if (stats.stockAlerts.isNotEmpty()) {
                SectionHeader(title = "Stock Alerts")

                stats.stockAlerts.take(5).forEach { alert ->
                    StockAlertCard(alert)
                }
            }
        }

        // Customer Stats
        uiState.customerStats?.let { stats ->
            SectionHeader(title = "Customer Analytics")

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
                    CustomerStatRow("Total Customers", stats.totalCustomers)
                    CustomerStatRow("New Customers", stats.newCustomers)
                    CustomerStatRow("Active Customers", stats.activeCustomers)
                    CustomerStatRow(
                        "Retention Rate",
                        "${String.format("%.1f", stats.customerRetentionRate)}%"
                    )
                    CustomerStatRow(
                        "Avg Customer Value",
                        "₹${String.format("%.2f", stats.averageCustomerValue)}"
                    )
                }
            }

            // Top Customers
            if (stats.topCustomers.isNotEmpty()) {
                SectionHeader(title = "Top Customers")

                stats.topCustomers.take(5).forEach { customer ->
                    TopCustomerCard(customer)
                }
            }
        }
    }
}

@Composable
private fun StatusRow(label: String, count: Int, color: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.small),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.size(12.dp)) {
                drawCircle(color = color)
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Text(
            text = "$count",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun PriorityRow(priority: String, count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = priority,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "$count",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
    }
}

@Composable
private fun StockAlertCard(alert: StockAlert) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (alert.alertLevel == "OUT")
                MaterialTheme.colorScheme.errorContainer
            else
                MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alert.itemName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                )
                Text(
                    text = "Current: ${alert.currentStock} | Min: ${alert.minimumStock}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Surface(
                shape = MaterialTheme.shapes.small,
                color = if (alert.alertLevel == "OUT")
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.secondary
            ) {
                Text(
                    text = if (alert.alertLevel == "OUT") "OUT" else "LOW",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun CustomerStatRow(label: String, value: Any) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = "$value",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun TopCustomerCard(customer: TopCustomer) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = customer.customerName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                )
                Text(
                    text = "${customer.jobCardCount} job cards",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "₹${String.format("%.2f", customer.totalSpent)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// ===========================================================================
// Team Tab
// ===========================================================================
@Composable
private fun TeamTab(uiState: ReportsUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        uiState.workerPerformance?.let { performance ->
            SectionHeader(title = "Worker Performance")

            if (performance.workers.isEmpty()) {
                InfoCard(
                    title = "No Data",
                    description = "No worker performance data available for the selected period",
                    type = InfoType.INFO,
                    icon = Icons.Default.Info
                )
            } else {
                performance.workers.forEach { worker ->
                    WorkerPerformanceCard(worker)
                }
            }
        }
    }
}

@Composable
private fun WorkerPerformanceCard(
    worker: WorkerStats
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
            // Worker Name and Revenue
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    text = worker.workerName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                Text(
                    text = "₹${String.format("%.2f", worker.revenueGenerated)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Divider()

            // Stats
            WorkerStatRow("Total Jobs", "${worker.totalJobsAssigned}")
            WorkerStatRow("Completed", "${worker.completedJobs}")
            WorkerStatRow("Pending", "${worker.pendingJobs}")
            WorkerStatRow(
                "Completion Rate",
                "${String.format("%.1f", worker.completionRate)}%"
            )
            WorkerStatRow(
                "Avg Completion Time",
                "${String.format("%.1f", worker.averageCompletionTime)} hrs"
            )

            // Completion Rate Progress
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Performance",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LinearProgressIndicator(
                    progress = (worker.completionRate / 100).toFloat(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun WorkerStatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
        )
    }
}
