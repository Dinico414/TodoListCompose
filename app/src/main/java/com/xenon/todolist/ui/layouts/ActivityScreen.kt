package com.xenon.todolist.ui.layouts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text // ***** ADD THIS IMPORT *****
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
// Import QuicksandTitleVariable (it's in the same package, so direct access)
// import com.xenon.todolist.ui.layouts.QuicksandTitleVariable // Not strictly needed if in same package
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.xenon.todolist.ui.values.LargerCornerRadius

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(
    titleText: String,
    navigationIcon: @Composable (() -> Unit)? = null,
    appBarActions: @Composable RowScope.() -> Unit = {},
    collapsedAppBarTextColor: Color = MaterialTheme.colorScheme.onSurface,
    expandedAppBarTextColor: Color = MaterialTheme.colorScheme.primary,
    appBarNavigationIconContentColor: Color = MaterialTheme.colorScheme.onSurface,
    appBarActionIconContentColor: Color = MaterialTheme.colorScheme.onSurface,
    screenBackgroundColor: Color = MaterialTheme.colorScheme.surfaceDim,
    contentBackgroundColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    contentCornerRadius: Dp = LargerCornerRadius,
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit,
    dialogs: @Composable () -> Unit = {}
) {
    FlexibleTopAppBarLayout(
        title = { fontWeightFromAppBar, fontSizeFromAppBar, colorFromAppBar ->
            Text(
                text = titleText,
                fontFamily = QuicksandTitleVariable,
                fontSize = fontSizeFromAppBar,
                color = colorFromAppBar
            )
        },
        navigationIcon = {
            navigationIcon?.invoke()
        },
        modifier = modifier,
        actionsIcon = appBarActions,
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
