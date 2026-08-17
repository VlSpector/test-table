package com.softspector.testtable.presentation.sizeselection

import com.softspector.testtable.MainDispatcherRule
import com.softspector.testtable.presentation.table.TableRoute
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SizeSelectionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val viewModel = SizeSelectionViewModel()

    @Test
    fun `initial state has defaults and confirm enabled`() {
        val state = viewModel.state.value

        assertEquals(SizeSelectionViewModel.DEFAULT_ROWS.toString(), state.rowsText)
        assertEquals(SizeSelectionViewModel.DEFAULT_COLUMNS.toString(), state.columnsText)
        assertTrue(state.isConfirmEnabled)
    }

    @Test
    fun `rows text strips non-digit characters`() {
        viewModel.onRowsTextChanged("1a2b3")

        assertEquals("123", viewModel.state.value.rowsText)
    }

    @Test
    fun `rows text is clamped to MAX_ROWS`() {
        viewModel.onRowsTextChanged("999999")

        assertEquals(SizeSelectionViewModel.MAX_ROWS.toString(), viewModel.state.value.rowsText)
    }

    @Test
    fun `columns text is clamped to MAX_COLUMNS`() {
        viewModel.onColumnsTextChanged("99")

        assertEquals(
            SizeSelectionViewModel.MAX_COLUMNS.toString(),
            viewModel.state.value.columnsText
        )
    }

    @Test
    fun `text can be cleared to empty instead of snapping to a default`() {
        viewModel.onRowsTextChanged("")

        assertEquals("", viewModel.state.value.rowsText)
    }

    @Test
    fun `confirm is disabled when rows text is empty`() {
        viewModel.onRowsTextChanged("")

        assertFalse(viewModel.state.value.isConfirmEnabled)
    }

    @Test
    fun `confirm is disabled when columns text is empty`() {
        viewModel.onColumnsTextChanged("")

        assertFalse(viewModel.state.value.isConfirmEnabled)
    }

    @Test
    fun `confirm re-enables once both fields are non-empty again`() {
        viewModel.onRowsTextChanged("")
        viewModel.onRowsTextChanged("5")

        assertTrue(viewModel.state.value.isConfirmEnabled)
    }

    @Test
    fun `confirming navigates to a table with the entered rows and columns`() =
        runTest(mainDispatcherRule.testDispatcher) {
            viewModel.onRowsTextChanged("12")
            viewModel.onColumnsTextChanged("5")
            val results = mutableListOf<TableRoute>()
            backgroundScope.launch { results.add(viewModel.navigateToTableEvent.first()) }
            runCurrent()

            viewModel.onConfirmClicked()
            advanceUntilIdle()

            assertEquals(listOf(TableRoute(rows = 12, columns = 5)), results)
        }
}
