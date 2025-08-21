@file:Suppress("unused")

package com.xenon.todolist.ui.res

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


private val DefaultXenonButtonShape = CircleShape
@Composable
fun XenonBaseButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = DefaultXenonButtonShape,
    buttonColors: XenonButtonColors, 
    border: BorderStroke? = null,
    elevation: Dp = 0.dp,
    contentPadding: PaddingValues = XenonButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit,
) {
    val currentBackgroundColor by buttonColors.backgroundColor(enabled)
    val currentContentColor by buttonColors.contentColor(enabled)

    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 64.dp, minHeight = 36.dp)
            .then(
                if (elevation > 0.dp) Modifier.shadow(
                    elevation,
                    shape,
                    clip = true
                ) else Modifier
            )
            .clip(shape)
            .background(
                color = currentBackgroundColor, // Use state value
                shape = shape
            )
            .then(
                if (border != null && enabled) Modifier.border(
                    border,
                    shape
                ) else if (border != null) Modifier.border(
                    border.copy(
                        brush = SolidColor(
                            currentContentColor.copy(alpha = 0.5f)
                        )
                    ), shape
                ) else Modifier
            ) // Adjust border for disabled state
            .clickable(
                onClick = onClick,
                enabled = enabled,
                role = Role.Button,
                interactionSource = interactionSource,
                indication = null

            )
            .padding(contentPadding)
            .semantics { role = Role.Button },
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(LocalContentColor provides currentContentColor) { // Use state value
            ProvideTextStyle(value = MaterialTheme.typography.labelLarge.copy(color = currentContentColor)) { // Use state value
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    content = content
                )
            }
        }
    }
}

// --- XenonButtonColors Data Class & Defaults ---
// (This was mostly correct, but ensure it's used consistently)
data class XenonButtonColors(
    private val backgroundColor: Color, // Make these private if only accessed via functions
    private val contentColor: Color,
    private val disabledBackgroundColor: Color,
    private val disabledContentColor: Color,
) {
    @Composable
    fun backgroundColor(enabled: Boolean): State<Color> {
        return rememberUpdatedState(if (enabled) backgroundColor else disabledBackgroundColor)
    }

    @Composable
    fun contentColor(enabled: Boolean): State<Color> {
        return rememberUpdatedState(if (enabled) contentColor else disabledContentColor)
    }
}

object XenonButtonDefaults {
    val ContentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    val TextButtonContentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
    internal val IconSpacing = 8.dp // Keep internal if only used within this file

    @Composable
    fun filledButtonColors(
        containerColor: Color = MaterialTheme.colorScheme.primary,
        contentColor: Color = contentColorFor(containerColor), // contentColorFor is an M3 utility
        disabledContainerColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
        disabledContentColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
    ): XenonButtonColors = XenonButtonColors(
        backgroundColor = containerColor,
        contentColor = contentColor,
        disabledBackgroundColor = disabledContainerColor,
        disabledContentColor = disabledContentColor
    )

    @Composable
    fun outlinedButtonColors(
        // For outlined, background is typically transparent, content is a primary/accent color
        containerColor: Color = Color.Transparent,
        contentColor: Color = MaterialTheme.colorScheme.primary,
        disabledContainerColor: Color = Color.Transparent,
        disabledContentColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
    ): XenonButtonColors = XenonButtonColors(
        backgroundColor = containerColor,
        contentColor = contentColor,
        disabledBackgroundColor = disabledContainerColor,
        disabledContentColor = disabledContentColor
    )

    @Composable
    fun outlinedBorderStroke(
        enabled: Boolean,
        colors: XenonButtonColors, // Use our XenonButtonColors
    ): BorderStroke {
        val borderColor by colors.contentColor(enabled) // Get the State<Color>
        val finalBorderColor = if (enabled) borderColor else borderColor.copy(alpha = 0.5f) // Dim border when disabled
        return BorderStroke(1.dp, finalBorderColor)
    }


    @Composable
    fun textButtonColors(
        containerColor: Color = Color.Transparent,
        contentColor: Color = MaterialTheme.colorScheme.primary,
        disabledContainerColor: Color = Color.Transparent,
        disabledContentColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
    ): XenonButtonColors = XenonButtonColors(
        backgroundColor = containerColor,
        contentColor = contentColor,
        disabledBackgroundColor = disabledContainerColor,
        disabledContentColor = disabledContentColor
    )
}


// --- Xenon Filled Button ---
@Composable
fun XenonFilledButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = DefaultXenonButtonShape,
    colors: XenonButtonColors = XenonButtonDefaults.filledButtonColors(), // Use XenonButtonColors
    contentPadding: PaddingValues = XenonButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    elevation: Dp = 0.dp, // Add elevation if desired for filled buttons
    content: @Composable RowScope.() -> Unit,
) {
    XenonBaseButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        buttonColors = colors, // Pass the XenonButtonColors instance
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        elevation = elevation,
        content = content
    )
}

@Suppress("unused")
@Composable
fun XenonFilledButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = DefaultXenonButtonShape,
    colors: XenonButtonColors = XenonButtonDefaults.filledButtonColors(),
    contentPadding: PaddingValues = XenonButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    elevation: Dp = 0.dp,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    XenonFilledButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        colors = colors,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        elevation = elevation
    ) {
        XenonButtonContent(text = text, leadingIcon = leadingIcon, trailingIcon = trailingIcon)
    }
}


// --- Xenon Outlined Button ---
@Composable
fun XenonOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = DefaultXenonButtonShape,
    colors: XenonButtonColors = XenonButtonDefaults.outlinedButtonColors(),
    // Border is now created inside XenonBaseButton or passed explicitly
    // We can provide a default border using our helper
    border: BorderStroke? = XenonButtonDefaults.outlinedBorderStroke(enabled, colors),
    contentPadding: PaddingValues = XenonButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit,
) {
    XenonBaseButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        buttonColors = colors,
        border = border, // Pass the calculated border
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = content
    )
}

@Suppress("unused")
@Composable
fun XenonOutlinedButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = DefaultXenonButtonShape,
    colors: XenonButtonColors = XenonButtonDefaults.outlinedButtonColors(),
    border: BorderStroke? = XenonButtonDefaults.outlinedBorderStroke(enabled, colors),
    contentPadding: PaddingValues = XenonButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    XenonOutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        colors = colors,
        border = border,
        contentPadding = contentPadding,
        interactionSource = interactionSource
    ) {
        XenonButtonContent(text = text, leadingIcon = leadingIcon, trailingIcon = trailingIcon)
    }
}


// --- Xenon Text Button ---
@Composable
fun XenonTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = DefaultXenonButtonShape, // Text buttons can also have shapes for focus/hover indication
    colors: XenonButtonColors = XenonButtonDefaults.textButtonColors(),
    contentPadding: PaddingValues = XenonButtonDefaults.TextButtonContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit,
) {
    XenonBaseButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape, // Allow shape for ripple and potential background on interaction
        buttonColors = colors,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        // Text buttons typically don't have their own border or elevation by default
        border = null,
        elevation = 0.dp,
        content = content
    )
}

@Composable
fun XenonTextButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = DefaultXenonButtonShape,
    colors: XenonButtonColors = XenonButtonDefaults.textButtonColors(),
    contentPadding: PaddingValues = XenonButtonDefaults.TextButtonContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    XenonTextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        colors = colors,
        contentPadding = contentPadding,
        interactionSource = interactionSource
    ) {
        XenonButtonContent(text = text, leadingIcon = leadingIcon, trailingIcon = trailingIcon)
    }
}


// --- Xenon Icon Button ---
// (This one is structured a bit differently as it doesn't use XenonBaseButton
//  due to its unique unbounded ripple and typical fixed size/shape)
@Composable
fun XenonIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    iconColor: Color = LocalContentColor.current,
    disabledIconColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
    backgroundColor: Color = Color.Transparent, // Usually transparent
    shape: Shape = CircleShape,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit,
) {
    val currentIconColor = if (enabled) iconColor else disabledIconColor
    if (enabled) LocalContentColor.current.copy(alpha = 0.2f) else Color.Transparent // Ripple only when enabled

    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 40.dp, minHeight = 40.dp) // Ensure minimum touch target
            .clip(shape)
            .background(
                color = if (enabled) backgroundColor else Color.Transparent,
                shape = shape
            )
            .clickable(
                onClick = onClick,
                enabled = enabled,
                role = Role.Button,
                interactionSource = interactionSource,
                indication = null
                                )
            .padding(8.dp), // Padding around the icon itself
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(LocalContentColor provides currentIconColor) {
            content()
        }
    }
}

// --- Xenon Open Content Button (Flexible Box-based) ---
@Composable
fun XenonOpenContentButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = DefaultXenonButtonShape,
    // Use XenonButtonColors for consistency
    colors: XenonButtonColors = XenonButtonDefaults.filledButtonColors(
        containerColor = MaterialTheme.colorScheme.secondary, // Example default
        contentColor = contentColorFor(MaterialTheme.colorScheme.secondary)
    ),
    border: BorderStroke? = null,
    elevation: Dp = 0.dp,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable BoxScope.() -> Unit,
) {
    val currentBackgroundColor by colors.backgroundColor(enabled)
    val currentContentColor by colors.contentColor(enabled)

    Box(
        modifier = modifier
            .then(
                if (elevation > 0.dp) Modifier.shadow(
                    elevation,
                    shape,
                    clip = true
                ) else Modifier
            )
            .clip(shape)
            .background(
                color = currentBackgroundColor,
                shape = shape
            )
            .then(
                if (border != null && enabled) Modifier.border(
                    border,
                    shape
                ) else if (border != null) Modifier.border(
                    border.copy(
                        brush = SolidColor(
                            currentContentColor.copy(alpha = 0.5f)
                        )
                    ), shape
                ) else Modifier
            )
            .clickable(
                onClick = onClick,
                enabled = enabled,
                role = Role.Button,
                interactionSource = interactionSource,
                indication = null
            )
            .padding(contentPadding)
            .semantics { role = Role.Button },
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(LocalContentColor provides currentContentColor) {
            ProvideTextStyle(value = MaterialTheme.typography.labelLarge.copy(color = currentContentColor)) {
                content()
            }
        }
    }
}


// --- Helper for Text and Icon content ---
@Composable
internal fun RowScope.XenonButtonContent(
    text: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    if (leadingIcon != null) {
        Box(
            Modifier
                .sizeIn(maxHeight = 18.dp, maxWidth = 18.dp) // Constrain icon size
                .align(Alignment.CenterVertically) // Align icon within its box
        ) {
            leadingIcon()
        }
        Spacer(Modifier.width(XenonButtonDefaults.IconSpacing))
    }
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge, // Ensure text style is applied
        modifier = Modifier.align(Alignment.CenterVertically) // Align text with icons
    )
    if (trailingIcon != null) {
        Spacer(Modifier.width(XenonButtonDefaults.IconSpacing))
        Box(
            Modifier
                .sizeIn(maxHeight = 18.dp, maxWidth = 18.dp) // Constrain icon size
                .align(Alignment.CenterVertically) // Align icon within its box
        ){
            trailingIcon()
        }
    }
}

