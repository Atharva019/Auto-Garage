package com.autogarage.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector? = null
) {
    // Bottom Nav Screens
    data object Dashboard : Screen(
        route = "dashboard",
        title = "Dashboard",
        icon = Icons.Default.Dashboard
    )

    data object JobCards : Screen(
        route = "job_cards",
        title = "Job Cards",
        icon = Icons.Default.Assistant
    )

    data object Customers : Screen(
        route = "customers",
        title = "Customers",
        icon = Icons.Default.Person4
    )

    data object Inventory : Screen(
        route = "inventory",
        title = "Inventory",
        icon = Icons.Default.Inventory
    )

    data object More : Screen(
        route = "more",
        title = "More",
        icon = Icons.Default.Menu
    )

    // Detail Screens (not in bottom nav)
    data object JobCardDetail : Screen(
        route = "job_card_detail/{jobCardId}",
        title = "Job Card Details"
    ) {
        fun createRoute(jobCardId: Long) = "job_card_detail/$jobCardId"
    }
    object CreateJobCard : Screen(
        "create_job_card?vehicleId={vehicleId}"
        ,"Create Job Card") {
        fun createRoute(vehicleId: Long): String {
            return "create_job_card?vehicleId=$vehicleId"
        }
    }

    data object CustomerDetail : Screen(
        route = "customer_detail/{customerId}",
        title = "Customer Details"
    ) {
        fun createRoute(customerId: Long) = "customer_detail/$customerId"
    }

    data object CreateCustomer : Screen(
        route = "create_customer",
        title = "Add Customer"
    )
    data object AddVehicle : Screen(
        route = "add_vehicle/{customerId}",
        title = "Add Vehicle"
    ) {
        fun createRoute(customerId: Long) = "add_vehicle/$customerId"
    }
    data object VehicleDetail : Screen(
        route = "vehicle_detail/{vehicleId}",
        title = "Vehicle Details"
    ) {
        fun createRoute(vehicleId: Long) = "vehicle_detail/$vehicleId"
    }

    data object EditVehicle : Screen(
        route = "edit_vehicle/{vehicleId}",
        title = "Edit Vehicle"
    ) {
        fun createRoute(vehicleId: Long) = "edit_vehicle/$vehicleId"
    }

    data object InventoryDetail : Screen(
        route = "inventory_detail/{itemId}",
        title = "Item Details"
    ) {
        fun createRoute(itemId: Long) = "inventory_detail/$itemId"
    }

    data object AddInventoryItem : Screen(
        route = "add_inventory_item",
        title = "Add Item"
    )

    data object EditInventoryItem : Screen(
        route = "edit_inventory_item/{itemId}",
        title = "Edit Item"
    ) {
        fun createRoute(itemId: Long) = "edit_inventory_item/$itemId"
    }
    data object Reports : Screen(
        route = "reports",
        title = "Reports"
    )

    data object Settings : Screen(
        route = "settings",
        title = "Settings"
    )

    data object Workers : Screen(
        route = "workers",
        title = "Workers"
    )

    data object AddWorker : Screen(
        route = "add_worker",
        title = "Add Worker"
    )
    data object EditWorker : Screen(
        route = "edit_worker/{workerId}",
        title = "Edit Worker"
    ) {
        fun createRoute(workerId: Long) = "edit_worker/$workerId"
    }

    data object WorkerDetail : Screen(
        route = "worker_detail/{workerId}",
        title = "Worker Details"
    ) {
        fun createRoute(workerId: Long) = "worker_detail/$workerId"

        // ✅ Add argument definition
        val arguments = listOf(
            navArgument("workerId") {
                type = NavType.LongType
                nullable = false
            }
        )
    }

    data object Invoice : Screen(
        route = "invoice/{invoiceId}",
        title = "Invoice"
    ) {
        fun createRoute(invoiceId: Long) = "invoice/$invoiceId"
    }
}

// Bottom navigation items
val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.JobCards,
    Screen.Customers,
    Screen.Inventory,
    Screen.More
)