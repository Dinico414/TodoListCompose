package com.xenon.todolist.ui.res

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.xenon.todolist.ui.values.DialogCornerRadius
import com.xenon.todolist.ui.values.DialogPadding
import com.xenon.todolist.ui.values.LargestPadding

@Composable
fun XenonDialog(
    onDismissRequest: () -> Unit,
    title: String,
    properties: DialogProperties = DialogProperties(
        usePlatformDefaultWidth = false,
        dismissOnClickOutside = true,
        dismissOnBackPress = true
    ),
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(DialogCornerRadius),
    containerColor: Color = MaterialTheme.colorScheme.surface,
    tonalElevation: Dp = 6.dp,
    contentPadding: PaddingValues = PaddingValues(DialogPadding),
    actionButton1Text: String? = null,
    onActionButton1Click: (() -> Unit)? = null,
    confirmButtonText: String? = null,
    onConfirmButtonClick: (() -> Unit)? = null,
    actionButton2Text: String? = null,
    onActionButton2Click: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = shape,
            color = containerColor,
            tonalElevation = tonalElevation
        ) {
            Column(
                modifier = Modifier.padding(contentPadding)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = LargestPadding),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    IconButton(onClick = onDismissRequest) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Dismiss Dialog"
                        )
                    }
                }

                content()

                val action1Composable: (@Composable RowScope.() -> Unit)? =
                    if (actionButton1Text != null && onActionButton1Click != null) {
                        {
                            TextButton(
                                onClick = onActionButton1Click,
                                modifier = if (actionButton2Text != null && confirmButtonText != null) Modifier.weight(1f) else Modifier
                            ) {
                                Text(actionButton1Text)
                            }
                        }
                    } else null

                val confirmComposable: (@Composable RowScope.() -> Unit)? =
                    if (confirmButtonText != null && onConfirmButtonClick != null) {
                        {
                            FilledTonalButton(
                                onClick = onConfirmButtonClick,
                                modifier = if (actionButton1Text != null && actionButton2Text != null) Modifier.weight(1.2f) else Modifier.weight(1.2f),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )  {
                                Text(confirmButtonText)
                            }
                        }
                    } else null

                val action2Composable: (@Composable RowScope.() -> Unit)? =
                    if (actionButton2Text != null && onActionButton2Click != null) {
                        {
                            TextButton(
                                onClick = onActionButton2Click,
                                modifier = if (actionButton1Text != null && confirmButtonText != null) Modifier.weight(1f) else Modifier
                            ) {
                                Text(actionButton2Text)
                            }
                        }
                    } else null

                val hasAction1 = action1Composable != null
                val hasConfirm = confirmComposable != null
                val hasAction2 = action2Composable != null

                val anyButtonPresent = hasAction1 || hasConfirm || hasAction2

                if (anyButtonPresent) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = DialogPadding),
                        horizontalArrangement = if (hasAction1 && hasConfirm && hasAction2) Arrangement.spacedBy(8.dp) else Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        @Suppress("KotlinConstantConditions")
                        if (hasAction1 && hasConfirm && hasAction2) {
                            action1Composable.invoke(this)
                            confirmComposable.invoke(this)
                            action2Composable.invoke(this)
                        } else if (!hasAction1 && hasConfirm && !hasAction2) {
                            Spacer(Modifier.weight(1f))
                            confirmComposable.invoke(this)
                            Spacer(Modifier.weight(1f))
                        } else if (hasAction1 && hasConfirm && !hasAction2) {
                            Spacer(Modifier.weight(1f))
                            action1Composable.invoke(this)
                            confirmComposable.invoke(this)
                            Spacer(Modifier.weight(1f))
                        } else if (!hasAction1 && hasConfirm && hasAction2) {
                            Spacer(Modifier.weight(1f))
                            confirmComposable.invoke(this)
                            action2Composable.invoke(this)
                            Spacer(Modifier.weight(1f))
                        } else if (hasAction1 && !hasConfirm && !hasAction2) {
                            Spacer(Modifier.weight(1f))
                            action1Composable.invoke(this)
                        } else if (!hasAction1 && !hasConfirm && hasAction2) {
                            Spacer(Modifier.weight(1f))
                            action2Composable.invoke(this)
                        } else if (hasAction1 && !hasConfirm && hasAction2) {
                            action1Composable.invoke(this)
                            Spacer(Modifier.weight(1f))
                            action2Composable.invoke(this)
                        }
                    }
                }
            }
        }
    }
}