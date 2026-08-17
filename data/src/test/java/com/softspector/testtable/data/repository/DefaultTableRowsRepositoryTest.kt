package com.softspector.testtable.data.repository

import androidx.paging.InvalidatingPagingSourceFactory
import com.softspector.testtable.common.SuspendResult
import com.softspector.testtable.data.api.TableApi
import com.softspector.testtable.domain.model.TableCellPosition
import com.softspector.testtable.domain.model.TableRow
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultTableRowsRepositoryTest {

    private val position = TableCellPosition(row = 2, column = 1)
    private val api = mockk<TableApi>()
    private val pagingSourceFactory =
        mockk<InvalidatingPagingSourceFactory<Int, TableRow>>(relaxed = true)
    private val repository = DefaultTableRowsRepository(api, pagingSourceFactory)

    @Test
    fun `editCell delegates to the api with the same params and returns its result`() = runTest {
        coEvery {
            api.editCell(position = position, value = "x")
        } returns SuspendResult.success(Unit)

        val result = repository.editCell(position = position, value = "x")

        assertEquals(SuspendResult.success(Unit), result)
        coVerify(exactly = 1) { api.editCell(position = position, value = "x") }
    }

    @Test
    fun `editCell propagates a failure from the api`() = runTest {
        val exception = RuntimeException("network down")
        coEvery { api.editCell(any(), any()) } returns SuspendResult.failure(exception)

        val result = repository.editCell(position = position, value = "x")

        assertEquals(SuspendResult.Failure(exception), result)
    }

    @Test
    fun `editCell invalidates the paging source so the table reloads`() = runTest {
        coEvery { api.editCell(any(), any()) } returns SuspendResult.success(Unit)

        repository.editCell(position = position, value = "x")

        verify(exactly = 1) { pagingSourceFactory.invalidate() }
    }

    @Test
    fun `editCell does not invalidate when the api call fails`() = runTest {
        coEvery {
            api.editCell(any(), any())
        } returns SuspendResult.failure(RuntimeException("boom"))

        repository.editCell(position = position, value = "x")

        verify(exactly = 0) { pagingSourceFactory.invalidate() }
    }
}
