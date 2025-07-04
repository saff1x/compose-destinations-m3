package com.ramcosta.composedestinations.bottomsheet.m3_navigator

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetDefaults
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun M3ModalBottomSheetLayout(
    bottomSheetNavigator: M3BottomSheetNavigator,
    modifier: Modifier = Modifier,
    sheetModifier: Modifier = Modifier,
    sheetMaxWidth: Dp = BottomSheetDefaults.SheetMaxWidth,
    shape: Shape = BottomSheetDefaults.ExpandedShape,
    containerColor: Color = BottomSheetDefaults.ContainerColor,
    contentColor: Color = contentColorFor(containerColor),
    tonalElevation: Dp = BottomSheetDefaults.Elevation,
    scrimColor: Color = BottomSheetDefaults.ScrimColor,
    dragHandle: @Composable (() -> Unit)? = { BottomSheetDefaults.DragHandle() },
    contentWindowInsets: @Composable () -> WindowInsets = { BottomSheetDefaults.windowInsets },
    properties: ModalBottomSheetProperties = ModalBottomSheetDefaults.properties,
    content: @Composable () -> Unit,
) {

    Column(modifier = modifier) {
        bottomSheetNavigator.sheetInitializer()
        content()
    }


    val context = LocalContext.current

    if (bottomSheetNavigator.sheetEnabled) {
        ModalBottomSheet(
            onDismissRequest = bottomSheetNavigator.onDismissRequest,
            modifier = sheetModifier,
            sheetState = bottomSheetNavigator.sheetState,
            content = {
                CompositionLocalProvider(
                    LocalContext provides context
                ) {
                    bottomSheetNavigator.sheetContent(this)
                }
            },
            sheetMaxWidth = sheetMaxWidth,
            shape = shape,
            containerColor = containerColor,
            contentColor = contentColor,
            tonalElevation = tonalElevation,
            scrimColor = scrimColor,
            dragHandle = dragHandle,
            contentWindowInsets = contentWindowInsets,
            properties = properties,
        )
    }
}