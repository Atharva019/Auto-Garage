package com.autogarage.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.autogarage.presentation.ui.components.GMTextField
import com.autogarage.presentation.ui.theme.Spacing
import com.autogarage.viewmodel.SettingsUiState

@Composable
fun BusinessDetailsDialog(
    uiState: SettingsUiState,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onGstNumberChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Business,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text("Business Details")
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Spacing.medium)
            ) {
                GMTextField(
                    value = uiState.businessName,
                    onValueChange = onNameChange,
                    label = "Business Name",
                    placeholder = "Enter business name",
                    leadingIcon = Icons.Default.Store
                )

                GMTextField(
                    value = uiState.businessPhone,
                    onValueChange = onPhoneChange,
                    label = "Phone Number",
                    placeholder = "10 digit number",
                    leadingIcon = Icons.Default.Phone,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                GMTextField(
                    value = uiState.businessEmail,
                    onValueChange = onEmailChange,
                    label = "Email Address",
                    placeholder = "business@example.com",
                    leadingIcon = Icons.Default.Email,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                GMTextField(
                    value = uiState.businessAddress,
                    onValueChange = onAddressChange,
                    label = "Address",
                    placeholder = "Enter business address",
                    leadingIcon = Icons.Default.LocationOn,
                    singleLine = false,
                    maxLines = 3
                )

                GMTextField(
                    value = uiState.gstNumber,
                    onValueChange = onGstNumberChange,
                    label = "GST Number",
                    placeholder = "Enter GST number",
                    leadingIcon = Icons.Default.Description
                )
            }
        },
        confirmButton = {
            Button(onClick = onSave) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}




// ===========================================================================
// Add to RepositoryModule.kt
// ===========================================================================
/*
@Binds
@Singleton
abstract fun bindSettingsRepository(
    settingsRepositoryImpl: SettingsRepositoryImpl
): SettingsRepository
*/

// ===========================================================================
// Update AppNavigation.kt - Add Settings Route
// ===========================================================================
/*
import com.garagemaster.presentation.settings.SettingsScreen

composable(Screen.Settings.route) {
    SettingsScreen(
        onNavigateBack = { navController.navigateUp() }
    )
}
*/