package com.ramcosta.samples.playground.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.bottomsheet.spec.DestinationStyleBottomSheet
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.ResultBackNavigator
import com.ramcosta.samples.playground.commons.SettingsGraph
import com.ramcosta.samples.playground.commons.requireTitle
import com.ramcosta.samples.playground.ui.screens.destinations.TestDestination
import com.ramcosta.samples.playground.ui.screens.destinations.ThemeSettingsDestination
import com.ramcosta.samples.playground.ui.screens.profile.SerializableExampleWithNavTypeSerializer

@Destination<SettingsGraph>(style = DestinationStyleBottomSheet::class)
@Composable
fun ColumnScope.ThemeSettings(
    viewModel: SettingsViewModel,
    navigator: DestinationsNavigator,
    resultNavigator: ResultBackNavigator<SerializableExampleWithNavTypeSerializer>
) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(300.dp)
            .background(Color.Yellow)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Switch(checked = viewModel.isToggleOn, onCheckedChange = { viewModel.toggle() })

            Text(
                text = stringResource(id = ThemeSettingsDestination.requireTitle),
            )

            Button(
                onClick = {
                    resultNavigator.navigateBack(
                        result = SerializableExampleWithNavTypeSerializer("RESULT!!", "THING2")
                    )
                }
            ) {
                Text("Go back with result")
            }
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    navigator.navigateUp()
                }
            ) {
                Text("Go back")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    navigator.navigate(TestDestination)
                }
            ) {
                Text("Test Navigate")
            }
        }
    }
}