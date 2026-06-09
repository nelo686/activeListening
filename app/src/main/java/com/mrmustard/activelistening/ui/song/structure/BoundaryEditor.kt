package com.mrmustard.activelistening.ui.song.structure

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mrmustard.activelistening.R

@Composable
fun BoundaryEditor(
    title: String,
    suggestedTimeMillis: Long,
    onChange: (Long) -> Unit,
) {
    var text by remember(suggestedTimeMillis, title) {
        mutableStateOf(formatSectionTime(suggestedTimeMillis))
    }

    LaunchedEffect(suggestedTimeMillis) {
        text = formatSectionTime(suggestedTimeMillis)
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        val parsedTime = parseSectionTime(text)
        val hasEditedValue = text != formatSectionTime(suggestedTimeMillis)
        val isInvalid = text.isNotBlank() && parsedTime == null
        OutlinedTextField(
            value = text,
            onValueChange = { newValue ->
                text = newValue
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = isInvalid,
            label = { Text(stringResource(R.string.structure_time_input_label)) },
            supportingText = {
                Text(
                    stringResource(
                        if (isInvalid) {
                            R.string.structure_time_input_error
                        } else {
                            R.string.structure_time_input_hint
                        },
                    ),
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = {
                        parsedTime?.let(onChange)
                    },
                    enabled = hasEditedValue && parsedTime != null,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_check_24),
                        contentDescription = stringResource(R.string.structure_time_apply),
                    )
                }
            },
        )
    }
}

private fun parseSectionTime(input: String): Long? {
    val trimmed = input.trim()
    if (trimmed.isBlank()) return null

    val parts = trimmed.split(":")
    if (parts.size != 2) return null

    val minutes = parts[0].toLongOrNull() ?: return null
    val seconds = parts[1].toLongOrNull() ?: return null
    if (minutes < 0L || seconds !in 0L..59L) return null
    return (minutes * 60_000L) + (seconds * 1_000L)
}
