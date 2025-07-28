package com.xenon.todolist.ui.res

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope // XenonDialog content is ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.rememberScrollState // Not needed if XenonDialog handles scrolling
//import androidx.compose.foundation.verticalScroll     // Not needed if XenonDialog handles scrolling
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.IndeterminateCheckBox
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.xenon.todolist.R
import com.xenon.todolist.ui.values.LargerPadding
import com.xenon.todolist.ui.values.MediumPadding
import com.xenon.todolist.viewmodel.FilterableAttribute
import com.xenon.todolist.viewmodel.FilterState

// XenonDialog is now expected to be in its own file and imported.
// No need for a placeholder here.

@Composable
fun DialogTaskItemFiltering(
    initialFilterStates: Map<FilterableAttribute, FilterState>,
    onDismissRequest: () -> Unit, // This is for the 'X' icon or background click
    onApplyFilters: (Map<FilterableAttribute, FilterState>) -> Unit,
    onResetFilters: () -> Unit  // This is for the "Reset" TextButton
) {
    val currentDialogFilterStates = remember {
        mutableStateMapOf<FilterableAttribute, FilterState>().apply {
            putAll(initialFilterStates)
        }
    }

    LaunchedEffect(initialFilterStates) {
        currentDialogFilterStates.clear()
        currentDialogFilterStates.putAll(initialFilterStates)
    }

    XenonDialog(
        onDismissRequest = onDismissRequest, // For the 'X' icon & scrim
        title = stringResource(R.string.filter_tasks_description),

        // Confirm button (Apply)
        confirmButtonText = stringResource(R.string.ok),
        onConfirmButtonClick = {
            onApplyFilters(currentDialogFilterStates.toMap())
            onDismissRequest() // Explicitly call onDismissRequest to close dialog
        },

        // Action Button 1: Cancel
        actionButton1Text = stringResource(R.string.cancel),
        onActionButton1Click = {
            onDismissRequest() // Closes dialog without applying changes
        },

        // Action Button 2: Reset
        actionButton2Text = stringResource(R.string.reset),
        onActionButton2Click = {
            onResetFilters() // Tells ViewModel to reset its filters
            FilterableAttribute.entries.forEach { attribute -> // Reset dialog's internal state
                currentDialogFilterStates[attribute] = FilterState.IGNORED
            }
            // Dialog remains open for further changes or explicit apply/cancel via other buttons
        },
        contentManagesScrolling = false // Let XenonDialog's internal Column manage scrolling
    ) { // Content for XenonDialog (ColumnScope)
        // The Column for filter items is now directly the content of XenonDialog's scrollable area
        FilterableAttribute.entries.forEachIndexed { index, attribute ->
            val currentState = currentDialogFilterStates[attribute] ?: FilterState.IGNORED
            TriStateFilterRow(
                attribute = attribute,
                state = currentState,
                onStateChange = { newState ->
                    currentDialogFilterStates[attribute] = newState
                }
            )
            if (index < FilterableAttribute.entries.size - 1) {
                Spacer(Modifier.height(MediumPadding / 2)) // Optional spacer between rows
            }
        }
    }
}

@Composable
private fun TriStateFilterRow(
    attribute: FilterableAttribute,
    state: FilterState,
    onStateChange: (FilterState) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = state == FilterState.INCLUDED,
                role = Role.Checkbox,
                onValueChange = {
                    val nextState = when (state) {
                        FilterState.IGNORED -> FilterState.INCLUDED
                        FilterState.INCLUDED -> FilterState.EXCLUDED
                        FilterState.EXCLUDED -> FilterState.IGNORED
                    }
                    onStateChange(nextState)
                }
            )
            // XenonDialog's `contentPadding` will handle horizontal padding for the overall content block.
            // We only need vertical padding for the row itself if desired.
            .padding(vertical = MediumPadding), // Adjusted padding
        verticalAlignment = Alignment.CenterVertically
    ) {
        val icon = when (state) {
            FilterState.INCLUDED -> Icons.Filled.CheckBox
            FilterState.EXCLUDED -> Icons.Filled.CheckBoxOutlineBlank
            FilterState.IGNORED -> Icons.Filled.IndeterminateCheckBox
        }
        val tint = when (state) {
            FilterState.INCLUDED -> MaterialTheme.colorScheme.primary
            FilterState.EXCLUDED -> LocalContentColor.current.copy(alpha = 0.6f)
            FilterState.IGNORED -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        }

        Icon(
            imageVector = icon,
            contentDescription = "${attribute.toDisplayString()} filter is ${state.name.lowercase()}",
            tint = tint
        )
        Spacer(Modifier.width(LargerPadding)) // Space between icon and text
        Text(
            text = attribute.toDisplayString(),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
