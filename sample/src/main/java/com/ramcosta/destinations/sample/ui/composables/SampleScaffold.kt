package com.ramcosta.destinations.sample.ui.composables

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Scaffold
import androidx.compose.material.navigation.ModalBottomSheetLayout
import androidx.compose.material.navigation.rememberBottomSheetNavigator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.plusAssign
import com.ramcosta.composedestinations.bottomsheet.m3_navigator.M3ModalBottomSheetLayout
import com.ramcosta.composedestinations.bottomsheet.m3_navigator.rememberM3BottomSheetNavigator
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.utils.currentDestinationAsState
import com.ramcosta.composedestinations.utils.startDestination

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("RestrictedApi")
@Composable
fun SampleScaffold(
    navController: NavHostController,
    topBar: @Composable (DestinationSpec, NavBackStackEntry?) -> Unit,
    bottomBar: @Composable (DestinationSpec) -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    val destination = navController.currentDestinationAsState().value
        ?: NavGraphs.root.startDestination
    val navBackStackEntry = navController.currentBackStackEntry

    // 👇 only for debugging, you shouldn't use currentBackStack API as it is restricted by annotation
    navController.currentBackStack.collectAsState().value.print()

    val bottomSheetNavigator = rememberM3BottomSheetNavigator(navController)
    navController.navigatorProvider += bottomSheetNavigator

    // 👇 ModalBottomSheetLayout is only needed if some destination is bottom sheet styled
    M3ModalBottomSheetLayout(
        bottomSheetNavigator = bottomSheetNavigator
    ) {
        Scaffold(
            topBar = { topBar(destination, navBackStackEntry) },
            bottomBar = { bottomBar(destination) },
            content = content
        )
    }
}

private fun Collection<NavBackStackEntry>.print(prefix: String = "stack") {
    val stack = map { it.destination.route }.toTypedArray().contentToString()
    println("$prefix = $stack")
}
