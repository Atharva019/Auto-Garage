package com.autogarage.domain.usecase.invoice

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.autogarage.domain.model.Invoice
import com.autogarage.domain.repository.InvoiceRepository
import com.autogarage.domain.repository.SettingsRepository
import com.autogarage.domain.usecase.base.UseCase
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.borders.SolidBorder
import com.itextpdf.layout.element.*
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.itextpdf.layout.properties.VerticalAlignment
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class GenerateInvoicePdfUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val invoiceRepository: InvoiceRepository,
    private val settingsRepository: SettingsRepository
) : UseCase<Long, File>() {

    override suspend fun execute(params: Long): File {
        // Fetch invoice with complete details
        val invoice = invoiceRepository.getInvoiceByIdSync(params)
            ?: throw IllegalStateException("Invoice not found")

        // ✅ DEBUG: Log what we got
        println("DEBUG: Invoice ID: ${invoice.id}")
        println("DEBUG: JobCard ID: ${invoice.jobCard.id}")
        println("DEBUG: Services count: ${invoice.jobCard.services.size}")
        println("DEBUG: Parts count: ${invoice.jobCard.parts.size}")

        invoice.jobCard.services.forEach { service ->
            println("DEBUG: Service: ${service.serviceName} - ₹${service.totalCost}")
        }

        invoice.jobCard.parts.forEach { part ->
            println("DEBUG: Part: ${part.partName} (${part.partNumber}) - Qty: ${part.quantity}")
        }

        // Get business settings - use default values if methods don't exist
        val businessName = try {
            settingsRepository.getBusinessName()
        } catch (e: Exception) {
            "Auto Garage" // Default name
        }

        val businessAddress = try {
            settingsRepository.getBusinessAddress()
        } catch (e: Exception) {
            null
        }

        val businessPhone = try {
            settingsRepository.getBusinessPhone()
        } catch (e: Exception) {
            null
        }

        val businessEmail = try {
            settingsRepository.getBusinessEmail()
        } catch (e: Exception) {
            null
        }

        val businessGST = try {
            settingsRepository.getGstNumber()
        } catch (e: Exception) {
            null
        }

        val logoPath: String? = null // Logo feature can be added later

        // Create PDF file
        val fileName = "Invoice_${invoice.invoiceNumber}_${System.currentTimeMillis()}.pdf"
        val file = File(context.getExternalFilesDir(null), "Invoices/$fileName")
        file.parentFile?.mkdirs()

        // Generate PDF
        val pdfWriter = PdfWriter(file)
        val pdfDocument = PdfDocument(pdfWriter)
        val document = Document(pdfDocument, PageSize.A4)
        document.setMargins(40f, 40f, 40f, 40f)

        // Build PDF content
        addHeader(document, businessName, businessAddress, businessPhone, businessEmail, businessGST, logoPath)
        addInvoiceInfo(document, invoice)
        addCustomerVehicleInfo(document, invoice)
        addServicesTable(document, invoice)
        addPartsTable(document, invoice)
        addCostSummary(document, invoice)
        addPaymentInfo(document, invoice)
        addTermsAndConditions(document, invoice)
        addFooter(document, businessName)

        document.close()

        return file
    }

    private fun addHeader(
        document: Document,
        businessName: String,
        businessAddress: String?,
        businessPhone: String?,
        businessEmail: String?,
        businessGST: String?,
        logoPath: String?
    ) {
        val headerTable = Table(UnitValue.createPercentArray(floatArrayOf(30f, 70f)))
            .useAllAvailableWidth()

        // Logo section (left)
        val logoCell = Cell()
            .setBorder(Border.NO_BORDER)
            .setVerticalAlignment(VerticalAlignment.MIDDLE)

        logoPath?.let {
            try {
                val logoFile = File(it)
                if (logoFile.exists()) {
                    val imageData = ImageDataFactory.create(it)
                    val logo = Image(imageData)
                        .setWidth(100f)
                        .setHeight(100f)
                    logoCell.add(logo)
                }
            } catch (e: Exception) {
                // Logo loading failed, skip
            }
        }

        headerTable.addCell(logoCell)

        // Business info section (right)
        val businessInfoCell = Cell()
            .setBorder(Border.NO_BORDER)
            .setTextAlignment(TextAlignment.RIGHT)

        businessInfoCell.add(
            Paragraph(businessName)
                .setFontSize(20f)
                .setBold()
                .setMarginBottom(5f)
        )

        businessAddress?.let {
            businessInfoCell.add(
                Paragraph(it)
                    .setFontSize(10f)
                    .setMarginBottom(2f)
            )
        }

        businessPhone?.let {
            businessInfoCell.add(
                Paragraph("Phone: $it")
                    .setFontSize(10f)
                    .setMarginBottom(2f)
            )
        }

        businessEmail?.let {
            businessInfoCell.add(
                Paragraph("Email: $it")
                    .setFontSize(10f)
                    .setMarginBottom(2f)
            )
        }

        businessGST?.let {
            businessInfoCell.add(
                Paragraph("GSTIN: $it")
                    .setFontSize(10f)
                    .setBold()
            )
        }

        headerTable.addCell(businessInfoCell)
        document.add(headerTable)

        // Invoice title
        document.add(
            Paragraph("TAX INVOICE")
                .setFontSize(24f)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(20f)
                .setMarginBottom(20f)
                .setFontColor(DeviceRgb(41, 128, 185))
        )
    }

    private fun addInvoiceInfo(document: Document, invoice: Invoice) {
        val infoTable = Table(UnitValue.createPercentArray(floatArrayOf(50f, 50f)))
            .useAllAvailableWidth()
            .setMarginBottom(20f)

        // Left column - Invoice details
        val leftCell = Cell()
            .setBorder(SolidBorder(ColorConstants.LIGHT_GRAY, 1f))
            .setPadding(10f)

        leftCell.add(
            Paragraph("Invoice Number: ${invoice.invoiceNumber}")
                .setFontSize(12f)
                .setBold()
                .setMarginBottom(5f)
        )

        leftCell.add(
            Paragraph("Invoice Date: ${formatDate(invoice.invoiceDate)}")
                .setFontSize(10f)
                .setMarginBottom(5f)
        )

        leftCell.add(
            Paragraph("Job Card: ${invoice.jobCard.jobCardNumber}")
                .setFontSize(10f)
                .setMarginBottom(5f)
        )

        leftCell.add(
            Paragraph("Status: ${invoice.paymentStatus.name}")
                .setFontSize(10f)
                .setBold()
                .setFontColor(
                    when (invoice.paymentStatus.name) {
                        "PAID" -> DeviceRgb(39, 174, 96)
                        "UNPAID" -> DeviceRgb(231, 76, 60)
                        else -> DeviceRgb(149, 165, 166)
                    }
                )
        )

        infoTable.addCell(leftCell)

        // Right column - Customer details
        val rightCell = Cell()
            .setBorder(SolidBorder(ColorConstants.LIGHT_GRAY, 1f))
            .setPadding(10f)

        val customer = invoice.customer

        rightCell.add(
            Paragraph("Bill To:")
                .setFontSize(10f)
                .setBold()
                .setMarginBottom(5f)
        )

        rightCell.add(
            Paragraph(customer.name)
                .setFontSize(12f)
                .setBold()
                .setMarginBottom(5f)
        )

        rightCell.add(
            Paragraph("Phone: ${customer.phone}")
                .setFontSize(10f)
                .setMarginBottom(3f)
        )

        customer.email?.let {
            rightCell.add(
                Paragraph("Email: $it")
                    .setFontSize(10f)
                    .setMarginBottom(3f)
            )
        }

        customer.address?.let {
            rightCell.add(
                Paragraph("Address: $it")
                    .setFontSize(10f)
            )
        }

        infoTable.addCell(rightCell)
        document.add(infoTable)
    }

    private fun addCustomerVehicleInfo(document: Document, invoice: Invoice) {
        val vehicle = invoice.jobCard.vehicle

        val vehicleTable = Table(UnitValue.createPercentArray(floatArrayOf(100f)))
            .useAllAvailableWidth()
            .setMarginBottom(20f)

        val vehicleCell = Cell()
            .setBackgroundColor(DeviceRgb(236, 240, 241))
            .setBorder(SolidBorder(ColorConstants.LIGHT_GRAY, 1f))
            .setPadding(10f)

        vehicleCell.add(
            Paragraph("Vehicle Details")
                .setFontSize(12f)
                .setBold()
                .setMarginBottom(5f)
        )

        val detailsText = """
            Registration Number: ${vehicle.registrationNumber} | 
            Make & Model: ${vehicle.make} ${vehicle.model} | 
            Year: ${vehicle.year} | 
            VIN: ${vehicle.registrationNumber ?: "N/A"}
        """.trimIndent()

        vehicleCell.add(
            Paragraph(detailsText)
                .setFontSize(10f)
        )

        vehicleTable.addCell(vehicleCell)
        document.add(vehicleTable)
    }

    private fun addServicesTable(document: Document, invoice: Invoice) {
        if (invoice.jobCard.services.isEmpty()) return

        document.add(
            Paragraph("Services Provided")
                .setFontSize(14f)
                .setBold()
                .setMarginBottom(10f)
        )

        val servicesTable = Table(UnitValue.createPercentArray(floatArrayOf(10f, 40f, 15f, 15f, 20f)))
            .useAllAvailableWidth()
            .setMarginBottom(20f)

        // Header
        val headerColor = DeviceRgb(52, 73, 94)
        listOf("S.No", "Service", "Quantity", "Rate", "Amount").forEach { header ->
            servicesTable.addHeaderCell(
                Cell()
                    .add(Paragraph(header).setFontColor(ColorConstants.WHITE).setBold())
                    .setBackgroundColor(headerColor)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(8f)
            )
        }

        // Data rows
        invoice.jobCard.services.forEachIndexed { index, service ->
            servicesTable.addCell(
                Cell().add(Paragraph("${index + 1}"))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(5f)
            )
            servicesTable.addCell(
                Cell().add(Paragraph(service.serviceName))
                    .setPadding(5f)
            )
            servicesTable.addCell(
                Cell().add(Paragraph("${service.quantity}"))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(5f)
            )
            servicesTable.addCell(
                Cell().add(Paragraph("₹${String.format("%.2f", service.laborCost)}"))
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setPadding(5f)
            )
            servicesTable.addCell(
                Cell().add(Paragraph("₹${String.format("%.2f", service.totalCost)}"))
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setPadding(5f)
                    .setBold()
            )
        }

        document.add(servicesTable)
    }

    private fun addPartsTable(document: Document, invoice: Invoice) {
        if (invoice.jobCard.parts.isEmpty()) return

        document.add(
            Paragraph("Parts Used")
                .setFontSize(14f)
                .setBold()
                .setMarginBottom(10f)
        )

        val partsTable = Table(UnitValue.createPercentArray(floatArrayOf(10f, 30f, 20f, 10f, 15f, 15f)))
            .useAllAvailableWidth()
            .setMarginBottom(20f)

        // Header
        val headerColor = DeviceRgb(52, 73, 94)
        listOf("S.No", "Part Name", "Part Number", "Qty", "Unit Price", "Amount").forEach { header ->
            partsTable.addHeaderCell(
                Cell()
                    .add(Paragraph(header).setFontColor(ColorConstants.WHITE).setBold())
                    .setBackgroundColor(headerColor)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(8f)
            )
        }

        // Data rows
        invoice.jobCard.parts.forEachIndexed { index, part ->
            partsTable.addCell(
                Cell().add(Paragraph("${index + 1}"))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(5f)
            )
            partsTable.addCell(
                Cell().add(Paragraph(part.partName))
                    .setPadding(5f)
            )
            partsTable.addCell(
                Cell().add(Paragraph(part.partNumber))
                    .setPadding(5f)
            )
            partsTable.addCell(
                Cell().add(Paragraph("${part.quantity}"))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(5f)
            )
            partsTable.addCell(
                Cell().add(Paragraph("₹${String.format("%.2f", part.unitPrice)}"))
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setPadding(5f)
            )
            partsTable.addCell(
                Cell().add(Paragraph("₹${String.format("%.2f", part.totalCost)}"))
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setPadding(5f)
                    .setBold()
            )
        }

        document.add(partsTable)
    }

    private fun addCostSummary(document: Document, invoice: Invoice) {
        val summaryTable = Table(UnitValue.createPercentArray(floatArrayOf(70f, 30f)))
            .useAllAvailableWidth()
            .setMarginTop(20f)
            .setMarginBottom(20f)

        // Labor Cost
        addSummaryRow(summaryTable, "Labor Cost", invoice.laborCost)

        // Parts Cost
        addSummaryRow(summaryTable, "Parts Cost", invoice.partsCost)

        // Subtotal
        summaryTable.addCell(
            Cell()
                .add(Paragraph("Subtotal").setBold())
                .setBorder(Border.NO_BORDER)
                .setBorderTop(SolidBorder(ColorConstants.GRAY, 1f))
                .setPadding(5f)
        )
        summaryTable.addCell(
            Cell()
                .add(Paragraph("₹${String.format("%.2f", invoice.subtotal)}").setBold())
                .setTextAlignment(TextAlignment.RIGHT)
                .setBorder(Border.NO_BORDER)
                .setBorderTop(SolidBorder(ColorConstants.GRAY, 1f))
                .setPadding(5f)
        )

        // Discount
        if (invoice.discount > 0) {
            val discountLabel = if (invoice.discountPercentage > 0) {
                "Discount (${invoice.discountPercentage}%)"
            } else {
                "Discount"
            }
            summaryTable.addCell(
                Cell()
                    .add(Paragraph(discountLabel))
                    .setBorder(Border.NO_BORDER)
                    .setPadding(5f)
            )
            summaryTable.addCell(
                Cell()
                    .add(Paragraph("-₹${String.format("%.2f", invoice.discount)}"))
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setBorder(Border.NO_BORDER)
                    .setFontColor(DeviceRgb(231, 76, 60))
                    .setPadding(5f)
            )
        }

        // Taxable Amount
        addSummaryRow(summaryTable, "Taxable Amount", invoice.taxableAmount, bold = true)

        // GST
        addSummaryRow(summaryTable, "GST (${invoice.taxRate}%)", invoice.taxAmount)

        // Total Amount
        summaryTable.addCell(
            Cell()
                .add(Paragraph("Total Amount").setFontSize(14f).setBold())
                .setBackgroundColor(DeviceRgb(52, 73, 94))
                .setFontColor(ColorConstants.WHITE)
                .setPadding(10f)
                .setBorder(Border.NO_BORDER)
        )
        summaryTable.addCell(
            Cell()
                .add(Paragraph("₹${String.format("%.2f", invoice.totalAmount)}").setFontSize(14f).setBold())
                .setTextAlignment(TextAlignment.RIGHT)
                .setBackgroundColor(DeviceRgb(52, 73, 94))
                .setFontColor(ColorConstants.WHITE)
                .setPadding(10f)
                .setBorder(Border.NO_BORDER)
        )

        document.add(summaryTable)

        // Amount in words
        document.add(
            Paragraph("Amount in Words: ${convertToWords(invoice.totalAmount)}")
                .setFontSize(10f)
                .setItalic()
                .setMarginBottom(20f)
        )
    }

    private fun addSummaryRow(table: Table, label: String, amount: Double, bold: Boolean = false) {
        val labelParagraph = Paragraph(label)
        val amountParagraph = Paragraph("₹${String.format("%.2f", amount)}")

        if (bold) {
            labelParagraph.setBold()
            amountParagraph.setBold()
        }

        table.addCell(
            Cell()
                .add(labelParagraph)
                .setBorder(Border.NO_BORDER)
                .setPadding(5f)
        )
        table.addCell(
            Cell()
                .add(amountParagraph)
                .setTextAlignment(TextAlignment.RIGHT)
                .setBorder(Border.NO_BORDER)
                .setPadding(5f)
        )
    }

    private fun addPaymentInfo(document: Document, invoice: Invoice) {
        if (invoice.paymentStatus.name == "PAID") {
            val paymentTable = Table(UnitValue.createPercentArray(floatArrayOf(100f)))
                .useAllAvailableWidth()
                .setMarginBottom(20f)

            val paymentCell = Cell()
                .setBackgroundColor(DeviceRgb(212, 239, 223))
                .setBorder(SolidBorder(DeviceRgb(39, 174, 96), 2f))
                .setPadding(15f)

            paymentCell.add(
                Paragraph("✓ PAYMENT RECEIVED")
                    .setFontSize(14f)
                    .setBold()
                    .setFontColor(DeviceRgb(39, 174, 96))
                    .setMarginBottom(10f)
            )

            invoice.paymentMode?.let {
                paymentCell.add(
                    Paragraph("Payment Mode: ${it.name}")
                        .setFontSize(10f)
                        .setMarginBottom(3f)
                )
            }

            invoice.paymentDate?.let {
                paymentCell.add(
                    Paragraph("Payment Date: ${formatDate(it)}")
                        .setFontSize(10f)
                        .setMarginBottom(3f)
                )
            }

            invoice.transactionId?.let {
                paymentCell.add(
                    Paragraph("Transaction ID: $it")
                        .setFontSize(10f)
                )
            }

            paymentTable.addCell(paymentCell)
            document.add(paymentTable)
        }
    }

    private fun addTermsAndConditions(document: Document, invoice: Invoice) {
        invoice.termsAndConditions?.let { terms ->
            document.add(
                Paragraph("Terms & Conditions")
                    .setFontSize(12f)
                    .setBold()
                    .setMarginTop(20f)
                    .setMarginBottom(10f)
            )

            document.add(
                Paragraph(terms)
                    .setFontSize(9f)
                    .setMarginBottom(20f)
            )
        }

        invoice.notes?.let { notes ->
            document.add(
                Paragraph("Notes:")
                    .setFontSize(10f)
                    .setBold()
                    .setMarginBottom(5f)
            )

            document.add(
                Paragraph(notes)
                    .setFontSize(9f)
                    .setItalic()
                    .setMarginBottom(20f)
            )
        }
    }

    private fun addFooter(document: Document, businessName: String) {
        document.add(
            Paragraph("Thank you for your business!")
                .setFontSize(12f)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(30f)
                .setMarginBottom(10f)
                .setFontColor(DeviceRgb(41, 128, 185))
        )

        document.add(
            Paragraph("This is a computer-generated invoice and does not require a signature.")
                .setFontSize(8f)
                .setTextAlignment(TextAlignment.CENTER)
                .setItalic()
                .setFontColor(ColorConstants.GRAY)
        )
    }

    private fun formatDate(timestamp: Long): String {
        val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return format.format(Date(timestamp))
    }

    private fun convertToWords(amount: Double): String {
        // Simple implementation - you can enhance this
        val rupees = amount.toInt()
        val paise = ((amount - rupees) * 100).toInt()

        val ones = arrayOf(
            "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
            "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen",
            "Seventeen", "Eighteen", "Nineteen"
        )

        val tens = arrayOf(
            "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
        )

        fun convertLessThanThousand(num: Int): String {
            return when {
                num == 0 -> ""
                num < 20 -> ones[num]
                num < 100 -> "${tens[num / 10]} ${ones[num % 10]}".trim()
                else -> "${ones[num / 100]} Hundred ${convertLessThanThousand(num % 100)}".trim()
            }
        }

        val result = when {
            rupees == 0 -> "Zero"
            rupees < 1000 -> convertLessThanThousand(rupees)
            rupees < 100000 -> {
                val thousands = rupees / 1000
                val remainder = rupees % 1000
                "${convertLessThanThousand(thousands)} Thousand ${convertLessThanThousand(remainder)}".trim()
            }
            else -> {
                val lakhs = rupees / 100000
                val remainder = rupees % 100000
                val thousands = remainder / 1000
                val hundreds = remainder % 1000
                buildString {
                    append("${convertLessThanThousand(lakhs)} Lakh")
                    if (thousands > 0) append(" ${convertLessThanThousand(thousands)} Thousand")
                    if (hundreds > 0) append(" ${convertLessThanThousand(hundreds)}")
                }.trim()
            }
        }

        return if (paise > 0) {
            "$result Rupees and ${convertLessThanThousand(paise)} Paise Only"
        } else {
            "$result Rupees Only"
        }
    }
}