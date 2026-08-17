package com.softspector.testtable.domain.model

data class TableRow(
    // the row number the data source assigned, not the position in a loaded page
    val index: Int,
    val columnValues: List<String>,
)
