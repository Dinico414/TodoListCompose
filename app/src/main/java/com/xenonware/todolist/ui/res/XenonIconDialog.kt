package com.xenonware.todolist.ui.res

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.xenon.mylibrary.theme.QuicksandTitleVariable
import com.xenon.mylibrary.values.DialogCornerRadius
import com.xenon.mylibrary.values.DialogPadding
import com.xenon.mylibrary.values.LargestPadding
import com.xenon.mylibrary.values.MediumPadding
import com.xenonware.todolist.R

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun XenonIconDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    title: String,
    properties: DialogProperties = DialogProperties(
        usePlatformDefaultWidth = false, dismissOnClickOutside = true, dismissOnBackPress = true
    ),
    shape: Shape = RoundedCornerShape(DialogCornerRadius),
    containerColor: Color = MaterialTheme.colorScheme.surface,
    tonalElevation: Dp = 6.dp,

    dialogPadding: PaddingValues = PaddingValues(DialogPadding /2),
    dialogTitleRowPadding: PaddingValues = PaddingValues(
        start = DialogPadding, end = DialogPadding, top = 0.dp, bottom = LargestPadding
    ),
    contentPadding: PaddingValues = PaddingValues(horizontal = DialogPadding),
    buttonRowPadding: PaddingValues = PaddingValues(
        horizontal = MediumPadding, vertical = 0.dp
    ),

    confirmButtonText: String,
    onConfirmButtonClick: () -> Unit,
    isConfirmButtonEnabled: Boolean = true,
    confirmContainerColor: Color = MaterialTheme.colorScheme.primary,
    confirmContentColor: Color = MaterialTheme.colorScheme.onPrimary,

    dismissIconButtonContainerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    dismissIconButtonContentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,

    showResetIconButton: Boolean = true,
    onResetIconButtonClick: () -> Unit = {},
    resetIconColor: Color = MaterialTheme.colorScheme.primary,

    resetIconContent: @Composable (() -> Unit)? = null,

    resetIconPainter: Painter? = null,
    resetIconContentDescription: String = "Reset",


    contentManagesScrolling: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest, properties = properties
    ) {
        val configuration = LocalConfiguration.current
        val screenHeight = configuration.screenHeightDp.dp
        val maxDialogHeight = screenHeight * 0.9f

        Surface(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .heightIn(max = maxDialogHeight),
            shape = shape,
            color = containerColor,
            tonalElevation = tonalElevation
        ) {
            Column(
                modifier = Modifier.padding(
                    top = dialogPadding.calculateTopPadding(),
                    bottom = dialogPadding.calculateBottomPadding()
                )
            ) {
                var titleLineCount by remember { mutableIntStateOf(0) }
                val titleVerticalAlignment =
                    if (titleLineCount > 1) Alignment.Top else Alignment.CenterVertically

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = dialogTitleRowPadding.calculateStartPadding(LayoutDirection.Ltr),
                            end = dialogTitleRowPadding.calculateEndPadding(LayoutDirection.Ltr),
                            top = dialogTitleRowPadding.calculateTopPadding(),
                            bottom = dialogTitleRowPadding.calculateBottomPadding()
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = titleVerticalAlignment
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = QuicksandTitleVariable
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .align(titleVerticalAlignment),
                        onTextLayout = { textLayoutResult: TextLayoutResult ->
                            titleLineCount = textLayoutResult.lineCount
                        }
                    )
                    FilledTonalIconButton(

                        onClick = onDismissRequest,
                        modifier = Modifier
                            .size(32.dp)
                            .align(titleVerticalAlignment),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = dismissIconButtonContainerColor,
                            contentColor = dismissIconButtonContentColor
                        )
                    ){
                        Icon(
                            painter = painterResource(id = R.drawable.close),
                            contentDescription = "Dismiss Dialog (Close)"
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                ) {
                    if (contentManagesScrolling) {
                        Column(Modifier.padding(contentPadding)) {
                            content()
                        }
                    } else {
                        val scrollState = rememberScrollState()
                        val topDividerAlpha by remember {
                            derivedStateOf { if (scrollState.value > 0) 1f else 0f }
                        }
                        val bottomDividerAlpha by remember {
                            derivedStateOf { if (scrollState.canScrollForward) 1f else 0f }
                        }

                        val maxHeightForScrollableInternalContent = screenHeight * 0.5f

                        HorizontalDivider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .alpha(topDividerAlpha)
                                .padding(
                                    start = contentPadding.calculateStartPadding(LayoutDirection.Ltr),
                                    end = contentPadding.calculateEndPadding(LayoutDirection.Ltr)
                                )
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = maxHeightForScrollableInternalContent)
                                .verticalScroll(scrollState)
                                .padding(contentPadding),
                            content = content
                        )

                        HorizontalDivider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .alpha(bottomDividerAlpha)
                                .padding(
                                    start = contentPadding.calculateStartPadding(LayoutDirection.Ltr),
                                    end = contentPadding.calculateEndPadding(LayoutDirection.Ltr)
                                )
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = LargestPadding)
                        .padding(buttonRowPadding),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isResetButtonVisibleAndTakingSpace = showResetIconButton && (resetIconContent != null || resetIconPainter != null)

                    val leftSpacerWeight = if (isResetButtonVisibleAndTakingSpace) 1f else 0f
                    if (leftSpacerWeight > 0f) {
                        Spacer(Modifier.weight(leftSpacerWeight))
                    }


                    FilledTonalButton(
                        onClick = onConfirmButtonClick,
                        enabled = isConfirmButtonEnabled,
                        modifier = Modifier.weight(1.5f),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = confirmContainerColor,
                            contentColor = confirmContentColor
                        )
                    ) {
                        Text(confirmButtonText)
                    }

                    if (isResetButtonVisibleAndTakingSpace) {
                        Box(
                            Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(
                                onClick = onResetIconButtonClick,

                                modifier = Modifier
                                .height(40.dp)
                                .width(52.dp )
                            ,
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = resetIconColor
                            )
                        ){
                                if (resetIconContent != null) {
                                    resetIconContent()
                                } else if (resetIconPainter != null) {
                                    Icon(
                                        painter = resetIconPainter,
                                        contentDescription = resetIconContentDescription
                                    )
                                } else {
                                    Icon(
                                        painter = painterResource(id = R.drawable.reset_time),
                                        contentDescription = "Reset"
                                    )
                                }
                            }
                        }
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
