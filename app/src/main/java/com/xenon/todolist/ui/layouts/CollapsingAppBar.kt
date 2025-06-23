package com.xenon.todolist.ui.layouts

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.lerp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollapsingAppBarLayout(
//    do not make any changes here no matter what, all of this needs to stay functional
    modifier: Modifier = Modifier,
    title: @Composable (fontWeight: FontWeight, fontSize: TextUnit, color: Color) -> Unit = { _, _, _ -> },
    navigationIcon: @Composable () -> Unit = {},
    actionsIcon: @Composable RowScope.() -> Unit = {},
    collapsedTitleColor: Color = colorScheme.onBackground,
    expandedTitleColor: Color = colorScheme.primary,
    containerColor: Color = colorScheme.background,
    navigationIconContentColor: Color = colorScheme.onBackground,
    actionIconContentColor: Color = colorScheme.onBackground,
    content: @Composable (paddingValues: PaddingValues) -> Unit,
//    all the way till here
) {
    val topAppBarState = rememberTopAppBarState()
    val snapAnimationSpec = spring<Float>(stiffness = Spring.StiffnessMedium)
    val flingAnimationSpec = TopAppBarDefaults.exitUntilCollapsedScrollBehavior().flingAnimationSpec

    val scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        state = topAppBarState,
        snapAnimationSpec = snapAnimationSpec,
        flingAnimationSpec = flingAnimationSpec
    )

    Scaffold(
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .padding(
                WindowInsets.safeDrawing
                    .only(WindowInsetsSides.Horizontal)
                    .asPaddingValues()
            ),
        containerColor = containerColor,
        topBar = {
            val fraction = scrollBehavior.state.collapsedFraction

            val expandedFontSize = MaterialTheme.typography.headlineLarge.fontSize
            val collapsedFontSize = MaterialTheme.typography.titleLarge.fontSize

            val curFontSize by remember(fraction, expandedFontSize, collapsedFontSize) {
                derivedStateOf {
                    lerp(expandedFontSize, collapsedFontSize, fraction)
                }
            }

            val curFontWeight by remember(fraction) {
                derivedStateOf {
                    FontWeight.SemiBold
                }
            }

            val currentTitleColor by remember(fraction, expandedTitleColor, collapsedTitleColor) {
                derivedStateOf {
                    lerp(expandedTitleColor, collapsedTitleColor, fraction)
                }
            }

            LargeTopAppBar(
                title = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        title(curFontWeight, curFontSize, currentTitleColor)
                    }
                },
                navigationIcon = navigationIcon,
                actions = actionsIcon,
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = containerColor,
                    scrolledContainerColor = containerColor,
                    navigationIconContentColor = navigationIconContentColor,
                    titleContentColor = currentTitleColor,
                    actionIconContentColor = actionIconContentColor
                )
            )
        }
    ) { paddingValues ->
        content(paddingValues)
    }
}

