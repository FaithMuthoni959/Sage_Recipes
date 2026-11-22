package com.example.sagerecipes.ui.theme.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun AUthMessages(errorMessage: String?, infoMessage: String?) {
    val text = errorMessage ?: infoMessage ?: return
    val color = if (errorMessage != null) MaterialTheme.colorScheme.error
    else MaterialTheme.colorScheme.secondary
    Text(
        text = text,
        color = color,
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
    )
}
