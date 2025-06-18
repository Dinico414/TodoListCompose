package com.xenon.todolist.ui.res

import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import com.xenon.todolist.R // Make sure this R import is correct for your project structure

@OptIn(ExperimentalAnimationGraphicsApi::class)
@Composable
fun CustomAnimatedCheckbox(
    checked: Boolean,
    onCheckedChange: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    // Ensure your drawable resources R.drawable.checked and R.drawable.unchecked
    // are correctly defined AnimatedVectorDrawables for the check and uncheck states.
    val avdCheck = AnimatedImageVector.animatedVectorResource(R.drawable.unchecked)
    val avdUncheck = AnimatedImageVector.animatedVectorResource(R.drawable.checked)

    // This state controls the animation progression.
    // It's keyed to 'checked' to reset the animation if 'checked' changes externally.
    var atEnd by remember(checked) { mutableStateOf(checked) }

    val painter = rememberAnimatedVectorPainter(
        animatedImageVector = if (checked) avdCheck else avdUncheck,
        atEnd = atEnd
    )

    // This LaunchedEffect triggers the animation when the 'checked' state changes.
    LaunchedEffect(checked) {
        atEnd = false // Start the animation by setting atEnd to false
        // The painter will animate from its current state to the start of the new AVD,
        // then to its end.
        // We then immediately set atEnd to the target 'checked' state.
        // This ensures that if recomposition occurs during the animation,
        // the painter is correctly initialized to continue towards the final state.
        // For AVDs, 'atEnd = true' means show the end state, 'atEnd = false' means show the start.
        // When 'checked' changes, we want to play the *entire* animation.
        // The rememberAnimatedVectorPainter will handle animating to the new vector.
        // Setting atEnd = true/false immediately would jump to that frame.
        // The key is that 'animatedImageVector' changes, and the painter animates that change.
        // The 'atEnd' in rememberAnimatedVectorPainter is more about its *initial* state.
        // However, to ensure the animation plays from the beginning of the *new* AVD,
        // we can toggle atEnd.
        // Let's refine the LaunchedEffect slightly for clarity on animation triggering.
        // When 'checked' changes, the 'animatedImageVector' in 'rememberAnimatedVectorPainter'
        // will change. The 'rememberAnimatedVectorPainter' should handle the transition.
        // The 'atEnd' parameter for 'rememberAnimatedVectorPainter' defines if the AVD is initially
        // at its start or end. If 'checked' changes, we want to ensure the animation plays.

        // Re-evaluating the animation trigger:
        // When 'checked' flips, `animatedImageVector` changes.
        // `rememberAnimatedVectorPainter` will start drawing the new `animatedImageVector`.
        // To ensure it plays from its defined start:
        if (atEnd != checked) { // If the visual state doesn't match the logical state
            atEnd = !checked // Temporarily set to opposite to ensure animation plays from start
            atEnd = checked  // Then set to the correct end state for the new vector
        }
    }

    // For preview in IDE, show static versions of the drawables.
    val displayPainter = if (LocalInspectionMode.current) {
        if (checked) painterResource(R.drawable.unchecked) // Replace with static preview
        else painterResource(R.drawable.checked)   // Replace with static preview
    } else {
        painter
    }

    Icon(
        painter = displayPainter,
        contentDescription = if (checked) stringResource(R.string.yes) // Ensure R.string.yes exists
        else stringResource(R.string.no), // Ensure R.string.no exists
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null, // No visual ripple or other indication on click
                enabled = enabled,
                onClick = {
                    onCheckedChange() // Call the provided lambda to toggle the state
                }
            ),
        tint = if (checked) {
            MaterialTheme.colorScheme.primary // Color when checked (e.g., your app's main accent color)
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) // Color when unchecked (less prominent)
        }
    )
}