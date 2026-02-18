package com.autogarage.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.VisualTransformation
import com.autogarage.presentation.ui.theme.*

@Composable
fun GMTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    leadingIconPainter: Painter? = null,
    trailingIcon: ImageVector? = null,
    trailingIconPainter: Painter? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    supportingText: String? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = placeholder?.let { { Text(it) } },
            leadingIcon = if (leadingIcon != null) {
                {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(Spacing.iconMedium)
                    )
                }
            } else if (leadingIconPainter != null) {
                {
                    Icon(
                        painter = leadingIconPainter,
                        contentDescription = null,
                        modifier = Modifier.size(Spacing.iconMedium)
                    )
                }
            } else null,
            trailingIcon = if (trailingIcon != null) {
                {
                    IconButton(onClick = { onTrailingIconClick?.invoke() }) {
                        Icon(
                            imageVector = trailingIcon,
                            contentDescription = null,
                            modifier = Modifier.size(Spacing.iconMedium)
                        )
                    }
                }
            } else if (trailingIconPainter != null) {
                {
                    IconButton(onClick = { onTrailingIconClick?.invoke() }) {
                        Icon(
                            painter = trailingIconPainter,
                            contentDescription = null,
                            modifier = Modifier.size(Spacing.iconMedium)
                        )
                    }
                }
            } else null,
            supportingText = when {
                isError && errorMessage != null -> {
                    { Text(errorMessage) }
                }
                supportingText != null -> {
                    { Text(supportingText) }
                }
                else -> null
            },
            isError = isError,
            enabled = enabled,
            readOnly = readOnly,
            singleLine = singleLine,
            maxLines = maxLines,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            shape = CustomShapes.textField,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
