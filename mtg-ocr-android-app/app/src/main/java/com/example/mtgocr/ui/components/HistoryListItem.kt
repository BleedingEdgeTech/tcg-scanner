package com.example.mtgocr.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mtgocr.domain.model.CardDetails

@Composable
fun HistoryListItem(cardDetails: CardDetails, onDelete: () -> Unit, onView: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = cardDetails.name, style = MaterialTheme.typography.titleMedium)
                Text(text = "Set: ${cardDetails.setCode}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Collector Number: ${cardDetails.collectorNumber}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Year: ${cardDetails.yearOfPrint}", style = MaterialTheme.typography.bodyMedium)
                // mask info
                Text(text = "Lang: ${cardDetails.language}", style = MaterialTheme.typography.bodySmall)
                Text(text = "Cond: ${cardDetails.condition}", style = MaterialTheme.typography.bodySmall)
                Row {
                    if (cardDetails.foil) {
                        Text(text = "Foil", style = MaterialTheme.typography.bodySmall)
                    }
                    if (cardDetails.signed) {
                        Text(text = "Signed", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            IconButton(onClick = onView) {
                Icon(imageVector = Icons.Default.Visibility, contentDescription = "View Card")
            }
            IconButton(onClick = onDelete) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Card")
            }
        }
    }
}