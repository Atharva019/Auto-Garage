package com.autogarage.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.autogarage.presentation.more.MoreScreen
import com.autogarage.presentation.settings.SettingsScreen
import com.autogarage.presentation.ui.customer.CreateCustomerScreen
import com.autogarage.presentation.ui.customer.CustomerDetailScreen
import com.autogarage.presentation.ui.customer.CustomersScreen
import com.autogarage.presentation.ui.dashboard.DashboardScreen
import com.autogarage.presentation.ui.inventory.AddInventoryItemScreen
import com.autogarage.presentation.ui.inventory.EditInventoryItemScreen
import com.autogarage.presentation.ui.inventory.InventoryScreen
import com.autogarage.presentation.ui.invoice.InvoiceScreen
import com.autogarage.presentation.ui.jobcard.JobCardDetailScreen
import com.autogarage.presentation.ui.jobcard.CreateJobCardScreen
import com.autogarage.presentation.ui.jobcard.JobCardsScreen
import com.autogarage.presentation.ui.reports.ReportsScreen
//import com.autogarage.presentation.ui.reports.ReportsScreen
import com.autogarage.presentation.ui.vehicle.AddVehicleScreen
import com.autogarage.presentation.ui.vehicle.VehicleDetailScreen
import com.autogarage.presentation.ui.worker.CreateWorkerScreen
import com.autogarage.presentation.ui.worker.EditWorkerScreen
import com.autogarage.presentation.ui.worker.WorkerDetailScreen
import com.autogarage.presentation.ui.worker.WorkersScreen

@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier
    ) {
        // Bottom Nav Screens
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToJobCards = {
                    navController.navigate(Screen.JobCards.route)
                },
                onNavigateToCustomers = {
                    navController.navigate(Screen.Customers.route)
                },
                onNavigateToInventory = {
                    navController.navigate(Screen.Inventory.route)
                },
                onNavigateToCreateJobCard = {
                    navController.navigate(Screen.CreateJobCard.route)
                },
//                onNavigateToReports = { // ✅ Added
//                    navController.navigate(Screen.Reports.route)
//                },
                onJobCardClick = { jobCardId ->
                    navController.navigate(Screen.JobCardDetail.createRoute(jobCardId))
                }
            )
        }

        composable(Screen.JobCards.route) {
            JobCardsScreen(
                onJobCardClick = { jobCardId ->
                    navController.navigate(Screen.JobCardDetail.createRoute(jobCardId))
                },
                onCreateJobCard = {
                    navController.navigate(Screen.CreateJobCard.route)
                }
            )
        }

        composable(Screen.Customers.route) {
            CustomersScreen(
                onCustomerClick = { customerId ->
                    navController.navigate(Screen.CustomerDetail.createRoute(customerId))
                },
                onAddCustomer = {
                    navController.navigate(Screen.CreateCustomer.route)
                }
            )
        }

        composable(Screen.Inventory.route) {
            InventoryScreen(
                onItemClick = { itemId ->
                    navController.navigate(Screen.InventoryDetail.createRoute(itemId))
                },
                onAddItem = {
                    navController.navigate(Screen.AddInventoryItem.route)
                }
            )
        }

        composable(
            route = Screen.EditInventoryItem.route,
            arguments = listOf(
                navArgument("itemId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getLong("itemId") ?: 0L
            EditInventoryItemScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable(Screen.More.route) {
            MoreScreen(
                onNavigateToReports = {
                    navController.navigate(Screen.Reports.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToWorkers = {
                    navController.navigate(Screen.Workers.route)
                }
            )
        }

        // Detail Screens with Arguments
        //jobcard detail
        composable(
            route = Screen.JobCardDetail.route,
            arguments = listOf(
                navArgument("jobCardId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val jobCardId = backStackEntry.arguments?.getLong("jobCardId") ?: 0L
            JobCardDetailScreen(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToInvoice = { invoiceId ->
                    navController.navigate(Screen.Invoice.createRoute(invoiceId))
                }
            )
        }
        // jobcard
        composable(Screen.JobCards.route) {
            JobCardsScreen(
                onJobCardClick = { jobCardId ->
                    navController.navigate(Screen.JobCardDetail.createRoute(jobCardId))
                },
                onCreateJobCard = {
                    navController.navigate(Screen.CreateJobCard.route)
                }
            )
        }
        // create jobcard
        composable(Screen.CreateJobCard.route) {
            CreateJobCardScreen(
                onNavigateBack = { navController.navigateUp() },
                onJobCardCreated = { jobCardId ->
                    navController.navigateUp()
                    // Optionally navigate to job card detail
                    // navController.navigate(Screen.JobCardDetail.createRoute(jobCardId))
                }
            )
        }
        // customer detail
        composable(
            route = Screen.CustomerDetail.route,
            arguments = listOf(
                navArgument("customerId") { type = NavType.LongType }
            )
        ) {
            CustomerDetailScreen(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToAddVehicle = { customerId ->
                    navController.navigate(Screen.AddVehicle.createRoute(customerId))
                },
                onNavigateToVehicleDetail = { vehicleId ->
                    navController.navigate(Screen.VehicleDetail.createRoute(vehicleId))
                },
                onNavigateToJobCardDetail = { jobCardId ->
                    navController.navigate(Screen.JobCardDetail.createRoute(jobCardId))
                }
            )
        }
        // create customer
        composable(Screen.CreateCustomer.route) {
            CreateCustomerScreen(
                onNavigateBack = { navController.navigateUp() },
                onCustomerCreated = { customerId ->
                    navController.navigateUp()
                    // Optionally navigate to customer detail
                    // navController.navigate(Screen.CustomerDetail.createRoute(customerId))
                }
            )
        }
        // inventory
        composable(Screen.Inventory.route) {
            InventoryScreen(
                onItemClick = { itemId ->
                    navController.navigate(Screen.EditInventoryItem.createRoute(itemId))
                },
                onAddItem = {
                    navController.navigate(Screen.AddInventoryItem.route)
                }
            )
        }

        composable(Screen.AddInventoryItem.route) {
            AddInventoryItemScreen(
                onNavigateBack = { navController.navigateUp() },
                onItemAdded = { itemId ->
                    navController.navigateUp()
                }
            )
        }

        //vehicle
        composable(
            route = Screen.AddVehicle.route,
            arguments = listOf(
                navArgument("customerId") { type = NavType.LongType }
            )
        ) {
            AddVehicleScreen(
                onNavigateBack = { navController.navigateUp() },
                onVehicleAdded = { vehicleId ->
                    navController.navigateUp()
                    // Optionally navigate to vehicle detail
                    // navController.navigate(Screen.VehicleDetail.createRoute(vehicleId))
                }
            )
        }
        composable(
            route = Screen.VehicleDetail.route,
            arguments = listOf(
                navArgument("vehicleId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getLong("vehicleId") ?: 0L
            VehicleDetailScreen(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToEdit = { vehicleId ->
                    navController.navigate(Screen.EditVehicle.createRoute(vehicleId))
                },
                onNavigateToJobCard = { jobCardId ->
                    navController.navigate(Screen.JobCardDetail.createRoute(jobCardId))
                },
                onNavigateToCreateJobCard = { vehicleId ->
                    navController.navigate(Screen.CreateJobCard.createRoute(vehicleId))
                },
                onNavigateToCustomer = { customerId ->
                    navController.navigate(Screen.CustomerDetail.createRoute(customerId))
                }
            )
        }

        //  Invoice
        composable(
            route = Screen.Invoice.route,
            arguments = listOf(
                navArgument("invoiceId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val invoiceId = backStackEntry.arguments?.getLong("invoiceId") ?: 0L
            InvoiceScreen(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToJobCard = { jobCardId ->
                    navController.navigate(Screen.JobCardDetail.createRoute(jobCardId))
                }
            )
        }

        // more screens
        composable(Screen.Reports.route) {
            PlaceholderScreen("Report") {

            }
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }

        // Worker
        composable(Screen.Workers.route) {
            WorkersScreen(
                onWorkerClick = { workerId ->
                    navController.navigate(Screen.WorkerDetail.createRoute(workerId))
                },
                onAddWorker = {
                    navController.navigate(Screen.AddWorker.route)
                },
                onNavigateBack = { navController.navigateUp() }
            )
        }

// Add this new composable for CreateWorkerScreen
        composable(Screen.AddWorker.route) {
            CreateWorkerScreen(
                onNavigateBack = { navController.navigateUp() },
                onWorkerCreated = { workerId ->
                    navController.navigateUp()
                    // Optionally navigate to worker detail after creation
                    // navController.navigate(Screen.WorkerDetail.createRoute(workerId))
                }
            )
        }

        composable(
            route = Screen.EditWorker.route,
            arguments = listOf(
                navArgument("workerId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val workerId = backStackEntry.arguments?.getLong("workerId") ?: 0L
            EditWorkerScreen(
                onNavigateBack = { navController.navigateUp() },
                onWorkerUpdated = {
                    navController.navigateUp()
                }
            )
        }

        composable(
            Screen.WorkerDetail.route,
            arguments = Screen.WorkerDetail.arguments)
        {
            WorkerDetailScreen(onNavigateBack = { navController.navigateUp() },
                onNavigateToEdit = {navController.navigate(
                Screen.EditWorker.createRoute(it))})
        }

        // ✅ REPORTS SCREEN - Added
        composable(Screen.Reports.route) {
            ReportsScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }
    }
}

// Placeholder screen for routes we haven't built yet
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaceholderScreen(
    title: String,
    onNavigateBack: () -> Unit
) {
    androidx.compose.material3.Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { androidx.compose.material3.Text(title) },
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = onNavigateBack) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        androidx.compose.foundation.layout.Box(
            modifier = androidx.compose.ui.Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            androidx.compose.material3.Text(
                text = "$title\n(Coming Soon)",
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}