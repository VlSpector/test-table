package com.softspector.testtable.presentation.table

import androidx.paging.PagingData
import com.softspector.testtable.MainDispatcherRule
import com.softspector.testtable.common.SuspendResult
import com.softspector.testtable.domain.repository.TableRowsRepository
import com.softspector.testtable.domain.model.TableCellPosition
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TableViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<TableRowsRepository> {
        every { getTableRowsStream() } returns flowOf(PagingData.empty())
    }
    private val repositoryFactory = mockk<TableRowsRepository.Factory> {
        every { create(columnsNumber = any(), limit = any()) } returns repository
    }

    private fun createViewModel(rows: Int = 4, columns: Int = 3) =
        TableViewModel(
            route = TableRoute(rows = rows, columns = columns),
            repositoryFactory = repositoryFactory,
        )

    @Test
    fun `initial state is derived from the route`() {
        val viewModel = createViewModel(rows = 10, columns = 6)

        assertEquals(TableState(), viewModel.state.value)
    }

    @Test
    fun `the repository is built for the table size from the route`() {
        createViewModel(rows = 10, columns = 6)

        verify(exactly = 1) { repositoryFactory.create(columnsNumber = 6, limit = 10) }
    }

    @Test
    fun `clicking a cell selects it`() {
        val viewModel = createViewModel()
        val cell = TableCell(position = TableCellPosition(row = 2, column = 1), value = "x")

        viewModel.onCellClick(cell)

        assertEquals(cell.position, viewModel.state.value.selectedCellPosition)
    }

    @Test
    fun `clicking the selected cell again deselects it`() {
        val viewModel = createViewModel()
        val cell = TableCell(position = TableCellPosition(row = 2, column = 1), value = "x")
        viewModel.onCellClick(cell)

        viewModel.onCellClick(cell)

        assertNull(viewModel.state.value.selectedCellPosition)
    }

    @Test
    fun `clicking a different cell replaces the selection`() {
        val viewModel = createViewModel()
        val firstCell = TableCell(position = TableCellPosition(row = 0, column = 0), value = "a")
        val secondCell = TableCell(position = TableCellPosition(row = 0, column = 1), value = "b")
        viewModel.onCellClick(firstCell)

        viewModel.onCellClick(secondCell)

        assertEquals(secondCell.position, viewModel.state.value.selectedCellPosition)
    }

    @Test
    fun `selection follows the cell position and ignores its value`() {
        val viewModel = createViewModel()
        val cell = TableCell(position = TableCellPosition(row = 2, column = 1), value = "old")
        viewModel.onCellClick(cell)

        // the same cell after an edit reloaded it with a new value
        viewModel.onCellClick(cell.copy(value = "new"))

        assertNull(viewModel.state.value.selectedCellPosition)
    }

    @Test
    fun `double clicking a cell opens the edit sheet for that cell`() {
        val viewModel = createViewModel()
        val cell = TableCell(position = TableCellPosition(row = 2, column = 1), value = "x")

        viewModel.onCellDoubleClick(cell)

        assertEquals(
            TableState.BottomSheetState(selectedCell = cell),
            viewModel.state.value.bottomSheetState,
        )
    }

    @Test
    fun `changing the edit sheet input updates inputValue`() {
        val viewModel = createViewModel()
        viewModel.onCellDoubleClick(TableCell(position = TableCellPosition(row = 2, column = 1), value = "x"))

        viewModel.onEditSheetInputChange("new value")

        assertEquals("new value", viewModel.state.value.bottomSheetState?.inputValue)
    }

    @Test
    fun `dismissing the edit sheet clears bottomSheetState`() {
        val viewModel = createViewModel()
        viewModel.onCellDoubleClick(TableCell(position = TableCellPosition(row = 0, column = 0), value = "x"))

        viewModel.onEditSheetDismiss()

        assertNull(viewModel.state.value.bottomSheetState)
    }

    @Test
    fun `saving with no open edit sheet does not call the repository`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel()

        viewModel.onEditCellSaveClick()
        advanceUntilIdle()

        coVerify(exactly = 0) { repository.editCell(any(), any()) }
    }

    @Test
    fun `saving edits the cell and asks the sheet to close on success`() = runTest(mainDispatcherRule.testDispatcher) {
        coEvery {
            repository.editCell(position = TableCellPosition(row = 2, column = 1), value = "new value")
        } returns SuspendResult.success(Unit)
        val viewModel = createViewModel()
        val closeRequests = collectCloseRequests(viewModel)
        viewModel.onCellDoubleClick(TableCell(position = TableCellPosition(row = 2, column = 1), value = "old"))
        viewModel.onEditSheetInputChange("new value")

        viewModel.onEditCellSaveClick()
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.editCell(position = TableCellPosition(row = 2, column = 1), value = "new value") }
        assertEquals(1, closeRequests.size)
    }

    @Test
    fun `saving asks the sheet to close even when the repository call fails`() = runTest(mainDispatcherRule.testDispatcher) {
        coEvery {
            repository.editCell(any(), any())
        } returns SuspendResult.failure(RuntimeException("boom"))
        val viewModel = createViewModel()
        val closeRequests = collectCloseRequests(viewModel)
        viewModel.onCellDoubleClick(TableCell(position = TableCellPosition(row = 0, column = 0), value = "old"))

        viewModel.onEditCellSaveClick()
        advanceUntilIdle()

        assertEquals(1, closeRequests.size)
    }

    // the sheet stays in state until the UI has animated it out and calls onEditSheetDismiss
    @Test
    fun `saving leaves bottomSheetState alone so the sheet can animate out`() = runTest(mainDispatcherRule.testDispatcher) {
        coEvery { repository.editCell(any(), any()) } returns SuspendResult.success(Unit)
        val viewModel = createViewModel()
        collectCloseRequests(viewModel)
        val cell = TableCell(position = TableCellPosition(row = 2, column = 1), value = "old")
        viewModel.onCellDoubleClick(cell)

        viewModel.onEditCellSaveClick()
        advanceUntilIdle()

        assertEquals(cell, viewModel.state.value.bottomSheetState?.selectedCell)
    }

    @Test
    fun `saving with no open edit sheet asks for no close`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel()
        val closeRequests = collectCloseRequests(viewModel)

        viewModel.onEditCellSaveClick()
        advanceUntilIdle()

        assertEquals(0, closeRequests.size)
    }

    private fun TestScope.collectCloseRequests(viewModel: TableViewModel): List<Unit> {
        val closeRequests = mutableListOf<Unit>()
        backgroundScope.launch { viewModel.closeEditSheetEvent.collect { closeRequests.add(it) } }
        runCurrent()
        return closeRequests
    }
}
