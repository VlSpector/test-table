package com.softspector.testtable.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.softspector.testtable.presentation.sizeselection.SizeSelectionRoute
import com.softspector.testtable.presentation.sizeselection.SizeSelectionScreen
import com.softspector.testtable.presentation.table.TableRoute
import com.softspector.testtable.presentation.table.TableScreen

@Composable
fun TestTableNavigation() {
    val backStack = rememberNavBackStack(SizeSelectionRoute)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<SizeSelectionRoute> {
                SizeSelectionScreen(
                    onNavigateToTable = { route -> backStack.add(route) },
                )
            }
            entry<TableRoute> { route ->
                TableScreen(
                    route = route,
                    onBackClick = { backStack.removeLastOrNull() },
                )
            }
        },
    )
}