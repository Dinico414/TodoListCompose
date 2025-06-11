package com.xenon.todolist.ui.layouts

import android.R.attr.fontWeight
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollapsingAppBarLayout(
    modifier: Modifier = Modifier,
    collapsedHeight: Dp = 54.dp,
    title: @Composable (fontWeight: FontWeight, fontSize: TextUnit, color: Color) -> Unit = { _, _, _ -> },
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    expandable: Boolean = true,
    expandedTextColor: Color = MaterialTheme.colorScheme.primary,
    collapsedTextColor: Color = MaterialTheme.colorScheme.onBackground,
    expandedContainerColor: Color = MaterialTheme.colorScheme.background,
    collapsedContainerColor: Color = MaterialTheme.colorScheme.background,
    navigationIconContentColor: Color = MaterialTheme.colorScheme.onBackground,
    actionIconContentColor: Color = MaterialTheme.colorScheme.onBackground,
    content: @Composable (paddingValues: PaddingValues) -> Unit,
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val expandedHeight = remember(expandable) {
        if (expandable) (screenHeight / 100) * 35 else collapsedHeight
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    Scaffold(
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .background(MaterialTheme.colorScheme.background)
            .padding(
                WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Horizontal
                ).asPaddingValues()
            ),
        topBar = {
            LargeTopAppBar(
                title = {},
                collapsedHeight = collapsedHeight,
                expandedHeight = expandedHeight,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = expandedContainerColor,
                    scrolledContainerColor = collapsedContainerColor,
                    navigationIconContentColor = navigationIconContentColor,
                    titleContentColor = Color.Transparent,
                    actionIconContentColor = actionIconContentColor,
                ),
                scrollBehavior = scrollBehavior
            )

            val fraction = if (expandable) scrollBehavior.state.collapsedFraction else 1f
            val curHeight by remember(fraction, expandedHeight, collapsedHeight) {
                derivedStateOf { collapsedHeight.times(fraction) + expandedHeight.times(1 - fraction) }
            }
            val curFontSize by remember(fraction) { derivedStateOf { (24 * fraction + 45 * (1 - fraction)).sp } }
            val curFontWeight by remember(fraction) { derivedStateOf { if (fraction == 1f) FontWeight.SemiBold else FontWeight.Normal } }

            val interpolatedTextColor by remember(fraction, expandedTextColor, collapsedTextColor) {
                derivedStateOf {
                    lerp(expandedTextColor, collapsedTextColor, fraction)
                }
            }
            CenterAlignedTopAppBar(
                expandedHeight = curHeight,
                title = {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        title(curFontWeight, curFontSize, interpolatedTextColor)
                    }
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(top = curHeight - collapsedHeight),
                        contentAlignment = Alignment.Center
                    ) {
                        navigationIcon()
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(top = curHeight - collapsedHeight),
                        contentAlignment = Alignment.Center
                    ) {
                        Row {
                            actions()
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                    navigationIconContentColor = navigationIconContentColor,
                    titleContentColor = interpolatedTextColor,
                    actionIconContentColor = actionIconContentColor
                ),
            )
        }
    ) { paddingValues ->
        content(paddingValues)
    }
}