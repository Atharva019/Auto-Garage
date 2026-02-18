package com.autogarage.presentation.ui.worker

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
import com.autogarage.domain.model.Worker
import com.autogarage.domain.model.WorkerRole
import com.autogarage.domain.model.WorkerStatus
import com.autogarage.presentation.ui.components.*
import com.autogarage.presentation.ui.theme.Spacing
import com.autogarage.viewmodel.WorkerUiEvent
import com.autogarage.viewmodel.WorkersViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkersScreen(
    viewModel: WorkersViewModel = hiltViewModel(),
    onWorkerClick: (Long) -> Unit,
    onAddWorker: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterRole by viewModel.filterRole.collectAsState()
    val showActiveOnly by viewModel.showActiveOnly.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showFilterDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is WorkerUiEvent.NavigateToWorkerDetail -> onWorkerClick(event.workerId)
                is WorkerUiEvent.NavigateToAddWorker -> onAddWorker()
                is WorkerUiEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workers") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = "Filter",
                            tint = if (filterRole != null || !showActiveOnly) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                    IconButton(onClick = { viewModel.onRefresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onAddWorkerClick() },
                containerColor = MaterialTheme.colorScheme.secondary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Worker")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar
            GMSearchBar(
                query = searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                placeholder = "Search workers...",
                modifier = Modifier.padding(Spacing.medium)
            )

            // Active filter chip
            if (filterRole != null || !showActiveOnly) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.medium),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small)
                ) {
                    if (filterRole != null) {
                        FilterChip(
                            selected = true,
                            onClick = { viewModel.onFilterRoleChange(null) },
                            label = { Text(filterRole!!.name.replace("_", " ")) },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Clear",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                    if (!showActiveOnly) {
                        FilterChip(
                            selected = true,
                            onClick = { viewModel.onShowActiveOnlyChange(true) },
                            label = { Text("All Workers") },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Clear",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(Spacing.small))
            }

            when {
                uiState.isLoading -> {
                    LoadingIndicator(message = "Loading workers...")
                }
                uiState.error != null -> {
                    EmptyState(
                        icon = Icons.Default.Error,
                        title = "Error Loading Workers",
                        description = uiState.error ?: "Unknown error",
                        actionText = "Retry",
                        onActionClick = { viewModel.onRefresh() }
                    )
                }
                uiState.workers.isEmpty() -> {
                    EmptyState(
                        icon = Icons.Default.PersonAdd,
                        title = "No Workers Yet",
                        description = "Add your first worker to get started",
                        actionText = "Add Worker",
                        onActionClick = { viewModel.onAddWorkerClick() }
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(Spacing.medium),
                        verticalArrangement = Arrangement.spacedBy(Spacing.small)
                    ) {
                        items(uiState.workers, key = { it.id }) { worker ->
                            WorkerItem(
                                worker = worker,
                                onClick = { viewModel.onWorkerClick(worker.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Filter Dialog
    if (showFilterDialog) {
        WorkerFilterDialog(
            currentRole = filterRole,
            showActiveOnly = showActiveOnly,
            onRoleSelected = viewModel::onFilterRoleChange,
            onShowActiveOnlyChange = viewModel::onShowActiveOnlyChange,
            onDismiss = { showFilterDialog = false }
        )
    }
}

@Composable
private fun WorkerItem(
    worker: Worker,
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
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
            ) {
                // Avatar with role icon
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = when (worker.role) {
                        WorkerRole.MECHANIC -> MaterialTheme.colorScheme.primaryContainer
                        WorkerRole.ELECTRICIAN -> MaterialTheme.colorScheme.secondaryContainer
                        WorkerRole.PAINTER -> MaterialTheme.colorScheme.tertiaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when (worker.role) {
                                WorkerRole.MECHANIC -> Icons.Default.Build
                                WorkerRole.ELECTRICIAN -> Icons.Default.ElectricalServices
                                WorkerRole.PAINTER -> Icons.Default.FormatPaint
                                WorkerRole.SUPERVISOR -> Icons.Default.SupervisedUserCircle
                                WorkerRole.MANAGER -> Icons.Default.AdminPanelSettings
                                else -> Icons.Default.Person
                            },
                            contentDescription = null,
                            tint = when (worker.role) {
                                WorkerRole.MECHANIC -> MaterialTheme.colorScheme.onPrimaryContainer
                                WorkerRole.ELECTRICIAN -> MaterialTheme.colorScheme.onSecondaryContainer
                                WorkerRole.PAINTER -> MaterialTheme.colorScheme.onTertiaryContainer
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // Worker Info
                Column {
                    Text(
                        text = worker.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = worker.role.name.replace("_", " "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = worker.phone,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                    }
                }
            }

            // Status & Stats
            Column(
                horizontalAlignment = Alignment.End
            ) {
                when (worker.status) {
                    WorkerStatus.ACTIVE -> {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "Active",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    WorkerStatus.ON_LEAVE -> {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "On Leave",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    else -> {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                text = worker.status.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                if (worker.rating > 0) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Flag,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = String.format("%.1f", worker.rating),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (worker.completedJobs > 0) {
                    Text(
                        text = "${worker.completedJobs} jobs",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkerFilterDialog(
    currentRole: WorkerRole?,
    showActiveOnly: Boolean,
    onRoleSelected: (WorkerRole?) -> Unit,
    onShowActiveOnlyChange: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Workers") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                Text(
                    "Filter by Role",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = currentRole == null,
                        onClick = { onRoleSelected(null) },
                        label = { Text("All") }
                    )
                    WorkerRole.entries.forEach { role ->
                        FilterChip(
                            selected = currentRole == role,
                            onClick = { onRoleSelected(role) },
                            label = { Text(role.name.replace("_", " ")) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.medium))

                Text(
                    "Status",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Show active workers only")
                    Switch(
                        checked = showActiveOnly,
                        onCheckedChange = onShowActiveOnlyChange
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

// FlowRow placeholder (use accompanist or implement simple version)
@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    content: @Composable () -> Unit
) {
    // Simple horizontal scroll for now
    Row(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement
    ) {
        content()
    }
}