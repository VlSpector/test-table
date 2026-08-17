package com.softspector.testtable.data.paging

import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.softspector.testtable.common.SuspendResult
import com.softspector.testtable.data.api.TableApi
import com.softspector.testtable.domain.model.TableRow
import com.softspector.testtable.data.model.TableRowsResponse
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TableRowsPagingSourceTest {

    private val api = mockk<TableApi>()

    private fun pagingSource(columnsNumber: Int = 3, limit: Int = 50) =
        TableRowsPagingSource(api, columnsNumber = columnsNumber, limit = limit)

    @Test
    fun `load defaults to page 1 when no key is given and uses loadSize as pageSize`() = runTest {
        val rows = listOf(TableRow(index = 0, columnValues = listOf("a", "b", "c")))
        coEvery {
            api.getTableRows(columnsNumber = 3, pageSize = 20, page = 1, limit = 50)
        } returns SuspendResult.success(TableRowsResponse(tableRows = rows, nextPage = 2, previousPage = null))

        val result = pagingSource().load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false),
        )

        assertEquals(PagingSource.LoadResult.Page(data = rows, prevKey = null, nextKey = 2), result)
    }

    @Test
    fun `load uses the given page key`() = runTest {
        coEvery {
            api.getTableRows(columnsNumber = 3, pageSize = 15, page = 3, limit = 50)
        } returns SuspendResult.success(
            TableRowsResponse(tableRows = emptyList(), nextPage = 4, previousPage = 2)
        )

        val result = pagingSource().load(
            PagingSource.LoadParams.Append(key = 3, loadSize = 15, placeholdersEnabled = false),
        )

        assertEquals(PagingSource.LoadResult.Page(data = emptyList(), prevKey = 2, nextKey = 4), result)
    }

    @Test
    fun `load returns Error when the api call fails`() = runTest {
        val exception = RuntimeException("boom")
        coEvery {
            api.getTableRows(columnsNumber = 3, pageSize = 20, page = 1, limit = 50)
        } returns SuspendResult.failure(exception)

        val result = pagingSource().load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false),
        )

        assertEquals(PagingSource.LoadResult.Error<Int, TableRow>(exception), result)
    }

    @Test
    fun `getRefreshKey returns null when there is no anchor position`() {
        val state = PagingState<Int, TableRow>(
            pages = emptyList(),
            anchorPosition = null,
            config = PagingConfig(pageSize = 20),
            leadingPlaceholderCount = 0,
        )

        assertNull(pagingSource().getRefreshKey(state))
    }

    @Test
    fun `getRefreshKey derives the key from the closest page's prevKey`() {
        val page = PagingSource.LoadResult.Page(
            data = listOf(TableRow(index = 0, columnValues = listOf("a"))),
            prevKey = 2,
            nextKey = 4,
        )
        val state = PagingState(
            pages = listOf(page),
            anchorPosition = 0,
            config = PagingConfig(pageSize = 20),
            leadingPlaceholderCount = 0,
        )

        assertEquals(3, pagingSource().getRefreshKey(state))
    }

    @Test
    fun `getRefreshKey falls back to nextKey minus one when there is no prevKey`() {
        val page = PagingSource.LoadResult.Page(
            data = listOf(TableRow(index = 0, columnValues = listOf("a"))),
            prevKey = null,
            nextKey = 4,
        )
        val state = PagingState(
            pages = listOf(page),
            anchorPosition = 0,
            config = PagingConfig(pageSize = 20),
            leadingPlaceholderCount = 0,
        )

        assertEquals(3, pagingSource().getRefreshKey(state))
    }
}
