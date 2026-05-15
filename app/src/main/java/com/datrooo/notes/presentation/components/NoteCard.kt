package com.datrooo.notes.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.datrooo.notes.R
import com.datrooo.notes.domain.model.Note
import com.datrooo.notes.domain.model.NoteContentBlock

@Composable
fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val previewText = note.content.filterIsInstance<NoteContentBlock.Text>()
        .firstOrNull { it.text.isNotBlank() }?.text ?: stringResource(R.string.empty_note)
    
    val totalChars = note.content.sumOf { (it as? NoteContentBlock.Text)?.text?.length ?: 0 }

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                ) {}
            }
            Text(
                text = note.title.ifBlank { stringResource(R.string.no_title) },
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 4.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = previewText,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 18.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (note.tags.isNotEmpty()) {
                val displayTags = note.tags.take(3)
                val remainingCount = note.tags.size - displayTags.size

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    displayTags.forEach { tag ->
                        AssistChip(
                            onClick = { },
                            label = { Text(text = "#$tag") },
                            colors = AssistChipDefaults.assistChipColors(
                                labelColor = MaterialTheme.colorScheme.primary
                            ),
                            border = null,
                            shape = MaterialTheme.shapes.small
                        )
                    }
                    if (remainingCount > 0) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = "+$remainingCount",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 18.dp, bottom = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.22f),
                    shape = MaterialTheme.shapes.large
                ) {
                    Text(
                        text = pluralStringResource(
                            id = R.plurals.char_count,
                            count = totalChars,
                            totalChars
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = note.updatedAt.formatForUi(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
