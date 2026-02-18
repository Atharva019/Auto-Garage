package com.autogarage.presentation.ui.components

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.autogarage.viewmodel.CreateCustomerViewModel

// ✅ OPTIMIZATION: Use derivedStateOf for computed values
@Composable
fun CustomerForm(viewModel: CreateCustomerViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    // ✅ Only recomposes when validation actually changes
    val isFormValid by remember {
        derivedStateOf {
            uiState.name.isNotBlank() &&
                    uiState.phone.length == 10
        }
    }

    // UI code...
}