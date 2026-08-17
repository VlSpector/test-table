package com.softspector.testtable.data.api

import com.softspector.testtable.common.SuspendResult
import com.softspector.testtable.data.model.TableRowsResponse
import com.softspector.testtable.domain.model.TableCellPosition

/**
 * There is no serialization boundary yet, so there are no DTOs to map:
 * the api returns domain models directly rather than 1 to 1 copies of them.
 * Once a real BE lands, DTOs belong in data/model and the repository maps them to domain.
 */
interface TableApi {
    suspend fun getTableRows(
        columnsNumber: Int,
        pageSize: Int,
        page: Int,
        limit: Int,
    ): SuspendResult<TableRowsResponse>

    suspend fun editCell(
        position: TableCellPosition,
        value: String,
    ): SuspendResult<Unit>
}
