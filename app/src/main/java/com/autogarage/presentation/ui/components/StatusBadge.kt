package com.autogarage.presentation.ui.components


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.autogarage.presentation.ui.theme.*

enum class JobStatus {
    PENDING, IN_PROGRESS, COMPLETED, CANCELLED, DELIVERED
}

enum class StockStatus {
    IN_STOCK, LOW_STOCK, OUT_OF_STOCK
}

@Composable
fun StatusBadge(
    status: JobStatus,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor, statusText) = when (status) {
        JobStatus.PENDING -> Triple(
            MaterialTheme.customColors.warningContainer,
            MaterialTheme.customColors.onWarningContainer,
            "Pending"
        )
        JobStatus.IN_PROGRESS -> Triple(
            MaterialTheme.customColors.infoContainer,
            MaterialTheme.customColors.onInfoContainer,
            "In Progress"
        )
        JobStatus.COMPLETED -> Triple(
            MaterialTheme.customColors.successContainer,
            MaterialTheme.customColors.onSuccessContainer,
            "Completed"
        )
        JobStatus.CANCELLED -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            "Cancelled"
        )
        JobStatus.DELIVERED -> Triple(
            MaterialTheme.customColors.successContainer,
            MaterialTheme.customColors.onSuccessContainer,
            "Delivered"
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(Spacing.badgeCornerRadius))
            .background(backgroundColor)
            .padding(
                horizontal = Spacing.badgePaddingHorizontal,
                vertical = Spacing.badgePaddingVertical
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = statusText,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor,
            fontSize = 11.sp
        )
    }
}

@Composable
fun StockBadge(
    stockStatus: StockStatus,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor, statusText) = when (stockStatus) {
        StockStatus.IN_STOCK -> Triple(
            MaterialTheme.customColors.successContainer,
            MaterialTheme.customColors.inStock,
            "In Stock"
        )
        StockStatus.LOW_STOCK -> Triple(
            MaterialTheme.customColors.warningContainer,
            MaterialTheme.customColors.lowStock,
            "Low Stock"
        )
        StockStatus.OUT_OF_STOCK -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.customColors.outOfStock,
            "Out of Stock"
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(Spacing.badgeCornerRadius))
            .background(backgroundColor)
            .padding(
                horizontal = Spacing.badgePaddingHorizontal,
                vertical = Spacing.badgePaddingVertical
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = statusText,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}