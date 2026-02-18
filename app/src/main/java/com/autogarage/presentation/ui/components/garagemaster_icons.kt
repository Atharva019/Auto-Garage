// ===========================================================================
// GarageMasterIcons.kt - Centralized Icon Management
// ===========================================================================
package com.autogarage.presentation.ui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.painter.Painter
import com.autogarage.R
import com.autogarage.domain.model.WorkerRole
import com.autogarage.domain.model.WorkerStatus

/**
 * Centralized icon definitions for GarageMaster app
 * 
 * Usage:
 * Icon(GarageMasterIcons.Worker.Mechanic, contentDescription = "Mechanic")
 * Icon(painterResource(GarageMasterIcons.Custom.wrench), contentDescription = "Wrench")
 */
object GarageMasterIcons {
    
    // ===========================================================================
    // Worker Role Icons
    // ===========================================================================
    object Worker {
        val Mechanic: ImageVector get() = Icons.Filled.Build
        val Electrician: ImageVector get() = Icons.Filled.Star // Replace when you add extended icons
        val Painter: ImageVector get() = Icons.Filled.Brush
        val Supervisor: ImageVector get() = Icons.Filled.SupervisorAccount
        val Manager: ImageVector get() = Icons.Filled.ManageAccounts
        val Helper: ImageVector get() = Icons.Filled.Person
    }
    
    // ===========================================================================
    // Job Card & Service Icons
    // ===========================================================================
    object Service {
        val JobCard: ImageVector get() = Icons.Filled.Assignment
        val InProgress: ImageVector get() = Icons.Filled.Construction
        val Completed: ImageVector get() = Icons.Filled.CheckCircle
        val Pending: ImageVector get() = Icons.Filled.Schedule
        val Cancelled: ImageVector get() = Icons.Filled.Cancel
        val Delivered: ImageVector get() = Icons.Filled.LocalShipping
        val HighPriority: ImageVector get() = Icons.Filled.PriorityHigh
        val LowPriority: ImageVector get() = Icons.Filled.LowPriority
        val ServiceHistory: ImageVector get() = Icons.Filled.History
    }
    
    // ===========================================================================
    // Vehicle Icons
    // ===========================================================================
    object Vehicle {
        val Car: ImageVector get() = Icons.Filled.DirectionsCar
        val Registration: ImageVector get() = Icons.Filled.Description
        val Model: ImageVector get() = Icons.Filled.CarRental
        val VIN: ImageVector get() = Icons.Filled.Tag
        val Year: ImageVector get() = Icons.Filled.CalendarToday
    }
    
    // ===========================================================================
    // Customer Icons
    // ===========================================================================
    object Customer {
        val Person: ImageVector get() = Icons.Filled.Person
        val People: ImageVector get() = Icons.Filled.People
        val Add: ImageVector get() = Icons.Filled.PersonAdd
        val Phone: ImageVector get() = Icons.Filled.Phone
        val Email: ImageVector get() = Icons.Filled.Email
        val Location: ImageVector get() = Icons.Filled.LocationOn
        val Points: ImageVector get() = Icons.Filled.Stars
        val Gst: ImageVector get() = Icons.Filled.AttachMoney
        val AddNote: ImageVector get() = Icons.Filled.Note
        val AccountBalanceWallet: ImageVector get() = Icons.Filled.AccountBalanceWallet
        val loyaltyPoints: ImageVector get() = Icons.Filled.Loyalty
    }
    
    // ===========================================================================
    // Inventory Icons
    // ===========================================================================
    object Inventory {
        val Items: ImageVector get() = Icons.Filled.Inventory
        val InStock: ImageVector get() = Icons.Filled.CheckCircle
        val LowStock: ImageVector get() = Icons.Filled.Warning
        val OutOfStock: ImageVector get() = Icons.Filled.Error
        val Parts: ImageVector get() = Icons.Filled.Extension
        val Category: ImageVector get() = Icons.Filled.Category
    }
    
    // ===========================================================================
    // Financial Icons
    // ===========================================================================
    object Financial {
        val Invoice: ImageVector get() = Icons.Filled.Receipt
        val Payment: ImageVector get() = Icons.Filled.Payment
        val Cash: ImageVector get() = Icons.Filled.Money
        val Card: ImageVector get() = Icons.Filled.CreditCard
        val UPI: ImageVector get() = Icons.Filled.QrCode2
        val Revenue: ImageVector get() = Icons.Filled.TrendingUp
        val Discount: ImageVector get() = Icons.Filled.Discount
    }
    
    // ===========================================================================
    // Navigation Icons
    // ===========================================================================
    object Navigation {
        val Dashboard: ImageVector get() = Icons.Filled.Dashboard
        val Settings: ImageVector get() = Icons.Filled.Settings
        val Reports: ImageVector get() = Icons.Filled.Assessment
        val More: ImageVector get() = Icons.Filled.MoreVert
        val Menu: ImageVector get() = Icons.Filled.Menu
        val Back: ImageVector get() = Icons.Filled.ArrowBack
        val Forward: ImageVector get() = Icons.Filled.ArrowForward
    }
    
    // ===========================================================================
    // Action Icons
    // ===========================================================================
    object Action {
        val Add: ImageVector get() = Icons.Filled.Add
        val Edit: ImageVector get() = Icons.Filled.Edit
        val Delete: ImageVector get() = Icons.Filled.Delete
        val Save: ImageVector get() = Icons.Filled.Save
        val Close: ImageVector get() = Icons.Filled.Close
        val Done: ImageVector get() = Icons.Filled.Done
        val Refresh: ImageVector get() = Icons.Filled.Refresh
        val Search: ImageVector get() = Icons.Filled.Search
        val Filter: ImageVector get() = Icons.Filled.FilterList
        val Sort: ImageVector get() = Icons.Filled.Sort
        val Share: ImageVector get() = Icons.Filled.Share
        val Download: ImageVector get() = Icons.Filled.Download
        val Upload: ImageVector get() = Icons.Filled.Upload
        val Print: ImageVector get() = Icons.Filled.Print
        val Call: ImageVector get() = Icons.Filled.Call
    }
    
    // ===========================================================================
    // Status Icons
    // ===========================================================================
    object Status {
        val Active: ImageVector get() = Icons.Filled.CheckCircle
        val Inactive: ImageVector get() = Icons.Filled.Block
        val Warning: ImageVector get() = Icons.Filled.Warning
        val Error: ImageVector get() = Icons.Filled.Error
        val Info: ImageVector get() = Icons.Filled.Info
        val Success: ImageVector get() = Icons.Filled.CheckCircle
        val OnLeave: ImageVector get() = Icons.Filled.EventBusy
    }
    
    // ===========================================================================
    // Document Icons
    // ===========================================================================
    object Document {
        val File: ImageVector get() = Icons.Filled.Description
        val PDF: ImageVector get() = Icons.Filled.PictureAsPdf
        val Image: ImageVector get() = Icons.Filled.Image
        val Camera: ImageVector get() = Icons.Filled.CameraAlt
        val Attachment: ImageVector get() = Icons.Filled.AttachFile
        val Folder: ImageVector get() = Icons.Filled.Folder
    }
    
    // ===========================================================================
    // Miscellaneous Icons
    // ===========================================================================
    object Misc {
        val Star: ImageVector get() = Icons.Filled.Star
        val StarOutline: ImageVector get() = Icons.Outlined.Star
        val Favorite: ImageVector get() = Icons.Filled.Favorite
        val Calendar: ImageVector get() = Icons.Filled.CalendarToday
        val Time: ImageVector get() = Icons.Filled.Schedule
        val Notification: ImageVector get() = Icons.Filled.Notifications
        val Help: ImageVector get() = Icons.Filled.Help
        val Security: ImageVector get() = Icons.Filled.Security
        val Visibility: ImageVector get() = Icons.Filled.Visibility
        val VisibilityOff: ImageVector get() = Icons.Filled.VisibilityOff
        val ChevronRight: ImageVector get() = Icons.Filled.ChevronRight
        val ChevronLeft: ImageVector get() = Icons.Filled.ChevronLeft
        val ExpandMore: ImageVector get() = Icons.Filled.ExpandMore
        val ExpandLess: ImageVector get() = Icons.Filled.ExpandLess
    }
    
    // ===========================================================================
    // Custom Drawable Icons (Add your custom XML drawables here)
    // ===========================================================================
    object Custom {
        // Uncomment and add resource IDs when you create custom icons
        // val wrench: Int get() = R.drawable.ic_wrench
        // val carEngine: Int get() = R.drawable.ic_car_engine
        // val oilChange: Int get() = R.drawable.ic_oil_change
        // val tire: Int get() = R.drawable.ic_tire
        // val battery: Int get() = R.drawable.ic_battery
        // val brakeDisc: Int get() = R.drawable.ic_brake_disc
    }
}

// ===========================================================================
// Extension Functions for Easy Usage
// ===========================================================================

/**
 * Get icon for worker role
 */
fun getWorkerRoleIcon(role: com.autogarage.domain.model.WorkerRole): ImageVector {
    return when (role) {
        WorkerRole.MECHANIC -> GarageMasterIcons.Worker.Mechanic
        WorkerRole.ELECTRICIAN -> GarageMasterIcons.Worker.Electrician
        WorkerRole.PAINTER -> GarageMasterIcons.Worker.Painter
        WorkerRole.SUPERVISOR -> GarageMasterIcons.Worker.Supervisor
        WorkerRole.MANAGER -> GarageMasterIcons.Worker.Manager
        WorkerRole.HELPER -> GarageMasterIcons.Worker.Helper
        WorkerRole.WELDER -> GarageMasterIcons.Worker.Painter
    }
}

/**
 * Get icon for worker status
 */
fun getWorkerStatusIcon(status: com.autogarage.domain.model.WorkerStatus): ImageVector {
    return when (status) {
        WorkerStatus.ACTIVE -> GarageMasterIcons.Status.Active
        WorkerStatus.ON_LEAVE -> GarageMasterIcons.Status.OnLeave
        WorkerStatus.INACTIVE -> GarageMasterIcons.Status.Inactive
        WorkerStatus.TERMINATED -> GarageMasterIcons.Status.Error
        WorkerStatus.RESIGNED -> GarageMasterIcons.Status.OnLeave
    }
}

/**
 * Get icon for job card status
 */
fun getJobCardStatusIcon(status: com.autogarage.domain.model.JobCardStatus): ImageVector {
    return when (status) {
        com.autogarage.domain.model.JobCardStatus.PENDING -> GarageMasterIcons.Service.Pending
        com.autogarage.domain.model.JobCardStatus.IN_PROGRESS -> GarageMasterIcons.Service.InProgress
        com.autogarage.domain.model.JobCardStatus.COMPLETED -> GarageMasterIcons.Service.Completed
        com.autogarage.domain.model.JobCardStatus.CANCELLED -> GarageMasterIcons.Service.Cancelled
        com.autogarage.domain.model.JobCardStatus.DELIVERED -> GarageMasterIcons.Service.Delivered
    }
}

/**
 * Get icon for stock status
 */
fun getStockStatusIcon(status: com.autogarage.domain.model.StockStatus): ImageVector {
    return when (status) {
        com.autogarage.domain.model.StockStatus.IN_STOCK -> GarageMasterIcons.Inventory.InStock
        com.autogarage.domain.model.StockStatus.LOW_STOCK -> GarageMasterIcons.Inventory.LowStock
        com.autogarage.domain.model.StockStatus.OUT_OF_STOCK -> GarageMasterIcons.Inventory.OutOfStock
    }
}

// ===========================================================================
// Usage Examples
// ===========================================================================
/*
// 1. Using Material Icons (most common)
Icon(
    imageVector = GarageMasterIcons.Worker.Mechanic,
    contentDescription = "Mechanic",
    tint = MaterialTheme.colorScheme.primary
)

// 2. Using custom drawable icons
Icon(
    painter = painterResource(GarageMasterIcons.Custom.wrench),
    contentDescription = "Wrench",
    tint = MaterialTheme.colorScheme.primary
)

// 3. Using extension functions
Icon(
    imageVector = getWorkerRoleIcon(worker.role),
    contentDescription = worker.role.name,
    tint = MaterialTheme.colorScheme.primary
)

// 4. In your composables
IconButton(onClick = { /* action */ }) {
    Icon(GarageMasterIcons.Action.Edit, contentDescription = "Edit")
}

// 5. With custom size
Icon(
    imageVector = GarageMasterIcons.Dashboard,
    contentDescription = "Dashboard",
    modifier = Modifier.size(32.dp)
)
*/
