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
                            TextButton(onClick = onActionButton1Click) {
                                Text(actionButton1Text)
                            }
                        }
                    } else null

                val confirmComposable: (@Composable RowScope.() -> Unit)? =
                    if (confirmButtonText != null && onConfirmButtonClick != null) {
                        {
                            FilledTonalButton(onClick = onConfirmButtonClick) {
                                Text(confirmButtonText)
                            }
                        }
                    } else null

                val action2Composable: (@Composable RowScope.() -> Unit)? =
                    if (actionButton2Text != null && onActionButton2Click != null) {
                        {
                            TextButton(onClick = onActionButton2Click) {
                                Text(actionButton2Text)
                            }
                        }
                    } else null

                val buttonsToShow = listOfNotNull(action1Composable, confirmComposable, action2Composable)

                if (buttonsToShow.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = DialogPadding),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, if (buttonsToShow.size >= 3) Alignment.End else Alignment.Start),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        when (buttonsToShow.size) {
                            1 -> {
                                Spacer(Modifier.weight(1f))
                                buttonsToShow.first().invoke(this)
                            }
                            2 -> {
                                Spacer(Modifier.weight(0.5f))
                                buttonsToShow[0].invoke(this)
                                Spacer(Modifier.weight(0.5f))
                                buttonsToShow[1].invoke(this)
                                Spacer(Modifier.weight(0.5f))
                            }
                            3 -> {
                                val actualAction1 = if (actionButton1Text != null && onActionButton1Click != null) action1Composable else null
                                val actualAction2 = if (actionButton2Text != null && onActionButton2Click != null) action2Composable else null
                                val actualConfirm = if (confirmButtonText != null && onConfirmButtonClick != null) confirmComposable else null

                                actualAction1?.invoke(this)
                                actualAction2?.invoke(this)

                                if ((actualAction1 != null || actualAction2 != null) && actualConfirm != null) {
                                    Spacer(Modifier.weight(1f))
                                } else if (actualAction1 == null && actualAction2 == null && actualConfirm != null) {

                                    Spacer(Modifier.weight(1f))
                                }


                                actualConfirm?.invoke(this)

                            }
                            else -> {
                                buttonsToShow.forEach { button ->
                                    button.invoke(this)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}