package com.softspector.testtable.domain.repository

import androidx.paging.PagingData
import com.softspector.testtable.common.SuspendResult
import com.softspector.testtable.domain.model.TableCellPosition
import com.softspector.testtable.domain.model.TableRow
import kotlinx.coroutines.flow.Flow

interface TableRowsRepository {

    fun getTableRowsStream(): Flow<PagingData<TableRow>>

    suspend fun editCell(
        position: TableCellPosition,
        value: String,
    ): SuspendResult<Unit>

    interface Factory {
        fun create(columnsNumber: Int, limit: Int): TableRowsRepository
    }
}
