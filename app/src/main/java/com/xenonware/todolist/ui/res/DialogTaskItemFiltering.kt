package com.xenonware.todolist.ui.res

// import androidx.compose.material3.SegmentedButton // Not used directly if XenonSegmentedButton is created
// import androidx.compose.material3.SegmentedButtonDefaults // Not used directly
// import androidx.compose.material3.SingleChoiceSegmentedButtonRow // Not used directly
// Assuming you have XenonDialogPicker correctly defined as discussed previously
// and XenonSingleChoiceSegmentedButtonRow / XenonSegmentedButton if you made those custom
// If you create custom Xenon Segmented Buttons, import them here.
// For now, using Material 3 Segmented Buttons.
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FilterAlt
import androidx.compose.material.icons.rounded.FilterAltOff
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButtonDefaults
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
import com.xenon.mylibrary.values.LargerPadding
import com.xenon.mylibrary.values.LargestPadding
import com.xenonware.todolist.R
import com.xenonware.todolist.viewmodel.FilterState
import com.xenonware.todolist.viewmodel.FilterableAttribute


enum class FilterDialogMode {
    APPLY_AS_INCLUDED, APPLY_AS_EXCLUDED
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
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

    XenonIconDialog(
        onDismissRequest = onDismissRequest,
        title = stringResource(R.string.filter_tasks_description),
        properties = DialogProperties(usePlatformDefaultWidth = true),
        contentManagesScrolling = false,

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

        showResetIconButton = true,
        onResetIconButtonClick = {
            onResetFilters()
            checkedAttributesInDialog.clear()
            currentFilterDialogMode = FilterDialogMode.APPLY_AS_INCLUDED
        },
        resetIconColor = colorScheme.onSurfaceVariant,
        resetIconContent = {
            Icon(
                imageVector = Icons.Rounded.RestartAlt,
                contentDescription = stringResource(R.string.reset)
            )
        },
    ) {
        Column {
            XenonSingleChoiceButtonGroup(
                options = FilterDialogMode.entries.toList(),
                selectedOption = currentFilterDialogMode,
                onOptionSelect = { mode -> currentFilterDialogMode = mode },
                label = { mode ->
                    when (mode) {
                        FilterDialogMode.APPLY_AS_INCLUDED -> stringResource(R.string.include)
                        FilterDialogMode.APPLY_AS_EXCLUDED -> stringResource(R.string.exclude)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                colors = ToggleButtonDefaults.toggleButtonColors(
                    containerColor = colorScheme.surfaceContainerLow,
                    checkedContainerColor = colorScheme.primary,
                    contentColor = colorScheme.onSurface,
                    checkedContentColor = colorScheme.onPrimary
                ),
                icon = { mode, isSelected ->
                    when (mode) {
                        FilterDialogMode.APPLY_AS_INCLUDED -> Icon(
                            imageVector = Icons.Rounded.FilterAlt,
                            contentDescription = stringResource(R.string.include),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(18.dp)
                        )

                        FilterDialogMode.APPLY_AS_EXCLUDED -> Icon(
                            imageVector = Icons.Rounded.FilterAltOff,
                            contentDescription = stringResource(R.string.exclude),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(18.dp)
                        )
                    }
                })

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
                            .clip(RoundedCornerShape(100f))
                            .toggleable(
                                value = isChecked,
                                role = Role.Checkbox,
                                onValueChange = { toggleAction() }),
                        verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { toggleAction() },
                            colors = CheckboxDefaults.colors(
                                checkedColor = colorScheme.primary,
                                uncheckedColor = colorScheme.onSurfaceVariant,
                                checkmarkColor = colorScheme.onPrimary
                            )
                        )
                        Spacer(Modifier.width(LargerPadding))
                        Text(
                            text = attribute.toDisplayString(),
                            style = MaterialTheme.typography.bodyLarge,
                            color = colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
