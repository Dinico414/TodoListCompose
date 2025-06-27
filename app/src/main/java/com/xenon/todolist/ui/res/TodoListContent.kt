package com.xenon.todolist.ui.res

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween // Import tween for animation spec
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect // Import LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf // Import mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue // Import setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp // Import Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.xenon.todolist.R
import com.xenon.todolist.ui.values.LargePadding // Ensure this import is correct
import com.xenon.todolist.ui.values.MediumPadding // Ensure this import is correct
import com.xenon.todolist.viewmodel.TodoListViewModel // Ensure this import is correct
import kotlinx.coroutines.delay // Import delay

// Assuming DrawerItem data class structure, adjust if different
// data class DrawerItem(
//     val id: String,
//     val title: String,
//     val isSelectedForAction: Boolean,
//     // other properties if any
// )

// Assuming TodoListCell composable, adjust if different
// @Composable
// fun TodoListCell(
//     item: DrawerItem,
//     isSelectedForNavigation: Boolean,
//     isSelectionModeActive: Boolean,
//     isFirstItem: Boolean,
//     onClick: () -> Unit,
//     onLongClick: () -> Unit,
//     onCheckedChanged: (Boolean) -> Unit,
//     onRenameClick: () -> Unit,
//     modifier: Modifier = Modifier
// ) {
//     // Implementation of your TodoListCell
// }


@Composable
fun TodoListContent(
    viewModel: TodoListViewModel = viewModel(),
    onDrawerItemClicked: (itemId: String) -> Unit,
    onAddNewListClicked: () -> Unit,
    onRenameItemClicked: (itemId: String, currentName: String) -> Unit,
) {
    val drawerItems = viewModel.drawerItems
    val selectedDrawerItemId = viewModel.selectedDrawerItemId
    val isSelectionModeActive = viewModel.isDrawerSelectionModeActive

    ModalDrawerSheet {
        Column(modifier = Modifier.fillMaxWidth()) {
            Spacer(Modifier.height(LargePadding))

            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = LargePadding, vertical = MediumPadding)
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = MediumPadding),
                thickness = 1.dp,
                color = colorScheme.outlineVariant
            )
            Spacer(Modifier.height(MediumPadding))

            drawerItems.forEachIndexed { index, item ->
                TodoListCell(
                    item = item,
                    isSelectedForNavigation = selectedDrawerItemId == item.id,
                    isSelectionModeActive = isSelectionModeActive,
                    isFirstItem = index == 0,
                    onClick = {
                        if (isSelectionModeActive) {
                            viewModel.onItemCheckedChanged(item.id, !item.isSelectedForAction)
                        } else {
                            viewModel.onDrawerItemClick(item.id)
                            onDrawerItemClicked(item.id)
                        }
                    },
                    onLongClick = {
                        viewModel.onItemLongClick(item.id)
                    },
                    onCheckedChanged = { isChecked ->
                        viewModel.onItemCheckedChanged(item.id, isChecked)
                    },
                    onRenameClick = {
                        onRenameItemClicked(item.id, item.title)
                    },
                    modifier = Modifier.padding(horizontal = LargePadding)
                )
            }

            Spacer(Modifier.weight(1f))

            val anyItemSelectedForAction by remember(drawerItems) {
                derivedStateOf { drawerItems.any { it.isSelectedForAction } }
            }

            val buttonText = if (anyItemSelectedForAction) {
                stringResource(R.string.delete_lists) // Use string resources
            } else {
                stringResource(R.string.add_new_list) // Use string resources
            }

            val buttonContainerColor by animateColorAsState(
                targetValue = if (anyItemSelectedForAction) {
                    colorScheme.errorContainer
                } else {
                    colorScheme.primaryContainer
                },
                label = "Button Container Color Animation"
            )

            val buttonContentColor by animateColorAsState(
                targetValue = if (anyItemSelectedForAction) {
                    colorScheme.onErrorContainer
                } else {
                    colorScheme.onPrimaryContainer
                },
                label = "Button Content Color Animation"
            )

            // State for managing the pulse animation
            var currentButtonPadding by remember { mutableStateOf(LargePadding) }
            val pulsePadding = LargePadding + 8.dp // Increased padding for the pulse effect
            val defaultPadding = LargePadding

            // LaunchedEffect to trigger the pulse when anyItemSelectedForAction changes
            // We use Unit as a key to run it once on composition,
            // and then rely on the if condition inside to react to changes.
            // A more robust key might be needed if there are other recomposition triggers.
            // However, for this specific use case of reacting to anyItemSelectedForAction,
            // we can trigger the animation directly when it changes.

            val previousAnyItemSelectedForAction = remember { mutableStateOf(anyItemSelectedForAction) }

            LaunchedEffect(anyItemSelectedForAction) {
                // Only pulse if the state actually changed
                if (previousAnyItemSelectedForAction.value != anyItemSelectedForAction) {
                    currentButtonPadding = pulsePadding
                    delay(150) // Duration of the pulse (how long it stays expanded)
                    currentButtonPadding = defaultPadding
                    previousAnyItemSelectedForAction.value = anyItemSelectedForAction
                } else {
                    // Ensure padding is correct if the state hasn't changed but recomposition occurs
                    currentButtonPadding = defaultPadding
                }
            }


            // Animate the padding changes smoothly
            val animatedButtonPadding by animateDpAsState(
                targetValue = currentButtonPadding,
                animationSpec = tween(durationMillis = 150), // Animation speed for expansion/contraction
                label = "Button Padding Pulse Animation"
            )

            Button(
                onClick = {
                    if (anyItemSelectedForAction) {
                        viewModel.onDeleteSelectedClick()
                    } else {
                        onAddNewListClicked()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonContainerColor,
                    contentColor = buttonContentColor
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = animatedButtonPadding) // Apply the animated padding
                    .padding(vertical = LargePadding) // Keep vertical padding consistent or animate it too
            ) {
                Text(text = buttonText)
            }
        }
    }
}

// Example String Resources (add to your strings.xml)
// <string name="delete_selected_lists">Delete selected List(s)</string>
// <string name="add_new_list">Add new List</string>