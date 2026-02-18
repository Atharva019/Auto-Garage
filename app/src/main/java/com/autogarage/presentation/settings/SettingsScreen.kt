package com.autogarage.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.autogarage.domain.repository.ThemeMode
import com.autogarage.presentation.ui.components.*
import com.autogarage.presentation.ui.theme.Spacing
import com.autogarage.viewmodel.SettingsUiEvent
import com.autogarage.viewmodel.SettingsViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showBusinessDialog by remember { mutableStateOf(false) }
    var showAppSettingsDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is SettingsUiEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is SettingsUiEvent.ShowError -> {
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
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (uiState.isLoading) {
            LoadingIndicator(message = "Loading settings...")
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Business Information Section
                SettingsSectionHeader(title = "Business Information")

                SettingsItem(
                    icon = Icons.Default.Business,
                    title = "Business Details",
                    subtitle = uiState.businessName.ifEmpty { "Not set" },
                    onClick = { showBusinessDialog = true }
                )

                Divider(modifier = Modifier.padding(horizontal = Spacing.medium))

                // App Settings Section
                SettingsSectionHeader(title = "App Settings")

                SettingsItem(
                    icon = Icons.Default.Palette,
                    title = "Theme",
                    subtitle = when (uiState.themeMode) {
                        ThemeMode.LIGHT -> "Light"
                        ThemeMode.DARK -> "Dark"
                        ThemeMode.SYSTEM -> "System Default"
                    },
                    onClick = { showThemeDialog = true }
                )

                Divider(modifier = Modifier.padding(horizontal = Spacing.medium))

                SettingsItem(
                    icon = Icons.Default.AttachMoney,
                    title = "Currency & Tax",
                    subtitle = "${uiState.currency} • Tax: ${uiState.defaultTaxRate}%",
                    onClick = { showAppSettingsDialog = true }
                )

                Divider(modifier = Modifier.padding(horizontal = Spacing.medium))

                // Notifications Section
                SettingsSectionHeader(title = "Notifications")

                SettingsSwitchItem(
                    icon = Icons.Default.Inventory,
                    title = "Low Stock Alerts",
                    subtitle = "Get notified when inventory is low",
                    checked = uiState.lowStockAlertEnabled,
                    onCheckedChange = viewModel::onLowStockAlertToggle
                )

                Divider(modifier = Modifier.padding(horizontal = Spacing.medium))

                SettingsSwitchItem(
                    icon = Icons.Default.Notifications,
                    title = "Job Completion",
                    subtitle = "Notify when job card is completed",
                    checked = uiState.jobCompletionNotificationEnabled,
                    onCheckedChange = viewModel::onJobCompletionNotificationToggle
                )

                Divider(modifier = Modifier.padding(horizontal = Spacing.medium))

                // About Section
                SettingsSectionHeader(title = "About")

                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "Version",
                    subtitle = "1.0.0",
                    onClick = {}
                )

                Spacer(modifier = Modifier.height(Spacing.large))
            }
        }
    }

    // Business Details Dialog
    if (showBusinessDialog) {
        BusinessDetailsDialog(
            uiState = uiState,
            onNameChange = viewModel::onBusinessNameChange,
            onPhoneChange = viewModel::onBusinessPhoneChange,
            onEmailChange = viewModel::onBusinessEmailChange,
            onAddressChange = viewModel::onBusinessAddressChange,
            onGstNumberChange = viewModel::onGstNumberChange,
            onSave = {
                viewModel.onSaveBusinessSettings()
                showBusinessDialog = false
            },
            onDismiss = { showBusinessDialog = false }
        )
    }

    // App Settings Dialog
    if (showAppSettingsDialog) {
        AppSettingsDialog(
            currency = uiState.currency,
            taxRate = uiState.defaultTaxRate,
            onCurrencyChange = viewModel::onCurrencyChange,
            onTaxRateChange = viewModel::onDefaultTaxRateChange,
            onSave = {
                viewModel.onSaveAppSettings()
                showAppSettingsDialog = false
            },
            onDismiss = { showAppSettingsDialog = false }
        )
    }

    // Theme Dialog
    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = uiState.themeMode,
            onThemeSelected = {
                viewModel.onThemeModeChange(it)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(
            horizontal = Spacing.medium,
            vertical = Spacing.small
        )
    )
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingContent = {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null
            )
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    )
}