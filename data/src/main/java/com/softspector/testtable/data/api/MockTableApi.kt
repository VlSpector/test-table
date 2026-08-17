package com.softspector.testtable.data.api

import com.softspector.testtable.common.SuspendResult
import com.softspector.testtable.data.model.TableRowsResponse
import com.softspector.testtable.domain.model.TableCellPosition
import com.softspector.testtable.domain.model.TableRow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton


/**
 * Mock implementation of TableApi
 * Simulates BE behavior for a Paged List of Table Rows
 * Caches user's Cell edits
 */

@Singleton
class MockTableApi @Inject constructor() : TableApi {
    private val mutex = Mutex()

    private val cachedTableEdits = mutableMapOf<TableCellPosition, String>()

    override suspend fun getTableRows(
        columnsNumber: Int,
        pageSize: Int,
        page: Int,
        limit: Int
    ) = SuspendResult {
        val previousPage = if (page > 1) {
            page - 1
        } else {
            null
        }

        val nextPage = if (page * pageSize < limit) {
            page + 1
        } else {
            null
        }

        val offset = (page - 1) * pageSize

        // use mutex as the map is not thread safe and edits can land mid-read
        val rows = mutex.withLock {
            // the last page is short when limit is not a multiple of pageSize
            List((limit - offset).coerceIn(0, pageSize)) { rowIndex ->
                val row = offset + rowIndex
                TableRow(
                    index = row,
                    columnValues = List(columnsNumber) { index ->
                        cachedTableEdits[TableCellPosition(row = row, column = index)]
                            ?: "$row:$index"
                    },
                )
            }
        }

        TableRowsResponse(
            tableRows = rows,
            previousPage = previousPage,
            nextPage = nextPage,
        )
    }

    // Simulating BE updating the data
    override suspend fun editCell(
        position: TableCellPosition,
        value: String
    ): SuspendResult<Unit> {
        // use mutex as the map is not thread safe
        mutex.withLock {
            cachedTableEdits[position] = value
        }

        return SuspendResult.Success(Unit)
    }
}
