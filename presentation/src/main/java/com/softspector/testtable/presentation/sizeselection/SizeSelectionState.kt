package com.softspector.testtable.presentation.sizeselection

import com.softspector.testtable.presentation.sizeselection.SizeSelectionViewModel.Companion.DEFAULT_COLUMNS
import com.softspector.testtable.presentation.sizeselection.SizeSelectionViewModel.Companion.DEFAULT_ROWS

data class SizeSelectionState(
    val rowsText: String = DEFAULT_ROWS.toString(),
    val columnsText: String = DEFAULT_COLUMNS.toString(),
    val isConfirmEnabled: Boolean = true,
)
