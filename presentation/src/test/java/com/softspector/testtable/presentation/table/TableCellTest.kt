package com.softspector.testtable.presentation.table

import com.softspector.testtable.domain.model.TableCellPosition
import com.softspector.testtable.domain.model.TableRow
import org.junit.Assert.assertEquals
import org.junit.Test

class TableCellTest {

    @Test
    fun `toCells positions every cell on the row number the data source assigned`() {
        val row = TableRow(index = 617, columnValues = listOf("a", "b", "c"))

        assertEquals(
            listOf(
                TableCell(TableCellPosition(row = 617, column = 0), "a"),
                TableCell(TableCellPosition(row = 617, column = 1), "b"),
                TableCell(TableCellPosition(row = 617, column = 2), "c"),
            ),
            row.toCells(),
        )
    }

    @Test
    fun `toCells numbers columns left to right`() {
        val row = TableRow(index = 0, columnValues = listOf("first", "second"))

        assertEquals(listOf(0, 1), row.toCells().map { it.position.column })
    }

    @Test
    fun `toCells returns nothing for a row without columns`() {
        val row = TableRow(index = 3, columnValues = emptyList())

        assertEquals(emptyList<TableCell>(), row.toCells())
    }
}
