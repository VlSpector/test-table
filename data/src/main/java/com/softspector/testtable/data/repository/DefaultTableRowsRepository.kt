package com.softspector.testtable.data.repository

import androidx.paging.InvalidatingPagingSourceFactory
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.softspector.testtable.common.SuspendResult
import com.softspector.testtable.common.doOnSuccess
import com.softspector.testtable.data.api.TableApi
import com.softspector.testtable.data.paging.TableRowsPagingSource
import com.softspector.testtable.domain.model.TableCellPosition
import com.softspector.testtable.domain.model.TableRow
import com.softspector.testtable.domain.repository.TableRowsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class DefaultTableRowsRepository(
    // Local data source to be added here if we add caching or implement offline first app
    private val api: TableApi,
    private val pagingSourceFactory: InvalidatingPagingSourceFactory<Int, TableRow>,
) : TableRowsRepository {

    override fun getTableRowsStream(): Flow<PagingData<TableRow>> = Pager(
        config = PagingConfig(
            pageSize = PAGE_SIZE,
            initialLoadSize = PAGE_SIZE,
            enablePlaceholders = false,
        ),
        pagingSourceFactory = pagingSourceFactory,
    ).flow

    override suspend fun editCell(
        position: TableCellPosition,
        value: String,
    ): SuspendResult<Unit> = api.editCell(
        position = position,
        value = value,
    ).doOnSuccess {
        // the edit changed the source data, so the loaded pages have to be reloaded
        pagingSourceFactory.invalidate()
    }

    class Factory @Inject constructor(
        private val api: TableApi,
    ) : TableRowsRepository.Factory {

        override fun create(columnsNumber: Int, limit: Int): TableRowsRepository =
            DefaultTableRowsRepository(
                api = api,
                pagingSourceFactory = InvalidatingPagingSourceFactory {
                    TableRowsPagingSource(
                        api = api,
                        columnsNumber = columnsNumber,
                        limit = limit,
                    )
                },
            )
    }

    companion object {
        private const val PAGE_SIZE = 25
    }
}
