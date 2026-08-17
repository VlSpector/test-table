package com.softspector.testtable.domain.model

/**
 * Coordinates of a single cell.
 * Shared vocabulary: the UI selects by position and the data source edits by position,
 * so neither side passes bare row and column ints around.
 */
data class TableCellPosition(
    val row: Int,
    val column: Int,
)
