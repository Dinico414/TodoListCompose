package com.xenon.todolist.ui.res

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.xenon.todolist.R
import com.xenon.todolist.ui.values.LargerPadding
import com.xenon.todolist.ui.values.LargestPadding
import com.xenon.todolist.viewmodel.FilterState
import com.xenon.todolist.viewmodel.FilterableAttribute

enum class FilterDialogMode {
    APPLY_AS_INCLUDED,
    APPLY_AS_EXCLUDED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogTaskItemFiltering(
    initialFilterStates: Map<FilterableAttribute, FilterState>,
    onDismissRequest: () -> Unit,
    onApplyFilters: (Map<FilterableAttribute, FilterState>) -> Unit,
    onResetFilters: () -> Unit,
) {
    val checkedAttributesInDialog = remember {
        mutableStateMapOf<FilterableAttribute, Boolean>()
    }
    var currentFilterDialogMode by remember { mutableStateOf(FilterDialogMode.APPLY_AS_INCLUDED) }

    LaunchedEffect(initialFilterStates) {
        checkedAttributesInDialog.clear()
        var foundActiveExcludeFilter = false
        initialFilterStates.forEach { (attribute, state) ->
            if (state == FilterState.INCLUDED) {
                checkedAttributesInDialog[attribute] = true
            } else if (state == FilterState.EXCLUDED) {
                checkedAttributesInDialog[attribute] = true
                foundActiveExcludeFilter = true
            }
        }
        currentFilterDialogMode = if (foundActiveExcludeFilter) {
            FilterDialogMode.APPLY_AS_EXCLUDED
        } else {
            FilterDialogMode.APPLY_AS_INCLUDED
        }
    }

    XenonDialog(
        onDismissRequest = onDismissRequest,
        title = stringResource(R.string.filter_tasks_description),
        confirmButtonText = stringResource(R.string.ok),
        onConfirmButtonClick = {
            val filtersToApply = mutableMapOf<FilterableAttribute, FilterState>()
            val targetState = if (currentFilterDialogMode == FilterDialogMode.APPLY_AS_INCLUDED) {
                FilterState.INCLUDED
            } else {
                FilterState.EXCLUDED
            }
            checkedAttributesInDialog.filterValues { it }.keys.forEach { attribute ->
                filtersToApply[attribute] = targetState
            }
            onApplyFilters(filtersToApply)
            onDismissRequest()
        },
        actionButton2Text = stringResource(R.string.reset),
        onActionButton2Click = {
            onResetFilters()
            checkedAttributesInDialog.clear()
            currentFilterDialogMode = FilterDialogMode.APPLY_AS_INCLUDED
        },
        properties = DialogProperties(usePlatformDefaultWidth = true),
        contentManagesScrolling = false
    ) {
        Column {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                val modes = FilterDialogMode.entries
                modes.forEachIndexed { i, mode ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = i, count = modes.size
                        ),
                        onClick = { currentFilterDialogMode = mode },
                        selected = currentFilterDialogMode == mode,
                        icon = {
                            when (mode) {
                                FilterDialogMode.APPLY_AS_INCLUDED -> Icon(
                                    Icons.Filled.FilterAlt,
                                    contentDescription = stringResource(R.string.include)
                                )

                                FilterDialogMode.APPLY_AS_EXCLUDED -> Icon(
                                    Icons.Filled.FilterAltOff,
                                    contentDescription = stringResource(R.string.exclude)
                                )
                            }
                        }
                    ) {
                        Text(
                            text = when (mode) {
                                FilterDialogMode.APPLY_AS_INCLUDED -> stringResource(R.string.include)
                                FilterDialogMode.APPLY_AS_EXCLUDED -> stringResource(R.string.exclude)
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(LargestPadding))

            Column {
                FilterableAttribute.entries.forEach { attribute ->
                    val isChecked = checkedAttributesInDialog[attribute] ?: false
                    val toggleAction = {
                        if (!isChecked) {
                            checkedAttributesInDialog[attribute] = true
                        } else {
                            checkedAttributesInDialog.remove(attribute)
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(100.dp))
                            .toggleable(
                                value = isChecked,
                                role = Role.Checkbox,
                                onValueChange = { toggleAction() }
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { toggleAction() },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary,
                                uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                checkmarkColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                        Spacer(Modifier.width(LargerPadding))
                        Text(
                            text = attribute.toDisplayString(),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}