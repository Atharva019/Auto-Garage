package com.autogarage.domain.usecase.invoice

import com.autogarage.domain.repository.SettingsRepository
import com.autogarage.domain.usecase.base.UseCase
import javax.inject.Inject

data class InvoiceCalculation(
    val laborCost: Double,
    val partsCost: Double,
    val subtotal: Double,
    val discount: Double,
    val taxableAmount: Double,
    val taxRate: Double,
    val taxAmount: Double,
    val totalAmount: Double
)

class CalculateInvoiceAmountUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) : UseCase<CalculateInvoiceAmountUseCase.Params, InvoiceCalculation>() {

    data class Params(
        val laborCost: Double,
        val partsCost: Double,
        val discount: Double = 0.0,
        val discountPercentage: Double = 0.0
    )

    override suspend fun execute(params: Params): InvoiceCalculation {
        val subtotal = params.laborCost + params.partsCost

        val discountAmount = if (params.discountPercentage > 0) {
            subtotal * (params.discountPercentage / 100)
        } else {
            params.discount
        }

        val taxableAmount = subtotal - discountAmount
        val taxRate = settingsRepository.getDefaultTaxRate()
        val taxAmount = taxableAmount * (taxRate / 100)
        val totalAmount = taxableAmount + taxAmount

        return InvoiceCalculation(
            laborCost = params.laborCost,
            partsCost = params.partsCost,
            subtotal = subtotal,
            discount = discountAmount,
            taxableAmount = taxableAmount,
            taxRate = taxRate,
            taxAmount = taxAmount,
            totalAmount = totalAmount
        )
    }
}
