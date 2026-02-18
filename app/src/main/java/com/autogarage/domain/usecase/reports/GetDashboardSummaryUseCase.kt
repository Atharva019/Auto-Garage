package com.autogarage.domain.usecase.reports

import android.util.Log
import com.autogarage.domain.usecase.base.UseCase
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

data class DashboardSummary(
    val todayRevenue: Double,
    val monthRevenue: Double,
    val pendingInvoices: Int,
    val pendingJobCards: Int,
    val activeJobCards: Int,
    val lowStockItems: Int,
    val newCustomersThisMonth: Int,
    val revenueGrowth: Double
)

class GetDashboardSummaryUseCase @Inject constructor(
    private val getRevenueReportUseCase: GetRevenueReportUseCase,
    private val getJobCardStatsUseCase: GetJobCardStatsUseCase,
    private val getCustomerStatsUseCase: GetCustomerStatsUseCase,
    private val getInventoryStatsUseCase: GetInventoryStatsUseCase
) : UseCase<Unit, DashboardSummary>() {

    override val dispatcher = Dispatchers.IO

    override suspend fun execute(params: Unit): DashboardSummary {
        Log.d("GetDashboardSummary", "Starting dashboard summary calculation...")

        val now = System.currentTimeMillis()
        val todayStart = getTodayStart()
        val monthStart = getMonthStart()
        val lastMonthStart = getLastMonthStart()
        val lastMonthEnd = monthStart - 1

        Log.d("GetDashboardSummary",
            "Date ranges - Today: $todayStart, Month: $monthStart, LastMonth: $lastMonthStart")

        // ✅ Today's revenue
        val todayReport = try {
            getRevenueReportUseCase(
                GetRevenueReportUseCase.Params(todayStart, now)
            ).getOrThrow()
        } catch (e: Exception) {
            Log.e("GetDashboardSummary", "Failed to get today's revenue", e)
            // Return empty report instead of crashing
            RevenueReport(
                totalRevenue = 0.0,
                paidAmount = 0.0,
                pendingAmount = 0.0,
                totalInvoices = 0,
                paidInvoices = 0,
                unpaidInvoices = 0,
                averageInvoiceValue = 0.0,
                paymentModeBreakdown = emptyMap(),
                dailyRevenue = emptyList()
            )
        }

        // ✅ This month's revenue
        val monthReport = try {
            getRevenueReportUseCase(
                GetRevenueReportUseCase.Params(monthStart, now)
            ).getOrThrow()
        } catch (e: Exception) {
            Log.e("GetDashboardSummary", "Failed to get month's revenue", e)
            RevenueReport(
                totalRevenue = 0.0,
                paidAmount = 0.0,
                pendingAmount = 0.0,
                totalInvoices = 0,
                paidInvoices = 0,
                unpaidInvoices = 0,
                averageInvoiceValue = 0.0,
                paymentModeBreakdown = emptyMap(),
                dailyRevenue = emptyList()
            )
        }

        // ✅ Last month's revenue for growth calculation
        val lastMonthReport = try {
            getRevenueReportUseCase(
                GetRevenueReportUseCase.Params(lastMonthStart, lastMonthEnd)
            ).getOrThrow()
        } catch (e: Exception) {
            Log.e("GetDashboardSummary", "Failed to get last month's revenue", e)
            RevenueReport(
                totalRevenue = 0.0,
                paidAmount = 0.0,
                pendingAmount = 0.0,
                totalInvoices = 0,
                paidInvoices = 0,
                unpaidInvoices = 0,
                averageInvoiceValue = 0.0,
                paymentModeBreakdown = emptyMap(),
                dailyRevenue = emptyList()
            )
        }

        // ✅ Job card stats
        val jobCardStats = try {
            getJobCardStatsUseCase(
                GetJobCardStatsUseCase.Params(0, now)
            ).getOrThrow()
        } catch (e: Exception) {
            Log.e("GetDashboardSummary", "Failed to get job card stats", e)
            JobCardStats(
                totalJobCards = 0,
                pendingJobCards = 0,
                inProgressJobCards = 0,
                completedJobCards = 0,
                deliveredJobCards = 0,
                cancelledJobCards = 0,
                averageCompletionTime = 0.0,
                statusBreakdown = emptyMap(),
                priorityBreakdown = emptyMap(),
                technicianWorkload = emptyMap()
            )
        }

        // ✅ Customer stats
        val customerStats = try {
            getCustomerStatsUseCase(
                GetCustomerStatsUseCase.Params(monthStart, now)
            ).getOrThrow()
        } catch (e: Exception) {
            Log.e("GetDashboardSummary", "Failed to get customer stats", e)
            CustomerStats(
                totalCustomers = 0,
                newCustomers = 0,
                activeCustomers = 0,
                topCustomers = emptyList(),
                customerRetentionRate = 0.0,
                averageCustomerValue = 0.0,
                totalLoyaltyPoints = 0
            )
        }

        // ✅ Inventory stats
        val inventoryStats = try {
            getInventoryStatsUseCase(
                GetInventoryStatsUseCase.Params(0, now)
            ).getOrThrow()
        } catch (e: Exception) {
            Log.e("GetDashboardSummary", "Failed to get inventory stats", e)
            InventoryStats(
                totalItems = 0,
                inStockItems = 0,
                lowStockItems = 0,
                outOfStockItems = 0,
                totalInventoryValue = 0.0,
                topUsedParts = emptyList(),
                stockAlerts = emptyList()
            )
        }

        // ✅ Calculate revenue growth
        val revenueGrowth = if (lastMonthReport.totalRevenue > 0) {
            ((monthReport.totalRevenue - lastMonthReport.totalRevenue) /
                    lastMonthReport.totalRevenue) * 100
        } else if (monthReport.totalRevenue > 0) {
            100.0 // 100% growth if there was no revenue last month but have revenue now
        } else {
            0.0
        }

        val summary = DashboardSummary(
            todayRevenue = todayReport.totalRevenue,
            monthRevenue = monthReport.totalRevenue,
            pendingInvoices = monthReport.unpaidInvoices,
            pendingJobCards = jobCardStats.pendingJobCards,
            activeJobCards = jobCardStats.inProgressJobCards,
            lowStockItems = inventoryStats.lowStockItems + inventoryStats.outOfStockItems,
            newCustomersThisMonth = customerStats.newCustomers,
            revenueGrowth = revenueGrowth
        )

        Log.d("GetDashboardSummary",
            "Summary calculated - Today: ${summary.todayRevenue}, " +
                    "Month: ${summary.monthRevenue}, " +
                    "Growth: ${summary.revenueGrowth}%")

        return summary
    }

    private fun getTodayStart(): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getMonthStart(): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getLastMonthStart(): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.MONTH, -1)
        calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
