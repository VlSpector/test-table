package com.softspector.testtable.data.model

import com.softspector.testtable.domain.model.TableRow

data class TableRowsResponse(
    val tableRows: List<TableRow>,
    val nextPage: Int?,
    val previousPage: Int?,
)