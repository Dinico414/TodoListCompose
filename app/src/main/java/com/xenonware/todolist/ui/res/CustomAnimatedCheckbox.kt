package com.xenonware.todolist.ui.res

import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.xenon.todolist.R
import com.xenonware.todolist.ui.values.SmallButtonSize

@OptIn(ExperimentalAnimationGraphicsApi::class)
@Composable
fun CustomAnimatedCheckbox(
    checked: Boolean,
    onCheckedChange: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {

//    val avdCheck = AnimatedImageVector.animatedVectorResource(R.drawable.unchecking)
    val avdUncheck = AnimatedImageVector.animatedVectorResource(R.drawable.checking)

    val displayPainter = if (LocalInspectionMode.current) {
        if (checked) painterResource(R.drawable.checked)
        else painterResource(R.drawable.unchecked)
    } else {
        rememberAnimatedVectorPainter(
            animatedImageVector = avdUncheck,
            atEnd = checked
        )
    }


    Icon(
        painter = displayPainter,
        contentDescription = if (checked) stringResource(R.string.yes)
            else stringResource(R.string.no),
        tint = if (checked) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            },
        modifier = modifier
            .size(SmallButtonSize)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = { onCheckedChange() }
            )
    )
}