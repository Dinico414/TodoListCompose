@file:Suppress("KotlinConstantConditions")

package com.xenon.todolist.ui.res // Ensure this package matches

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.xenon.todolist.R
import com.xenon.todolist.ui.values.SmallButtonSize
// import com.xenon.todolist.ui.values.SmallElevation // This import was present but not used

@OptIn(ExperimentalAnimationGraphicsApi::class)
@Composable
fun CustomAnimatedCheckbox(
    checked: Boolean,
    onCheckedChange: () -> Unit, // This signature is () -> Unit
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {

    val avdCheck = AnimatedImageVector.animatedVectorResource(R.drawable.unchecking)
    val avdUncheck = AnimatedImageVector.animatedVectorResource(R.drawable.checking)

    var atEnd by remember(checked) { mutableStateOf(checked) }

    val painter = rememberAnimatedVectorPainter(
        animatedImageVector = if (checked) avdCheck else avdUncheck, atEnd = atEnd
    )

    LaunchedEffect(checked) {
        // This logic seems a bit complex for just triggering the animation.
        // Typically, you might just want to set atEnd to false to start the animation
        // and let the rememberAnimatedVectorPainter handle playing it to its new 'atEnd' state
        // based on the 'checked' state.
        // Consider simplifying if it causes issues.
        // For instance, you might not need to toggle atEnd twice.
        // A simpler version:
        // if (atEnd != checked) { // Or just always restart if 'checked' changes
        //     atEnd = false // Start animation
        // }
        // The remember(checked) for atEnd and rememberAnimatedVectorPainter
        // should handle updating the painter correctly when 'checked' changes.
        // The LaunchedEffect is to *trigger* the animation if it's not playing automatically.

        // Current logic:
        atEnd = false // Start animation
        if (atEnd != checked) { // This condition will always be true now
            atEnd = !checked // This seems to immediately set it to the opposite of the target
            atEnd = checked  // And then to the target. This might work but could be simplified.
        }
    }

    val displayPainter = if (LocalInspectionMode.current) {
        if (checked) painterResource(R.drawable.checked)
        else painterResource(R.drawable.unchecked)
    } else {
        painter
    }

    Icon(
        painter = displayPainter, contentDescription = if (checked) stringResource(R.string.yes)
        else stringResource(R.string.no), modifier = modifier
            .size(SmallButtonSize)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = {
                    onCheckedChange() // This calls the () -> Unit lambda
                }), tint = if (checked) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        })
}