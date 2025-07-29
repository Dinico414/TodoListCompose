package com.xenon.todolist.ui.res

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.window.DialogProperties
import com.xenon.todolist.R
import com.xenon.todolist.ui.values.LargerPadding
import com.xenon.todolist.ui.values.MediumPadding
import com.xenon.todolist.viewmodel.FilterableAttribute
import com.xenon.todolist.viewmodel.FilterState

@Composable
fun DialogTaskItemFiltering(
    initialFilterStates: Map<FilterableAttribute, FilterState>,
    onDismissRequest: () -> Unit,
    onApplyFilters: (Map<FilterableAttribute, FilterState>) -> Unit,
    onResetFilters: () -> Unit,
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
        onDismissRequest = onDismissRequest,
        title = stringResource(R.string.filter_tasks_description),
        confirmButtonText = stringResource(R.string.ok),
        onConfirmButtonClick = {
            onApplyFilters(currentDialogFilterStates.toMap())
            onDismissRequest()
        },
        properties = DialogProperties(usePlatformDefaultWidth = true),
        actionButton2Text = stringResource(R.string.reset),
        onActionButton2Click = {
            onResetFilters()
            FilterableAttribute.entries.forEach { attribute ->
                currentDialogFilterStates[attribute] = FilterState.IGNORED
            }
        },
        contentManagesScrolling = false
    ) {
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
                Spacer(Modifier.height(MediumPadding / 2))
            }
        }
    }
}

@Composable
private fun TriStateFilterRow(
    attribute: FilterableAttribute,
    state: FilterState,
    onStateChange: (FilterState) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(100.0f))
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
            ),
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
        Spacer(Modifier.width(MediumPadding))

        Icon(
            imageVector = icon,
            contentDescription = "${attribute.toDisplayString()} filter is ${state.name.lowercase()}",
            tint = tint,
            modifier = Modifier.padding(vertical = MediumPadding)
        )
        Spacer(Modifier.width(LargerPadding))
        Text(
            text = attribute.toDisplayString(),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
