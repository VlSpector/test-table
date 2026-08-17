package com.softspector.testtable.data.api

import com.softspector.testtable.common.SuspendResult
import com.softspector.testtable.data.model.TableRowsResponse
import com.softspector.testtable.domain.model.TableCellPosition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MockTableApiTest {
    private val api = MockTableApi()

    private suspend fun loadPage(
        columnsNumber: Int = 3,
        pageSize: Int = 25,
        page: Int = 1,
        limit: Int = 100,
    ): TableRowsResponse = when (
        val result = api.getTableRows(
            columnsNumber = columnsNumber,
            pageSize = pageSize,
            page = page,
            limit = limit,
        )
    ) {
        is SuspendResult.Success -> result.value
        is SuspendResult.Failure -> error("expected a page, got ${result.throwable}")
    }

    // region paging arithmetic

    @Test
    fun `the first page starts at row 0`() = runTest {
        val rows = loadPage(pageSize = 25, page = 1, limit = 100).tableRows

        assertEquals(25, rows.size)
        assertEquals(0, rows.first().index)
        assertEquals(24, rows.last().index)
    }

    @Test
    fun `rows carry their absolute row number, not their position in the page`() = runTest {
        val rows = loadPage(pageSize = 10, page = 2, limit = 100).tableRows

        assertEquals((10..19).toList(), rows.map { it.index })
    }

    @Test
    fun `page contents are offset by the page size`() = runTest {
        val rows = loadPage(pageSize = 25, page = 3, limit = 100).tableRows

        assertEquals(50, rows.first().index)
        assertEquals("50:0", rows.first().columnValues.first())
        assertEquals(74, rows.last().index)
    }

    @Test
    fun `every row carries one value per requested column`() = runTest {
        val rows = loadPage(columnsNumber = 6, pageSize = 4, page = 1, limit = 100).tableRows

        assertTrue(rows.all { it.columnValues.size == 6 })
        assertEquals(
            listOf("0:0", "0:1", "0:2", "0:3", "0:4", "0:5"),
            rows.first().columnValues,
        )
    }

    @Test
    fun `the last page is short when the limit is not a multiple of the page size`() = runTest {
        val rows = loadPage(pageSize = 25, page = 2, limit = 30).tableRows

        assertEquals(5, rows.size)
        assertEquals((25..29).toList(), rows.map { it.index })
    }

    @Test
    fun `a table smaller than one page returns only the rows that exist`() = runTest {
        val rows = loadPage(pageSize = 25, page = 1, limit = 4).tableRows

        assertEquals(4, rows.size)
        assertEquals(3, rows.last().index)
    }

    @Test
    fun `a page past the end of the table is empty`() = runTest {
        val rows = loadPage(pageSize = 25, page = 5, limit = 10).tableRows

        assertTrue(rows.isEmpty())
    }

    @Test
    fun `nextPage points at the following page until the limit is reached`() = runTest {
        assertEquals(2, loadPage(pageSize = 25, page = 1, limit = 100).nextPage)
        assertEquals(4, loadPage(pageSize = 25, page = 3, limit = 100).nextPage)
        assertNull(loadPage(pageSize = 25, page = 4, limit = 100).nextPage)
    }

    @Test
    fun `previousPage is null only on the first page`() = runTest {
        assertNull(loadPage(pageSize = 25, page = 1, limit = 100).previousPage)
        assertEquals(1, loadPage(pageSize = 25, page = 2, limit = 100).previousPage)
        assertEquals(2, loadPage(pageSize = 25, page = 3, limit = 100).previousPage)
    }

    // endregion

    // region edits

    @Test
    fun `an edited cell comes back with the new value`() = runTest {
        api.editCell(TableCellPosition(row = 2, column = 1), "edited")

        val rows = loadPage().tableRows

        assertEquals("edited", rows[2].columnValues[1])
    }

    @Test
    fun `editing the same cell twice keeps the latest value`() = runTest {
        val position = TableCellPosition(row = 2, column = 1)
        api.editCell(position, "first")

        api.editCell(position, "second")

        assertEquals("second", loadPage().tableRows[2].columnValues[1])
    }

    @Test
    fun `an edit leaves its neighbours alone`() = runTest {
        api.editCell(TableCellPosition(row = 2, column = 1), "edited")

        val rows = loadPage().tableRows

        assertEquals("2:0", rows[2].columnValues[0])
        assertEquals("2:2", rows[2].columnValues[2])
        assertEquals("1:1", rows[1].columnValues[1])
        assertEquals("3:1", rows[3].columnValues[1])
    }

    @Test
    fun `an edit shows up on whichever page contains that row`() = runTest {
        api.editCell(TableCellPosition(row = 60, column = 0), "edited")

        val rows = loadPage(pageSize = 25, page = 3, limit = 100).tableRows

        val edited = rows.single { it.index == 60 }
        assertEquals("edited", edited.columnValues[0])
    }

    @Test
    fun `an edit survives being read back on a different page size`() = runTest {
        api.editCell(TableCellPosition(row = 7, column = 2), "edited")

        val rows = loadPage(pageSize = 4, page = 2, limit = 100).tableRows

        assertEquals("edited", rows.single { it.index == 7 }.columnValues[2])
    }

    // endregion

    @Test
    fun `concurrent edits and reads neither throw nor lose writes`() = runTest {
        // the edit cache is a plain map, so reads and writes have to be guarded;
        // unguarded this tends to throw ConcurrentModificationException
        withContext(Dispatchers.Default) {
            coroutineScope {
                repeat(CONCURRENT_EDITS) { row ->
                    launch { api.editCell(TableCellPosition(row = row, column = 0), "edited $row") }
                    launch { loadPage(pageSize = CONCURRENT_EDITS, page = 1, limit = 200) }
                }
            }
        }

        val rows = loadPage(pageSize = CONCURRENT_EDITS, page = 1, limit = 200).tableRows
        assertEquals(
            List(CONCURRENT_EDITS) { row -> "edited $row" },
            rows.map { it.columnValues.first() },
        )
    }

    private companion object {
        const val CONCURRENT_EDITS = 50
    }
}
