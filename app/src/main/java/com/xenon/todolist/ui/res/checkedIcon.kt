//package com.xenon.todolist.ui.res
//
//import androidx.compose.animation.animateColor
//import androidx.compose.animation.animateColorAsState
//import androidx.compose.animation.core.*
//import androidx.compose.foundation.Canvas
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.layout.size
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.CheckCircle // Example static icon
//import androidx.compose.runtime.*
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.scale
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.Path
//import androidx.compose.ui.graphics.SolidColor
//import androidx.compose.ui.graphics.StrokeCap
//import androidx.compose.ui.graphics.drawscope.Stroke
//import androidx.compose.ui.graphics.vector.ImageVector
//import androidx.compose.ui.graphics.vector.PathBuilder
//import androidx.compose.ui.graphics.vector.path
//import androidx.compose.ui.graphics.vector.rememberVectorPainter
//import androidx.compose.ui.graphics.vector.toPath
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import kotlin.io.path.moveTo
//
//// --- This is where defining your specific paths from XML would go ---
//// For your complex XML, this would be very long and involve manually
//// translating each path command.
//val myCustomStaticVector = ImageVector.Builder(
//    name = "MyCustomVector",
//    defaultWidth = 26.dp,
//    defaultHeight = 26.dp,
//    viewportWidth = 7f,
//    viewportHeight = 7f
//).apply {
//    path(
//        fill = SolidColor(Color.White), // Initial fill color
//        fillAlpha = 1f,
//        stroke = null,
//        strokeAlpha = 1f,
//        strokeLineWidth = 1f,
//        strokeLineCap = StrokeCap.Butt,
//        strokeLineJoin = Stroke.miter,
//        strokeLineMiter = 1f,
//        pathFillType = androidx.compose.ui.graphics.PathFillType.EvenOdd // Mapped from fillType
//    ) {
//        // Manually translate your initial pathData here
//        // Example: M 3.439 0.264 C 2.598 0.264 ... Z
//        // This is a simplified representation. You'd use moveTo, cubicTo, etc.
//        // For example (VERY simplified and not your actual path):
//        moveTo(3.439f, 0.264f)
//        cubicTo(2.598f, 0.264f, 1.789f, 0.599f, 1.194f, 1.194f)
//        // ... and so on for the entire path data
//        close()
//    }
//}.build()
//// --- End of vector definition ---
//
//
//@Composable
//fun PureKotlinAnimatedVectorExample() {
//    var animate by remember { mutableStateOf(false) }
//
//    // 1. Define target values for animation
//    val targetFillColor = if (animate) Color.Green else Color.White // Example: Animate fill color
//
//    // 2. Use Compose animation primitives
//    val animatedFillColor by animateColorAsState(
//        targetValue = targetFillColor,
//        animationSpec = tween(durationMillis = 500) // Corresponds to your objectAnimator duration
//    )
//
//    // 3. Draw using Canvas or ImageVector painter
//    // Here we'll redraw the vector with the animated property
//    // For pathData animation, this is where it gets extremely complex,
//    // as you'd need to interpolate between pathData strings or PathNode lists.
//
//    Canvas(
//        modifier = Modifier
//            .size(100.dp)
//            .clickable { animate = !animate }
//    ) {
//        // THIS IS A MAJOR SIMPLIFICATION.
//        // For your actual XML, you'd need to:
//        // 1. Parse all your <objectAnimator> elements.
//        // 2. For each 'pathData' animation, have Path objects for 'valueFrom' and 'valueTo'.
//        // 3. Implement or use a library for path morphing between these Path objects
//        //    driven by an animation progress (0f to 1f).
//        // 4. Handle 'startOffset' and 'interpolator'.
//
//        // Example: Drawing a static path with an animated fill
//        // (Not what your XML does, which is path MORPHING)
//        val path = PathBuilder().apply {
//            // Re-create the path that needs to be animated.
//            // This would be one of the states from your XML.
//            // Example for the *initial* pathData:
//            moveTo(3.439f, 0.264f)
//            cubicTo(2.598f, 0.264f, 1.789f, 0.599f, 1.194f, 1.194f)
//            cubicTo(0.599f, 1.79f, 0.264f, 2.598f, 0.264f, 3.439f)
//            cubicTo(0.264f, 4.281f, 0.599f, 5.089f, 1.194f, 5.684f)
//            cubicTo(1.789f, 6.28f, 2.598f, 6.614f, 3.439f, 6.614f)
//            cubicTo(4.281f, 6.614f, 5.089f, 6.28f, 5.684f, 5.684f)
//            cubicTo(6.28f, 5.089f, 6.614f, 4.281f, 6.614f, 3.439f)
//            cubicTo(6.614f, 2.598f, 6.28f, 1.79f, 5.684f, 1.194f)
//            cubicTo(5.089f, 0.599f, 4.281f, 0.264f, 3.439f, 0.264f)
//            close()
//            moveTo(3.439f, 0.794f)
//            cubicTo(3.904f, 0.794f, 4.36f, 0.916f, 4.762f, 1.149f)
//            cubicTo(5.164f, 1.381f, 5.499f, 1.715f, 5.731f, 2.117f)
//            cubicTo(5.963f, 2.519f, 6.085f, 2.975f, 6.085f, 3.44f)
//            cubicTo(6.085f, 3.904f, 5.963f, 4.361f, 5.731f, 4.763f)
//            cubicTo(5.499f, 5.165f, 5.164f, 5.499f, 4.762f, 5.731f)
//            cubicTo(4.36f, 5.964f, 3.904f, 6.086f, 3.439f, 6.086f)
//            cubicTo(2.975f, 6.086f, 2.519f, 5.964f, 2.116f, 5.731f)
//            cubicTo(1.714f, 5.499f, 1.38f, 5.165f, 1.148f, 4.763f)
//            cubicTo(0.916f, 4.361f, 0.793f, 3.904f, 0.793f, 3.44f)
//            cubicTo(0.793f, 2.975f, 0.916f, 2.519f, 1.148f, 2.117f)
//            cubicTo(1.38f, 1.715f, 1.714f, 1.381f, 2.116f, 1.149f)
//            cubicTo(2.519f, 0.916f, 2.975f, 0.794f, 3.439f, 0.794f)
//            close()
//        }.getNodes().toPath() // Convert List<PathNode> to Path
//
//        // Scale the path to fit the canvas if viewport and canvas size differ
//        val pathSize = path.getBounds().size
//        val canvasSize = this.size
//        val scaleX = canvasSize.width / 7f // 7f is viewportWidth
//        val scaleY = canvasSize.height / 7f // 7f is viewportHeight
//
//        scale(scaleX, scaleY, Offset.Zero) {
//            drawPath(
//                path = path,
//                color = animatedFillColor, // Using the animated color
//                style = androidx.compose.ui.graphics.drawscope.Fill // Or Stroke
//            )
//        }
//    }
//}
//
//
//@Preview(showBackground = true)
//@Composable
//fun PureKotlinAnimatedVectorPreview() {
//    Column(
//        modifier = Modifier.fillMaxSize(),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        PureKotlinAnimatedVectorExample()
//    }
//}