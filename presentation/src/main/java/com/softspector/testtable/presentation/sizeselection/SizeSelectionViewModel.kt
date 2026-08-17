package com.softspector.testtable.presentation.sizeselection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.softspector.testtable.presentation.table.TableRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SizeSelectionViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(SizeSelectionState())
    val state: StateFlow<SizeSelectionState> = _state.asStateFlow()

    // Usually a sealed class of events, but for a single event no need a for sealed class
    private val _navigateToTableEvent = Channel<TableRoute>()
    val navigateToTableEvent: Flow<TableRoute> = _navigateToTableEvent.receiveAsFlow()

    fun onRowsTextChanged(text: String) {
        _state.update {
            val rowsText = sanitizeCount(text, MAX_ROWS)
            it.copy(rowsText = rowsText, isConfirmEnabled = isConfirmEnabled(rowsText, it.columnsText))
        }
    }

    fun onColumnsTextChanged(text: String) {
        _state.update {
            val columnsText = sanitizeCount(text, MAX_COLUMNS)
            it.copy(columnsText = columnsText, isConfirmEnabled = isConfirmEnabled(it.rowsText, columnsText))
        }
    }

    fun onConfirmClicked() {
        val current = _state.value
        val rows = (current.rowsText.toIntOrNull() ?: 1).coerceIn(1, MAX_ROWS)
        val columns = (current.columnsText.toIntOrNull() ?: 1).coerceIn(1, MAX_COLUMNS)
        viewModelScope.launch {
            _navigateToTableEvent.send(TableRoute(rows = rows, columns = columns))
        }
    }

    private fun sanitizeCount(text: String, max: Int): String {
        val digitsOnly = text.filter { it.isDigit() }.take(MAX_DIGITS)
        // allow user to clear input
        if (digitsOnly.isEmpty()) return digitsOnly
        return digitsOnly.toInt().coerceIn(1, max).toString()
    }

    private fun isConfirmEnabled(rowsText: String, columnsText: String): Boolean =
        rowsText.isNotEmpty() && columnsText.isNotEmpty()

    companion object {
        // enough digits to type any value up to MAX_ROWS
        private const val MAX_DIGITS = 4
        const val DEFAULT_ROWS = 4
        const val DEFAULT_COLUMNS = 4
        const val MAX_ROWS = 1000
        const val MAX_COLUMNS = 6
    }
}
