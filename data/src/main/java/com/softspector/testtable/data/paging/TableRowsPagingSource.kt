package com.softspector.testtable.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.softspector.testtable.common.SuspendResult
import com.softspector.testtable.data.api.TableApi
import com.softspector.testtable.domain.model.TableRow
import timber.log.Timber

class TableRowsPagingSource(
    private val api: TableApi,
    private val columnsNumber: Int,
    private val limit: Int,
) : PagingSource<Int, TableRow>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, TableRow> {
        val page = params.key ?: 1

        return when (
            val result = api.getTableRows(
                columnsNumber = columnsNumber,
                pageSize = params.loadSize,
                page = page,
                limit = limit,
            )
        ) {
            is SuspendResult.Success -> LoadResult.Page(
                data = result.value.tableRows,
                prevKey = result.value.previousPage,
                nextKey = result.value.nextPage,
            )

            is SuspendResult.Failure ->  {
                Timber.e(result.throwable, "Failed load table page")
                LoadResult.Error(result.throwable)
            }
        }
    }

    override fun getRefreshKey(state: PagingState<Int, TableRow>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val anchorPage = state.closestPageToPosition(anchorPosition)
        return anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
    }
}
