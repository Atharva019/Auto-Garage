package com.autogarage.presentation.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val AppShapes = Shapes(
    // Small components - chips, badges
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),

    // Medium components - cards, dialogs
    medium = RoundedCornerShape(12.dp),

    // Large components - bottom sheets, large cards
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

// Custom shapes for specific components
object CustomShapes {
    val button = RoundedCornerShape(24.dp) // Fully rounded
    val buttonSmall = RoundedCornerShape(20.dp)
    val card = RoundedCornerShape(12.dp)
    val cardSmall = RoundedCornerShape(8.dp)
    val dialog = RoundedCornerShape(16.dp)
    val bottomSheet = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    val textField = RoundedCornerShape(8.dp)
    val badge = RoundedCornerShape(12.dp)
    val fab = RoundedCornerShape(16.dp)
}
