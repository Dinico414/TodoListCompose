package com.xenon.todolist.ui.res

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.xenon.todolist.R
import com.xenon.todolist.ui.values.LargerPadding
import com.xenon.todolist.viewmodel.ThemeSetting

@Composable
fun DialogThemeSelection(
    themeOptions: Array<ThemeSetting>,
    currentThemeIndex: Int,
    onThemeSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    XenonDialog(
        onDismissRequest = onDismiss,
        title = stringResource(R.string.theme_dialog_title),
        confirmButtonText = stringResource(R.string.ok),
        onConfirmButtonClick = onConfirm,
        properties = DialogProperties(usePlatformDefaultWidth = true),
    ) {
        val listState = rememberLazyListState()
        var canScroll by remember { mutableStateOf(false) }

        LaunchedEffect(remember { derivedStateOf { listState.layoutInfo } }, themeOptions.size) {
            canScroll =
                (listState.canScrollForward || listState.canScrollBackward) && listState.layoutInfo.totalItemsCount > 0
        }

        Box {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .selectableGroup()
                    .fillMaxWidth()
            ) {
                itemsIndexed(themeOptions) { index, theme ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .selectable(
                                selected = (index == currentThemeIndex),
                                onClick = { onThemeSelected(index) },
                                role = Role.RadioButton
                            )
                        ,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (index == currentThemeIndex),
                            onClick = null
                        )
                        Text(
                            text = theme.title,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = LargerPadding)
                        )
                    }
                }
            }

            if (canScroll) {
                HorizontalDivider(modifier = Modifier.align(Alignment.TopCenter))
                HorizontalDivider(modifier = Modifier.align(Alignment.BottomCenter))
            }
        }
    }
}
