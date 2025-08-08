package com.xenon.todolist.ui.layouts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.shape.CircleShape // Import CircleShape or use MaterialTheme.shapes.small
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.xenon.todolist.ui.values.LargerCornerRadius
import com.xenon.todolist.ui.values.LargestPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(
    titleText: String,
    navigationIconContent: @Composable (() -> Unit)? = null,
    onNavigationIconClick: (() -> Unit)? = null,
    appBarNavigationIconExtraContent: @Composable RowScope.() -> Unit = {},
    appBarActions: @Composable RowScope.() -> Unit = {},
    appBarSecondaryActionIcon: @Composable RowScope.() -> Unit = {},
    collapsedAppBarTextColor: Color = MaterialTheme.colorScheme.onSurface,
    expandedAppBarTextColor: Color = MaterialTheme.colorScheme.primary,
    appBarNavigationIconContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    appBarActionIconContentColor: Color = MaterialTheme.colorScheme.onSurface,
    screenBackgroundColor: Color = MaterialTheme.colorScheme.surfaceDim,
    contentBackgroundColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    contentCornerRadius: Dp = LargerCornerRadius,
    buttonPadding: Dp = LargestPadding, // This is the padding for the Box around the nav icons
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit,
    dialogs: @Composable () -> Unit = {}
) {
    FlexibleTopAppBarLayout(
        title = { fontWeightFromAppBar, colorFromAppBar ->
            Text(
                text = titleText,
                fontFamily = QuicksandTitleVariable,
                color = colorFromAppBar,
                fontWeight = fontWeightFromAppBar
            )
        },
        navigationIcon = {
            if (navigationIconContent != null || appBarNavigationIconExtraContent != {}) {
                val iconButtonContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                Box(
                    modifier = Modifier
                        .padding(horizontal = buttonPadding)
                        .clip(RoundedCornerShape(100.0f))
                        .background(iconButtonContainerColor)

                    ,
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (navigationIconContent != null && onNavigationIconClick != null) {
                            IconButton(
                                onClick = onNavigationIconClick,
                                colors = IconButtonDefaults.iconButtonColors(
                                    contentColor = appBarNavigationIconContentColor
                                ),
                            ) {
                                navigationIconContent()
                            }
                        }
                        this@Row.appBarNavigationIconExtraContent()
                    }
                }
            }
        },
        navigationIconExtraContent = { },
        modifier = modifier,
        actionsIcon = appBarActions,
        secondaryActionIcon = appBarSecondaryActionIcon,
        collapsedTitleColor = collapsedAppBarTextColor,
        expandedTitleColor = expandedAppBarTextColor,
        containerColor = screenBackgroundColor,
        navigationIconContentColor = appBarNavigationIconContentColor,
        actionIconContentColor = appBarActionIconContentColor,
    ) { paddingValuesFromAppBar ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(screenBackgroundColor)
                .padding(top = paddingValuesFromAppBar.calculateTopPadding())
                .padding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal).asPaddingValues())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .then(contentModifier)
                    .clip(
                        RoundedCornerShape(
                            topStart = contentCornerRadius,
                            topEnd = contentCornerRadius
                        )
                    )
                    .background(contentBackgroundColor)
            ) {
                content(paddingValuesFromAppBar)
            }
        }
        dialogs()
    }
}
