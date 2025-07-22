package com.xenon.todolist.ui.layouts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.xenon.todolist.ui.values.LargeCornerRadius

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(
    title: @Composable (fontWeight: FontWeight, fontSize: TextUnit, color: Color) -> Unit,
    navigationIcon: @Composable (() -> Unit)? = null,
    appBarActions: @Composable RowScope.() -> Unit = {},
    collapsedAppBarTextColor: Color = MaterialTheme.colorScheme.onBackground,
    expandedAppBarTextColor: Color = MaterialTheme.colorScheme.primary,
    appBarContainerColor: Color = MaterialTheme.colorScheme.background,
    appBarNavigationIconContentColor: Color = MaterialTheme.colorScheme.onBackground,
    appBarActionIconContentColor: Color = MaterialTheme.colorScheme.onBackground,
    screenBackgroundColor: Color = MaterialTheme.colorScheme.surfaceDim,
    contentBackgroundColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    contentCornerRadius: Dp = LargeCornerRadius,
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit,
    dialogs: @Composable () -> Unit = {}
) {
    FlexibleTopAppBarLayout(
        title = { fontWeight, fontSize, color ->
            title(fontWeight, fontSize, color)
        },
        navigationIcon = {
            navigationIcon?.invoke()
        },
        modifier = modifier,
        actionsIcon = appBarActions,
        collapsedTitleColor = collapsedAppBarTextColor,
        expandedTitleColor = expandedAppBarTextColor,
        containerColor = appBarContainerColor,
        navigationIconContentColor = appBarNavigationIconContentColor,
        actionIconContentColor = appBarActionIconContentColor,
    ) { paddingValuesFromAppBar ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(screenBackgroundColor)
                .padding(top = paddingValuesFromAppBar.calculateTopPadding())
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