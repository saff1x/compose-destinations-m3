package com.ramcosta.samples.playground.ui.screens.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.samples.playground.commons.SettingsGraph

@Destination<SettingsGraph>
@Composable
fun Test() {
    Box(modifier = Modifier.fillMaxSize()) {
        Text("Test Screen")
    }
}