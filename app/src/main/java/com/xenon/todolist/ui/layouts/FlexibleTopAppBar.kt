package com.xenon.todolist.ui.layouts

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.xenon.todolist.R
import androidx.compose.ui.graphics.lerp


@OptIn(ExperimentalTextApi::class)
val QuicksandTitleVariable = FontFamily(
    Font(
        R.font.quicksand_variable_font_wght,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(700),
             FontVariation.width(75f)
        )
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlexibleTopAppBarLayout(
    modifier: Modifier = Modifier,
    title: @Composable (fontWeight: FontWeight, color: Color) -> Unit = { _, _ -> },
    navigationIcon: @Composable () -> Unit = {},
    actionsIcon: @Composable RowScope.() -> Unit = {},
    collapsedTitleColor: Color = colorScheme.onSurface,
    expandedTitleColor: Color = colorScheme.primary,
    containerColor: Color = colorScheme.surfaceDim,
    navigationIconContentColor: Color = colorScheme.onSurface,
    actionIconContentColor: Color = colorScheme.onSurface,
    content: @Composable (paddingValues: PaddingValues) -> Unit,
) {
    val topAppBarState = rememberTopAppBarState()

    val scrollBehavior: TopAppBarScrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
            state = topAppBarState,
        )

    Scaffold(
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = containerColor,
        topBar = {
            val fraction = scrollBehavior.state.collapsedFraction

            val curFontWeight by remember(fraction) {
                derivedStateOf {
                    if (fraction > 0.5f) FontWeight.Bold else FontWeight.Medium
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
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        title(curFontWeight, currentTitleColor)
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
