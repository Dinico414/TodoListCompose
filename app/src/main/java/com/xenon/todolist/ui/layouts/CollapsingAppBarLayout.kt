package com.xenon.todolist.ui.layouts

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollapsingAppBarLayout(
    modifier: Modifier = Modifier,
    collapsedHeight: Dp = 54.dp,
    title: @Composable (fraction: Float) -> Unit = { _ -> },
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    titleAlignment: Alignment = Alignment.CenterStart,
    navigationIconAlignment: Alignment.Vertical = Alignment.Top,
    expandable: Boolean = true,
    expandedContainerColor: Color = MaterialTheme.colorScheme.background,
    collapsedContainerColor: Color = MaterialTheme.colorScheme.background,
    navigationIconContentColor: Color = MaterialTheme.colorScheme.onBackground,
    actionIconContentColor: Color = MaterialTheme.colorScheme.onBackground,
    content: @Composable (paddingValues: PaddingValues) -> Unit
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val expandedHeight = remember(expandable) {
        if (expandable) (screenHeight / 100) * 35 else collapsedHeight
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState()
    )
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
            // Dummy that consumes the scrollBehaviour
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

            // Real AppBar that reads scrollBehaviour values for its height
            val fraction = if (expandable) scrollBehavior.state.collapsedFraction else 1f
            val curHeight = collapsedHeight.times(fraction) +
                    expandedHeight.times(1 - fraction)
            val offset = curHeight - collapsedHeight

            var boxWidth by remember { mutableIntStateOf(0) }

            CenterAlignedTopAppBar(
                expandedHeight = curHeight,
                title = {
                    // navigationIcon
                    Box(
                        modifier = Modifier
                            .height(curHeight)
                            .then(
                                when (navigationIconAlignment) {
                                    Alignment.Top -> Modifier.padding(bottom = offset)
                                    Alignment.Bottom -> Modifier.padding(top = offset)
                                    else -> Modifier
                                }
                            )
                            .onGloballyPositioned { layoutCoordinates ->
                                if(layoutCoordinates.size.width != boxWidth)
                                    boxWidth = layoutCoordinates.size.width
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        navigationIcon()
                    }
                    // title
                    Box(
                        contentAlignment = titleAlignment,
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                            when (titleAlignment) {
                                Alignment.Center, Alignment.CenterStart, Alignment.CenterEnd ->
                                    Modifier.height(curHeight)
                                Alignment.BottomStart, Alignment.BottomCenter, Alignment.BottomEnd ->
                                    Modifier.padding(top = offset)
                                else -> Modifier
                            }
                            .padding(start = lerp(0.dp, (boxWidth / LocalDensity.current.density).dp, fraction))
                        ),
                    ) {
                        title(fraction)
                    }
                    // actions
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(curHeight)
                            .then(
                                when (navigationIconAlignment) {
                                    Alignment.Top -> Modifier.padding(bottom = offset)
                                    Alignment.Bottom -> Modifier.padding(top = offset)
                                    else -> Modifier
                                }
                            ),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Row {
                            actions()
                        }
                    }
                },
                navigationIcon = {},
                actions = {},
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                    navigationIconContentColor = navigationIconContentColor,
                    actionIconContentColor = actionIconContentColor
                ),
            )
        }
    ) { paddingValues ->
        content(paddingValues)
    }
}