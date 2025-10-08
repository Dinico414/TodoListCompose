package com.xenonware.todolist.ui.layouts

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext

@Composable
fun ShowScreenWidthToast() {
    val configuration = LocalConfiguration.current
    val context = LocalContext.current
    val screenWidthDp = configuration.screenWidthDp

    LaunchedEffect(screenWidthDp) {
        Toast.makeText(context, "Screen Width: $screenWidthDp dp", Toast.LENGTH_LONG).show()
    }
}