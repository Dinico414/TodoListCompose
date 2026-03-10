package com.xenonware.todolist.ui.res

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xenon.mylibrary.theme.QuicksandTitleVariable

object CustomSnackbarDefault {
    val backgroundColor: Color @Composable get() = MaterialTheme.colorScheme.inverseSurface
    val contentColor: Color @Composable get() = MaterialTheme.colorScheme.inverseOnSurface
    val actionContainerColor: Color @Composable get() = MaterialTheme.colorScheme.inversePrimary
    val actionContentColor: Color @Composable get() = MaterialTheme.colorScheme.onPrimaryContainer
    val startPadding: Dp = 16.dp
    val endPadding: Dp = 8.dp
    val textStyle: TextStyle @Composable get() = TextStyle(
        fontFamily = QuicksandTitleVariable,
        fontWeight = FontWeight.Thin,
        fontSize = 14.sp
    )
    val actionTextStyle: TextStyle @Composable get() = TextStyle(
        fontFamily = QuicksandTitleVariable,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    )
}

@Suppress("unused")
@Composable
fun CustomSnackbar(
    snackbarData: SnackbarData,
    modifier: Modifier = Modifier,
    backgroundColor: Color = CustomSnackbarDefault.backgroundColor,
    contentColor: Color = CustomSnackbarDefault.contentColor,
    actionContainerColor: Color = CustomSnackbarDefault.actionContainerColor,
    actionContentColor: Color = CustomSnackbarDefault.actionContentColor,
    contentTextStyle: TextStyle = CustomSnackbarDefault.textStyle,
    actionTextStyle: TextStyle = CustomSnackbarDefault.actionTextStyle,
    padding: Dp = CustomSnackbarDefault.startPadding,
    actionPadding: Dp = CustomSnackbarDefault.endPadding
) {
    Row(
        modifier = modifier
            .height(48.dp)
            .fillMaxWidth()
            .clip(CircleShape)
            .background(backgroundColor),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = snackbarData.visuals.message,
            style = contentTextStyle,
            color = contentColor,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = padding),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        snackbarData.visuals.actionLabel?.let { actionLabel ->
            FilledTonalButton(
                onClick = { snackbarData.performAction() },
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = actionContainerColor,
                    contentColor = actionContentColor
                ),
                modifier = Modifier.padding(end = actionPadding)
            ) {
                Text(
                    text = actionLabel,
                    style = actionTextStyle
                )
            }
        }
    }
}
