package com.xenonware.todolist.ui.res

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.xenon.mylibrary.FlexTopAppBar
import com.xenon.mylibrary.FlexTopContainer
import com.xenon.mylibrary.TopContainer
import com.xenon.mylibrary.theme.QuicksandTitleVariable
import com.xenon.mylibrary.values.LargerCornerRadius
import com.xenon.mylibrary.values.SmallPadding
import kotlin.math.roundToInt

@Suppress("unused")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
    flexModel: String? = "FlexTopAppBar",
    titleText: String = "",
    hasNavigationIconExtraContent: Boolean = false,
    navigationIcon: @Composable () -> Unit = {},
    onNavigationIconClick: (() -> Unit)? = null,
    navigationIconExtraContent: @Composable RowScope.() -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    collapsedAppBarTextColor: Color = MaterialTheme.colorScheme.onSurface,
    expandedAppBarTextColor: Color = MaterialTheme.colorScheme.primary,
    appBarNavigationIconContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    appBarActionIconContentColor: Color = MaterialTheme.colorScheme.onSurface,
    screenBackgroundColor: Color = MaterialTheme.colorScheme.surfaceDim,
    contentBackgroundColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    expandedContainerColor: Color = MaterialTheme.colorScheme.surfaceDim,
    collapsedContainerColor: Color = MaterialTheme.colorScheme.surfaceDim,
    contentCornerRadius: Dp = LargerCornerRadius,
    navigationIconStartPadding: Dp = SmallPadding,
    navigationIconPadding: Dp = SmallPadding,
    navigationIconSpacing: Dp = SmallPadding,
    collapsedByDefault: Boolean = false,
    collapsedHeight: Dp = 64.dp,
    expandedHeight: Dp = LocalConfiguration.current.screenHeightDp.dp.times(0.3f),
    expandable: Boolean = true,
    expand: Boolean = false,
    headerContent: @Composable (fraction: Float) -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
    dialogs: @Composable () -> Unit = {},
) {
    when (flexModel) {
        "FlexTopAppBar" -> {
            FlexTopAppBar(
                collapsedHeight = collapsedHeight,
                expandedHeight = expandedHeight,
                collapsedByDefault = collapsedByDefault,
                expandable = expandable,
                title = { fraction ->
                    Text(
                        text = titleText, fontFamily = QuicksandTitleVariable, color = lerp(
                            expandedAppBarTextColor, collapsedAppBarTextColor, fraction
                        ), fontSize = lerp(32F, 22F, fraction).sp, fontWeight = FontWeight(
                            lerp(700F, 100F, fraction).roundToInt()
                        )
                    )
                },
                navigationIcon = {
                    val iconButtonContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    val interactionSource = remember { MutableInteractionSource() }

                    val minButtonSize = 32.dp

                    var boxModifier =
                        Modifier
                            .defaultMinSize(minWidth = minButtonSize)
                            .clip(RoundedCornerShape(100.0f))
                            .background(iconButtonContainerColor)

                    if (onNavigationIconClick != null) {
                        boxModifier = boxModifier.clickable(
                            onClick = onNavigationIconClick,
                            role = Role.Button,
                            interactionSource = interactionSource,
                            indication = ripple(bounded = true)
                        )
                    }

                    Box(
                        Modifier.width(72.dp), contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = boxModifier, contentAlignment = Alignment.Center
                        ) {
                            CompositionLocalProvider(LocalContentColor provides appBarNavigationIconContentColor) {
                                Row(
                                    modifier = Modifier
                                        .padding(
                                            start = navigationIconStartPadding,
                                            end = navigationIconPadding
                                        )
                                        .padding(vertical = navigationIconPadding),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(
                                        navigationIconSpacing
                                    )
                                ) {
                                    navigationIcon()
                                    if (hasNavigationIconExtraContent) {
                                        navigationIconExtraContent()
                                    }
                                }
                            }

                        }
                    }
                },
                actions = actions,
                titleAlignment = if (onNavigationIconClick == null && !hasNavigationIconExtraContent) Alignment.Center else Alignment.CenterStart,
                collapsedContainerColor = screenBackgroundColor,
                expandedContainerColor = screenBackgroundColor,
                navigationIconContentColor = appBarNavigationIconContentColor,
                actionIconContentColor = appBarActionIconContentColor,
                modifier = modifier,
            ) { paddingValuesFromAppBar ->
                SharedScreenContent(
                    modifier = contentModifier,
                    paddingValuesFromAppBar = paddingValuesFromAppBar,
                    screenBackgroundColor = screenBackgroundColor,
                    contentBackgroundColor = contentBackgroundColor,
                    contentCornerRadius = contentCornerRadius,
                    content = content,
                    dialogs = dialogs
                )
            }
        }

        "FlexTopContainer" -> {
            FlexTopContainer(
                modifier = modifier,
                collapsedHeight = collapsedHeight,
                expandedHeight = expandedHeight,
                collapsedByDefault = collapsedByDefault,
                expandable = expandable,
                expandedContainerColor = screenBackgroundColor,
                collapsedContainerColor = screenBackgroundColor,
                headerContent = headerContent
            ) { paddingValuesFromContainer ->
                SharedScreenContent(
                    modifier = contentModifier,
                    paddingValuesFromAppBar = paddingValuesFromContainer,
                    screenBackgroundColor = screenBackgroundColor,
                    contentBackgroundColor = contentBackgroundColor,
                    contentCornerRadius = contentCornerRadius,
                    content = content,
                    dialogs = dialogs
                )
            }
        }
        "TopContainer" -> {
            TopContainer(
                modifier = modifier,
                collapsedHeight = collapsedHeight,
                expandedHeight = expandedHeight,
                expand = expand,
                expandedContainerColor = expandedContainerColor,
                collapsedContainerColor = collapsedContainerColor,
                headerContent = headerContent
            ) { paddingValuesFromContainer ->
                SharedScreenContent(
                    modifier = contentModifier,
                    paddingValuesFromAppBar = paddingValuesFromContainer,
                    screenBackgroundColor = screenBackgroundColor,
                    contentBackgroundColor = contentBackgroundColor,
                    contentCornerRadius = contentCornerRadius,
                    content = content,
                    dialogs = dialogs
                )
            }
        }
    }
}

@Composable
private fun SharedScreenContent(
    modifier: Modifier,
    paddingValuesFromAppBar: PaddingValues,
    screenBackgroundColor: Color,
    contentBackgroundColor: Color,
    contentCornerRadius: Dp,
    content: @Composable (PaddingValues) -> Unit,
    dialogs: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBackgroundColor)
            .padding(top = paddingValuesFromAppBar.calculateTopPadding())
            .padding(
                WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)
                    .asPaddingValues()
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(modifier)
                .clip(
                    RoundedCornerShape(
                        topStart = contentCornerRadius, topEnd = contentCornerRadius
                    )
                )
                .background(contentBackgroundColor)
        ) {
            content(paddingValuesFromAppBar)
        }
    }
    dialogs()
}
