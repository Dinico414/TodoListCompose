package com.xenon.todolist.ui.res

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.xenon.todolist.R

data class LanguageOption(val displayName: String, val localeTag: String)

@Composable
fun DialogLanguageSelection(
    availableLanguages: List<LanguageOption>,
    currentLanguageTag: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    var selectedOption by remember(currentLanguageTag) {
        mutableStateOf(
            availableLanguages.firstOrNull { it.localeTag == currentLanguageTag }
                ?: availableLanguages.first()
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.language_dialog_title),
            )
        },
        text = {
            val listState = rememberLazyListState()
            var canScroll by remember { mutableStateOf(false) }

            LaunchedEffect(remember { derivedStateOf { listState.layoutInfo } }) {
                canScroll = listState.canScrollForward || listState.canScrollBackward
            }

            Box {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.selectableGroup()
                ) {
                    items(availableLanguages) { languageOption ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .selectable(
                                    selected = (languageOption == selectedOption),
                                    onClick = {
                                        selectedOption = languageOption
                                        onLanguageSelected(languageOption.localeTag)
                                    },
                                    role = Role.RadioButton
                                )
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (languageOption == selectedOption),
                                onClick = null
                            )
                            Text(
                                text = languageOption.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                }
                if (canScroll) {
                    HorizontalDivider(modifier = Modifier.align(Alignment.TopCenter))
                    HorizontalDivider(modifier = Modifier.align(Alignment.BottomCenter))
                }
            }
        },
        confirmButton = {
            FilledTonalButton(onClick = {
                onConfirm()
                onDismiss()
            }) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}