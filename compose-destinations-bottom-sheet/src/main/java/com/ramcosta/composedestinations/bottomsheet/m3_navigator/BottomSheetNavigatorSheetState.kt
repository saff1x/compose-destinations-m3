package com.ramcosta.composedestinations.bottomsheet.m3_navigator

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.util.fastForEach
import androidx.navigation.FloatingWindow
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.NavigatorState
import androidx.navigation.compose.LocalOwnersProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch

/**
 * The state of a [ModalBottomSheetLayout] that the [BottomSheetNavigator] drives
 *
 * @param sheetState The sheet state that is driven by the [BottomSheetNavigator]
 */
@OptIn(ExperimentalMaterial3Api::class)
public class BottomSheetNavigatorSheetState  constructor(private val sheetState: SheetState) {
    /**
     * @see SheetState.isVisible
     */
    public val isVisible: Boolean
        get() = sheetState.isVisible

    /**
     * @see SheetState.currentValue
     */
    public val currentValue: SheetValue
        get() = sheetState.currentValue

    /**
     * @see SheetState.targetValue
     */
    public val targetValue: SheetValue
        get() = sheetState.targetValue
}

/**
 * Create and remember a [BottomSheetNavigator]
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun rememberM3BottomSheetNavigator(
    skipPartiallyExpanded: Boolean = false,
    confirmValueChange: (SheetValue) -> Boolean = { true },
): M3BottomSheetNavigator {

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded,
        confirmValueChange = confirmValueChange
    )

    return remember(sheetState) { M3BottomSheetNavigator(sheetState) }
}

/**
 * Navigator that drives a [ModalBottomSheetState] for use of [ModalBottomSheetLayout]s
 * with the navigation library. Every destination using this Navigator must set a valid
 * [Composable] by setting it directly on an instantiated [Destination] or calling
 * [navigation.bottomSheet].
 *
 * <b>The [sheetInitializer] [Composable] will always host the latest entry of the back stack. When
 * navigating from a [BottomSheetNavigator.Destination] to another
 * [BottomSheetNavigator.Destination], the content of the sheet will be replaced instead of a
 * new bottom sheet being shown.</b>
 *
 * When the sheet is dismissed by the user, the [state]'s [NavigatorState.backStack] will be popped.
 *
 * The primary constructor is not intended for public use. Please refer to
 * [rememberBottomSheetNavigator] instead.
 *
 * @param sheetState The [ModalBottomSheetState] that the [BottomSheetNavigator] will use to
 * drive the sheet state
 */
@OptIn(ExperimentalMaterial3Api::class)
@Navigator.Name("bottomSheet")
public class M3BottomSheetNavigator(
    internal val sheetState: SheetState
) : Navigator<M3BottomSheetNavigator.Destination>() {

    internal var sheetEnabled by mutableStateOf(false)
        private set

    private var attached by mutableStateOf(false)


    /**
     * Get the back stack from the [state]. In some cases, the [sheetInitializer] might be composed
     * before the Navigator is attached, so we specifically return an empty flow if we aren't
     * attached yet.
     */
    private val backStack: StateFlow<List<NavBackStackEntry>>
        get() = if (attached) {
            state.backStack
        } else {
            MutableStateFlow(emptyList())
        }

    /**
     * Get the transitionsInProgress from the [state]. In some cases, the [sheetInitializer] might be
     * composed before the Navigator is attached, so we specifically return an empty flow if we
     * aren't attached yet.
     */
    private val transitionsInProgress: StateFlow<Set<NavBackStackEntry>>
        get() = if (attached) {
            state.transitionsInProgress
        } else {
            MutableStateFlow(emptySet())
        }

    /**
     * Access properties of the [ModalBottomSheetLayout]'s [ModalBottomSheetState]
     */
    public val navigatorSheetState: BottomSheetNavigatorSheetState =
        BottomSheetNavigatorSheetState(sheetState)

    /**
     * A [Composable] function that hosts the current sheet content. This should be set as
     * sheetContent of your [ModalBottomSheetLayout].
     */

    internal var sheetContent: @Composable ColumnScope.() -> Unit = {}
    internal var onDismissRequest: () -> Unit = {}

    private var animateToDismiss: () -> Unit = {}
    public var hideForNavigate: suspend () -> Unit = {}
        private set


    internal val sheetInitializer: @Composable () -> Unit = {
        val saveableStateHolder = rememberSaveableStateHolder()
        val transitionsInProgressEntries by transitionsInProgress.collectAsState()


        // The latest back stack entry, retained until the sheet is completely hidden
        // While the back stack is updated immediately, we might still be hiding the sheet, so
        // we keep the entry around until the sheet is hidden
        val retainedEntry by produceState<NavBackStackEntry?>(
            initialValue = null,
            key1 = backStack
        ) {
            backStack
                .transform { backStackEntries ->
                    // Always hide the sheet when the back stack is updated
                    // Regardless of whether we're popping or pushing, we always want to hide
                    // the sheet first before deciding whether to re-show it or keep it hidden
                    try {
                        sheetEnabled = false
                    } catch (_: CancellationException) {
                        // We catch but ignore possible cancellation exceptions as we don't want
                        // them to bubble up and cancel the whole produceState coroutine
                    } finally {
                        emit(backStackEntries.lastOrNull())
                    }
                }
                .collect {
                    value = it
                }
        }

        if (retainedEntry != null) {
            val currentOnSheetShown by rememberUpdatedState {
                transitionsInProgressEntries.forEach(state::markTransitionComplete)
            }
            LaunchedEffect(sheetState, retainedEntry) {
                snapshotFlow { sheetState.isVisible }
                    // We are only interested in changes in the sheet's visibility
                    .distinctUntilChanged()
                    // distinctUntilChanged emits the initial value which we don't need
                    .drop(1)
                    .collect { visible ->
                        if (visible) {
                            currentOnSheetShown()
                        }
                    }
            }

            val scope = rememberCoroutineScope()

            LaunchedEffect(key1 = retainedEntry) {
                sheetEnabled = true

                sheetContent = {
                    retainedEntry?.let { retainedEntry ->
                        retainedEntry.LocalOwnersProvider(saveableStateHolder) {
                            val content =
                                (retainedEntry.destination as Destination).content
                            content(retainedEntry)
                        }
                    }

                }
                onDismissRequest = {
                    sheetEnabled = false
                    retainedEntry?.let {
                        state.pop(popUpTo = it, saveState = false)
                    }
                }

                animateToDismiss = {
                    scope
                        .launch { sheetState.hide() }
                        .invokeOnCompletion {
                            onDismissRequest()
                        }
                }

                hideForNavigate = {
                    sheetState.hide()
                    onDismissRequest()
                }
            }

            BackHandler {
                animateToDismiss()
            }

        } else {
            LaunchedEffect(key1 = Unit) {
                sheetContent = {}
                onDismissRequest = {}
                animateToDismiss = {}
                hideForNavigate = {}
            }
        }


    }

    override fun onAttach(state: NavigatorState) {
        super.onAttach(state)
        attached = true
    }

    override fun createDestination(): Destination = Destination(
        navigator = this,
        content = {}
    )

    override fun navigate(
        entries: List<NavBackStackEntry>,
        navOptions: NavOptions?,
        navigatorExtras: Extras?
    ) {
        onDismissRequest()
        entries.fastForEach { entry ->
            state.push(entry)
        }
    }

    override fun popBackStack(popUpTo: NavBackStackEntry, savedState: Boolean) {
        if (backStack.value.lastOrNull() == popUpTo && sheetState.isVisible) {
            animateToDismiss() // plays sheetState.hide(), waits, then pops
        } else {
            state.pop(popUpTo, savedState) // normal pop for deeper entries
        }
    }

    /**
     * [NavDestination] specific to [BottomSheetNavigator]
     */
    @NavDestination.ClassType(Composable::class)
    public class Destination(
        navigator: M3BottomSheetNavigator,
        internal val content: @Composable ColumnScope.(NavBackStackEntry) -> Unit
    ) : NavDestination(navigator), FloatingWindow
}