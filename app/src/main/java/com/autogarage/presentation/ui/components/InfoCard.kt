package com.autogarage.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.autogarage.presentation.ui.theme.Spacing
import com.autogarage.presentation.ui.theme.*

enum class InfoType {
    INFO, SUCCESS, WARNING, ERROR
}

@Composable
fun InfoCard(
    title: String,
    description: String,
    type: InfoType = InfoType.INFO,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    val (backgroundColor, iconColor) = when (type) {
        InfoType.INFO -> Pair(
            MaterialTheme.customColors.infoContainer,
            MaterialTheme.customColors.info
        )
        InfoType.SUCCESS -> Pair(
            MaterialTheme.customColors.successContainer,
            MaterialTheme.customColors.success
        )
        InfoType.WARNING -> Pair(
            MaterialTheme.customColors.warningContainer,
            MaterialTheme.customColors.warning
        )
        InfoType.ERROR -> Pair(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.error
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Spacing.cardCornerRadiusSmall),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
            horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = iconColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )

                if (actionText != null && onActionClick != null) {
                    Spacer(modifier = Modifier.height(Spacing.small))
                    TextButton(
                        onClick = onActionClick,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = actionText,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = iconColor
                        )
                    }
                }
            }
        }
    }
}