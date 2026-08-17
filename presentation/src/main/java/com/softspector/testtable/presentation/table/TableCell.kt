package com.softspector.testtable.presentation.table

import com.softspector.testtable.domain.model.TableCellPosition
import com.softspector.testtable.domain.model.TableRow

data class TableCell(
    val position: TableCellPosition,
    val value: String,
)

fun TableRow.toCells(): List<TableCell> = columnValues.mapIndexed { column, value ->
    TableCell(
        position = TableCellPosition(row = index, column = column),
        value = value,
    )
}
