//package com.xenon.todolist.ui.res // Adjust as needed
//
//import androidx.compose.animation.animateColor
//import androidx.compose.animation.core.*
//import androidx.compose.foundation.Canvas
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.Text
//import androidx.compose.runtime.*
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.geometry.CornerRadius
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.geometry.Size
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.Path
//import androidx.compose.ui.graphics.drawscope.Fill
//import androidx.compose.ui.graphics.vector.PathBuilder
//import androidx.compose.ui.graphics.vector.toPath
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//
//// Represents the states of our simplified animation
//enum class ShapeState {
//    SQUARE,
//    CIRCLE
//}
//
//@Composable
//fun PureKotlinAnimatedShape(
//    modifier: Modifier = Modifier,
//    targetState: ShapeState,
//    size: Float = 100f, // Size of the shape in dp
//) {
//    val transition = updateTransition(targetState = targetState, label = "ShapeMorph")
//
//    // 1. Animate Color
//    val animatedColor by transition.animateColor(
//        label = "ColorAnimation",
//        transitionSpec = {
//            tween(durationMillis = 500, easing = LinearEasing)
//        }
//    ) { state ->
//        when (state) {
//            ShapeState.SQUARE -> Color.Blue
//            ShapeState.CIRCLE -> Color.Red
//        }
//    }
//
//    // 2. Animate Corner Radius (to morph square to circle)
//    val animatedCornerRadius by transition.animateFloat(
//        label = "CornerRadiusAnimation",
//        transitionSpec = {
//            tween(durationMillis = 500, easing = FastOutSlowInEasing)
//        }
//    ) { state ->
//        when (state) {
//            ShapeState.SQUARE -> 0f
//            ShapeState.CIRCLE -> size / 2f // For a circle, corner radius is half the size
//        }
//    }
//
//    Canvas(modifier = modifier.size(size.dp)) {
//        drawRoundRect(
//            color = animatedColor,
//            topLeft = Offset.Zero,
//            size = Size(this.size.width, this.size.height),
//            cornerRadius = CornerRadius(animatedCornerRadius, animatedCornerRadius),
//            style = Fill
//        )
//    }
//}
//
//
//// --- Attempting a VERY basic path morph (conceptually) ---
//// This is still massively simpler than your XML and assumes compatible paths.
//
//// Define Path objects for two states (EXTREMELY simplified)
//// In reality, these would come from your complex pathData strings.
//val pathSquare = PathBuilder().apply {
//    moveTo(10f, 10f)
//    lineTo(90f, 10f)
//    lineTo(90f, 90f)
//    lineTo(10f, 90f)
//    close()
//}.getNodes().toPath() // Convert List<PathNode> to a Path object
//
//val pathCircleApproximation = PathBuilder().apply { // A very rough circle with 4 points
//    moveTo(50f, 10f) // Top
//    cubicTo(77f, 10f, 90f, 23f, 90f, 50f) // Right
//    cubicTo(90f, 77f, 77f, 90f, 50f, 90f) // Bottom
//    cubicTo(23f, 90f, 10f, 77f, 10f, 50f) // Left
//    cubicTo(10f, 23f, 23f, 10f, 50f, 10f) // Back to Top
//    close()
//}.getNodes().toPath()
//
//
//// !! THIS IS A PLACEHOLDER FOR A REAL PATH MORPHING FUNCTION !!
//// A real one would be incredibly complex.
//fun lerpPath(start: Path, end: Path, fraction: Float, target: Path): Boolean {
//    // This is a naive and INCORRECT way to "interpolate" paths for demonstration.
//    // It does NOT produce a smooth visual morph for arbitrary paths.
//    // It assumes paths have the same number and type of commands, which is rarely true.
//    // A proper implementation is a significant graphics algorithm.
//
//    // For this placeholder, let's just switch:
//    target.reset() // Clear the target path
//    if (fraction < 0.5f) {
//        target.addPath(start)
//    } else {
//        target.addPath(end)
//    }
//    return true // Indicates success (in a real system)
//}
//
//
//@Composable
//fun PureKotlinPathMorphAnimation(
//    modifier: Modifier = Modifier,
//    targetState: ShapeState,
//) {
//    val transition = updateTransition(targetState = targetState, label = "PathMorph")
//
//    val morphFraction by transition.animateFloat(
//        label = "MorphFraction",
//        transitionSpec = { tween(durationMillis = 1000, easing = LinearEasing) }
//    ) { state ->
//        if (state == ShapeState.CIRCLE) 1f else 0f
//    }
//
//    // Create a mutable path object that will be updated
//    val morphedPath = remember { Path() }
//
//    // This recomposes whenever morphFraction changes, updating the morphedPath
//    LaunchedEffect(morphFraction, targetState) {
//        // Call our (highly simplified and mostly incorrect) path interpolation
//        lerpPath(pathSquare, pathCircleApproximation, morphFraction, morphedPath)
//    }
//
//    Canvas(modifier = modifier.size(100.dp)) {
//        // Draw the currently morphed path
//        // You'd need to scale it to fit the viewport and canvas size properly here.
//        drawPath(
//            path = morphedPath,
//            color = if (targetState == ShapeState.SQUARE) Color.Green else Color.Magenta
//        )
//    }
//}
//
//
//@Preview(showBackground = true)
//@Composable
//fun PureKotlinAnimationPreview() {
//    var currentState by remember { mutableStateOf(ShapeState.SQUARE) }
//    Column(
//        Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//            .clickable {
//                currentState =
//                    if (currentState == ShapeState.SQUARE) ShapeState.CIRCLE else ShapeState.SQUARE
//            },
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.spacedBy(20.dp)
//    ) {
//        Text("Click to Animate (Simplified)")
//
//        Text("Shape Morph (RoundRect):")
//        PureKotlinAnimatedShape(targetState = currentState)
//
//        Text("Path Morph (Conceptual - VERY Basic):")
//        PureKotlinPathMorphAnimation(targetState = currentState)
//    }
//}