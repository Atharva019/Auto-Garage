package com.autogarage.presentation.ui.invoice

import com.autogarage.R
import android.content.Intent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.autogarage.domain.model.*
import com.autogarage.presentation.ui.components.*
import com.autogarage.presentation.ui.theme.Spacing
import com.autogarage.viewmodel.InvoiceUiEvent
import com.autogarage.viewmodel.InvoiceViewModel
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceScreen(
    viewModel: InvoiceViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToJobCard: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is InvoiceUiEvent.NavigateToJobCard -> onNavigateToJobCard(event.jobCardId)
                is InvoiceUiEvent.PaymentRecorded -> {
                    // Payment recorded - invoice will refresh automatically
                }
                is InvoiceUiEvent.PdfGenerated -> {
                    // Open PDF or show options
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(
                            FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.provider",
                                event.file
                            ),
                            "application/pdf"
                        )
                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    }
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // No PDF viewer installed
                        snackbarHostState.showSnackbar(
                            "No PDF viewer found. File saved at: ${event.file.absolutePath}"
                        )
                    }
                }
                is InvoiceUiEvent.SharePdf -> {
                    // Share PDF
                    try {
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.provider",
                            event.file
                        )
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/pdf"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        }
                        context.startActivity(Intent.createChooser(intent, "Share Invoice"))
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("Failed to share PDF")
                    }
                }
                is InvoiceUiEvent.PrintPdf -> {
                    // Print PDF - can be enhanced with Android Print Framework
                    try {
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.provider",
                            event.file
                        )
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, "application/pdf")
                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("Failed to open PDF for printing")
                    }
                }
                is InvoiceUiEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is InvoiceUiEvent.ShowError -> {
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
                title = {
                    Text(uiState.invoice?.invoiceNumber ?: "Invoice")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }

                    // More options menu
                    var showMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Download PDF") },
                            onClick = {
                                showMenu = false
                                viewModel.onDownloadPdfClick()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Download, contentDescription = null)//Download
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Share") },
                            onClick = {
                                showMenu = false
                                viewModel.onShareClick()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Share, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Print") },
                            onClick = {
                                showMenu = false
                                viewModel.onPrintClick()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Print, contentDescription = null)//Print
                            }
                        )
                        if (uiState.invoice?.paymentStatus?.name == "UNPAID") {
                            Divider()
                            DropdownMenuItem(
                                text = { Text("Cancel Invoice") },
                                onClick = {
                                    showMenu = false
                                    viewModel.onCancelInvoiceClick()
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Cancel,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = MaterialTheme.colorScheme.error
                                )
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when {
            uiState.isLoading -> {
                LoadingIndicator(message = "Loading invoice...")
            }
            uiState.error != null -> {
                EmptyState(
                    icon = Icons.Default.Error,
                    title = "Error",
                    description = uiState.error ?: "Unknown error",
                    actionText = "Retry",
                    onActionClick = viewModel::onRefresh
                )
            }
            uiState.invoice != null -> {
                InvoiceContent(
                    invoice = uiState.invoice!!,
                    onRecordPayment = viewModel::onRecordPaymentClick,
                    onViewJobCard = viewModel::onNavigateToJobCard,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }

    // PDF Generation Loading Overlay
    if (uiState.isGeneratingPdf) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
                //.padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.padding(32.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Generating PDF...",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }

    // Payment Dialog
    if (uiState.showPaymentDialog) {
        RecordPaymentDialog(
            invoice = uiState.invoice!!,
            selectedPaymentMode = uiState.selectedPaymentMode,
            transactionId = uiState.transactionId,
            isProcessing = uiState.isProcessingPayment,
            onPaymentModeChange = viewModel::onPaymentModeChange,
            onTransactionIdChange = viewModel::onTransactionIdChange,
            onConfirm = viewModel::onConfirmPayment,
            onDismiss = viewModel::onPaymentDialogDismiss
        )
    }

    // Cancel Confirmation Dialog
    if (uiState.showCancelDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onCancelDialogDismiss,
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Cancel Invoice?") },
            text = {
                Text("Are you sure you want to cancel this invoice? This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = viewModel::onConfirmCancel,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Cancel Invoice")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onCancelDialogDismiss) {
                    Text("Keep Invoice")
                }
            }
        )
    }
    if (uiState.isGeneratingPdf) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }


}

// ===========================================================================
// Invoice Content - Main Display
// ===========================================================================
@Composable
private fun InvoiceContent(
    invoice: Invoice,
    onRecordPayment: () -> Unit,
    onViewJobCard: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        // Payment Status Banner
        PaymentStatusBanner(invoice = invoice)

        // Invoice Header Card
        InvoiceHeaderCard(invoice = invoice)

        // Customer & Vehicle Info
        CustomerVehicleCard(invoice = invoice, onViewJobCard = onViewJobCard)

        // Cost Breakdown
        CostBreakdownCard(invoice = invoice)

        // Payment Information (if paid)
        if (invoice.paymentStatus == PaymentStatus.PAID) {
            PaymentInfoCard(invoice = invoice)
        }

        // Terms & Conditions
        if (invoice.termsAndConditions != null) {
            TermsCard(terms = invoice.termsAndConditions)
        }

        // Action Buttons
        if (invoice.paymentStatus == PaymentStatus.UNPAID) {
            Spacer(modifier = Modifier.height(Spacing.medium))
            GMButton(
                text = "Record Payment",
                onClick = onRecordPayment,
                type = ButtonType.PRIMARY,
                size = ButtonSize.LARGE,
                icon = Icons.Default.Payment,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun PaymentStatusBanner(invoice: Invoice) {
    val (backgroundColor, textColor, icon, statusText) = when (invoice.paymentStatus) {
        PaymentStatus.PAID -> Tuple4(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            Icons.Default.CheckCircle,
            "PAID"
        )
        PaymentStatus.UNPAID -> Tuple4(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            Icons.Default.PendingActions,
            "UNPAID"
        )
        PaymentStatus.CANCELLED -> Tuple4(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            Icons.Default.Cancel,
            "CANCELLED"
        )
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Spacing.cardCornerRadiusSmall),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
            horizontalArrangement = Arrangement.spacedBy(Spacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(32.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                if (invoice.paymentStatus == PaymentStatus.UNPAID) {
                    Text(
                        text = "Amount Due: ₹${String.format("%.2f", invoice.totalAmount)}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = textColor
                    )
                }
            }
        }
    }
}

// ===========================================================================
// Invoice Header Card
// ===========================================================================
@Composable
fun InvoiceHeaderCard(invoice: Invoice) {
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
            // Invoice Number
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Invoice Number",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = invoice.invoiceNumber,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Divider()

            // Invoice Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Invoice Date",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatTimestamp(invoice.invoiceDate),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            // Job Card Number
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Job Card",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = invoice.jobCard.jobCardNumber,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// ===========================================================================
// Customer & Vehicle Card
// ===========================================================================
@Composable
fun CustomerVehicleCard(
    invoice: Invoice,
    onViewJobCard: () -> Unit
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
            verticalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            Text(
                text = "Vehicle Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            // Registration Number
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = invoice.jobCard.vehicle.registrationNumber,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Vehicle Info
            Text(
                text = "${invoice.jobCard.vehicle.make} ${invoice.jobCard.vehicle.model}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "Year: ${invoice.jobCard.vehicle.year}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Divider()

            // View Job Card Button
            OutlinedButton(
                onClick = onViewJobCard,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Assignment,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("View Job Card Details")
            }
        }
    }
}

// ===========================================================================
// Cost Breakdown Card
// ===========================================================================
@Composable
fun CostBreakdownCard(invoice: Invoice) {
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
            Text(
                text = "Cost Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = Spacing.small)
            )

            // Labor Cost
            CostRow(
                label = "Labor Cost",
                amount = invoice.laborCost
            )

            // Parts Cost
            CostRow(
                label = "Parts Cost",
                amount = invoice.partsCost
            )

            Divider()

            // Subtotal
            CostRow(
                label = "Subtotal",
                amount = invoice.subtotal,
                fontWeight = FontWeight.Medium
            )

            // Discount (if any)
            if (invoice.discount > 0) {
                CostRow(
                    label = if (invoice.discountPercentage > 0) {
                        "Discount (${invoice.discountPercentage}%)"
                    } else {
                        "Discount"
                    },
                    amount = -invoice.discount,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Divider()

            // Taxable Amount
            CostRow(
                label = "Taxable Amount",
                amount = invoice.taxableAmount,
                fontWeight = FontWeight.Medium
            )

            // Tax
            CostRow(
                label = "GST (${invoice.taxRate}%)",
                amount = invoice.taxAmount
            )

            Divider(thickness = 2.dp)

            // Total Amount
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(Spacing.small),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Amount",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "₹${String.format("%.2f", invoice.totalAmount)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun CostRow(
    label: String,
    amount: Double,
    fontWeight: FontWeight = FontWeight.Normal,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = fontWeight,
            color = if (amount < 0) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "${if (amount < 0) "-" else ""}₹${String.format("%.2f", Math.abs(amount))}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = fontWeight,
            color = color
        )
    }
}

// ===========================================================================
// Payment Info Card (for paid invoices)
// ===========================================================================
@Composable
fun PaymentInfoCard(invoice: Invoice) {
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
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Payment Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Divider()

            // Payment Mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Payment Mode",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = invoice.paymentMode?.name ?: "N/A",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // Payment Date
            invoice.paymentDate?.let {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Payment Date",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = formatTimestamp(it),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Transaction ID
            invoice.transactionId?.let {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Transaction ID",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Amount Paid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Amount Paid",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "₹${String.format("%.2f", invoice.paidAmount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// ===========================================================================
// Terms & Conditions Card
// ===========================================================================
@Composable
fun TermsCard(terms: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            Text(
                text = "Terms & Conditions",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = terms,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ===========================================================================
// Record Payment Dialog
// ===========================================================================
@Composable
fun RecordPaymentDialog(
    invoice: Invoice,
    selectedPaymentMode: PaymentMode,
    transactionId: String,
    isProcessing: Boolean,
    onPaymentModeChange: (PaymentMode) -> Unit,
    onTransactionIdChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = if (!isProcessing) onDismiss else {{}},
        icon = {
            Icon(
                Icons.Default.Payment,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = { Text("Record Payment") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.medium)
            ) {
                // Amount to be paid
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.medium),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Amount",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "₹${String.format("%.2f", invoice.totalAmount)}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Payment Mode Selection
                Text(
                    text = "Payment Mode",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Column {
                    PaymentMode.values().forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPaymentModeChange(mode) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedPaymentMode == mode,
                                onClick = { onPaymentModeChange(mode) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = when (mode) {
                                    PaymentMode.CASH -> Icons.Default.Money
                                    PaymentMode.UPI -> Icons.Default.QrCodeScanner
                                },
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = mode.name,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

                // Transaction ID for UPI
                if (selectedPaymentMode == PaymentMode.UPI) {
//                    GMTextField(
//                        value = transactionId,
//                        onValueChange = onTransactionIdChange,
//                        label = "Transaction ID",
//                        placeholder = "Enter UPI transaction ID",
//                        leadingIcon = Icons.Default.Tag,
//                        enabled = !isProcessing
//                    )
//                        Image(
//                            painter = painterResource(R.drawable.ic_launcher_background),
//                            contentDescription = null,
//                            modifier = Modifier
//                                .size(200.dp)
//                                .clip(RoundedCornerShape(10))
//                                .border(5.dp, Color.Gray, RoundedCornerShape(10)),
//                            contentScale = ContentScale.Fit,
//
//                            )

                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isProcessing
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Confirm Payment")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isProcessing
            ) {
                Text("Cancel")
            }
        }
    )
}

// ===========================================================================
// Helper Functions
// ===========================================================================
private fun formatTimestamp(timestamp: Long): String {
    val format = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return format.format(Date(timestamp))
}

// Helper data class for tuple
private data class Tuple4<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

// ===========================================================================
// Navigation Setup - Add to AppNavigation.kt
// ===========================================================================
/*
import com.autogarage.presentation.ui.invoice.InvoiceScreen

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
*/