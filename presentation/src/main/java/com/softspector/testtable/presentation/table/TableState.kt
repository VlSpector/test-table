package com.softspector.testtable.presentation.table

import com.softspector.testtable.domain.model.TableCellPosition

data class TableState(
    val selectedCellPosition: TableCellPosition? = null,
    val bottomSheetState: BottomSheetState? = null,
) {
    data class BottomSheetState(
        val selectedCell: TableCell,
        val inputValue: String = selectedCell.value,
    )
}