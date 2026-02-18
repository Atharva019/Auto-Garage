package com.autogarage.presentation.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    onNavigateToReports: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToWorkers: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("More") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            MenuItemCard(
                icon = Icons.Default.Analytics,
                title = "Reports",
                subtitle = "View analytics and reports",
                onClick = onNavigateToReports
            )

            Divider()

            MenuItemCard(
                icon = Icons.Default.PermIdentity,
                title = "Workers",
                subtitle = "Manage employees",
                onClick = onNavigateToWorkers
            )

            Divider()

            MenuItemCard(
                icon = Icons.Default.Settings,
                title = "Settings",
                subtitle = "App preferences",
                onClick = onNavigateToSettings
            )
        }
    }
}

@Composable
private fun MenuItemCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
                modifier = Modifier.size(32.dp)
            )
        },
//        trailingContent = {
//            Icon(
//                imageVector = Icons.Default.Star,
//                contentDescription = null
//            )
//        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}