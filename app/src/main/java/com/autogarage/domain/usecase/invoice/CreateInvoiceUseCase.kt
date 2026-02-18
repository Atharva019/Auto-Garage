package com.autogarage.domain.usecase.invoice

import com.autogarage.domain.model.Invoice
import com.autogarage.domain.model.JobCard
import com.autogarage.domain.model.JobCardStatus
import com.autogarage.domain.model.PaymentStatus
import com.autogarage.domain.repository.CustomerRepository
import com.autogarage.domain.repository.InvoiceRepository
import com.autogarage.domain.repository.SettingsRepository
import com.autogarage.domain.usecase.base.UseCase
import javax.inject.Inject

class CreateInvoiceUseCase @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    private val settingsRepository: SettingsRepository,
    private val customerRepository: CustomerRepository // ADD THIS
) : UseCase<CreateInvoiceUseCase.Params, Long>() {

    data class Params(
        val jobCard: JobCard,
        val discount: Double = 0.0,
        val discountPercentage: Double = 0.0,
        val notes: String? = null,
        val termsAndConditions: String? = null
    )

    override suspend fun execute(params: Params): Long {
        val jobCard = params.jobCard

        // Validation
        require(jobCard.status == JobCardStatus.COMPLETED || jobCard.status == JobCardStatus.DELIVERED) {
            "Invoice can only be generated for completed or delivered job cards"
        }

        // Check if invoice already exists
        val existingInvoice = invoiceRepository.getInvoiceByJobCard(jobCard.id)
        if (existingInvoice != null) {
            throw IllegalStateException("Invoice already exists for this job card")
        }

        // Fetch customer from vehicle customerId
        val customer = customerRepository.getCustomerByIdSync(jobCard.vehicle.customerId)
            ?: throw IllegalStateException("Customer not found for vehicle")

        // Get tax rate from settings
        val taxRate = settingsRepository.getDefaultTaxRate()

        // Calculate costs
        val laborCost = jobCard.laborCost
        val partsCost = jobCard.partsCost
        val subtotal = laborCost + partsCost

        // Apply discount
        val discountAmount = if (params.discountPercentage > 0) {
            subtotal * (params.discountPercentage / 100)
        } else {
            params.discount
        }

        // Calculate taxable amount
        val taxableAmount = subtotal - discountAmount

        // Calculate tax
        val taxAmount = taxableAmount * (taxRate / 100)

        // Calculate total
        val totalAmount = taxableAmount + taxAmount

        // Generate invoice number
        val invoiceNumber = invoiceRepository.generateInvoiceNumber()

        // Get default terms and conditions from settings
        val defaultTerms = params.termsAndConditions ?: getDefaultTermsAndConditions()

        // Create invoice
        val invoice = Invoice(
            invoiceNumber = invoiceNumber,
            jobCard = jobCard,
            customer = customer, // ADD THIS LINE
            laborCost = laborCost,
            partsCost = partsCost,
            subtotal = subtotal,
            discount = discountAmount,
            discountPercentage = params.discountPercentage,
            taxableAmount = taxableAmount,
            taxRate = taxRate,
            taxAmount = taxAmount,
            totalAmount = totalAmount,
            paymentStatus = PaymentStatus.UNPAID,
            paidAmount = 0.0,
            pendingAmount = totalAmount,
            notes = params.notes,
            termsAndConditions = defaultTerms
        )

        return invoiceRepository.createInvoice(invoice)
    }

    private fun getDefaultTermsAndConditions(): String {
        return """
            1. Payment is due upon receipt of this invoice.
            2. All work is guaranteed for 30 days from the date of service.
            3. Parts warranty is as per manufacturer's terms.
            4. Late payment may incur additional charges.
            5. Disputes must be raised within 7 days of invoice date.
        """.trimIndent()
    }
}