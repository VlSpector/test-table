package com.softspector.testtable.presentation.table

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.softspector.testtable.common.doOnFailure
import com.softspector.testtable.domain.model.TableRow
import com.softspector.testtable.domain.repository.TableRowsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel(assistedFactory = TableViewModel.Factory::class)
class TableViewModel @AssistedInject constructor(
    @Assisted private val route: TableRoute,
    repositoryFactory: TableRowsRepository.Factory,
) : ViewModel() {

    // one repository per table, built for the size this screen was opened with
    private val repository = repositoryFactory.create(
        columnsNumber = route.columns,
        limit = route.rows,
    )

    private val _state = MutableStateFlow(TableState())
    val state: StateFlow<TableState> = _state.asStateFlow()

    private val _closeEditSheetEvent = Channel<Unit>()
    val closeEditSheetEvent: Flow<Unit> = _closeEditSheetEvent.receiveAsFlow()

    val tableRows: Flow<PagingData<TableRow>> = repository
        .getTableRowsStream()
        .cachedIn(viewModelScope)

    @AssistedFactory
    interface Factory {
        fun create(route: TableRoute): TableViewModel
    }

    fun onCellClick(cell: TableCell) {
        _state.update { state ->
            val clickedPosition = cell.position
            // clicking the selected cell clears it, any other cell takes the selection over
            val newSelectedPosition = if (state.selectedCellPosition == clickedPosition) {
                null
            } else {
                clickedPosition
            }

            state.copy(
                selectedCellPosition = newSelectedPosition,
            )
        }
    }

    fun onCellDoubleClick(cell: TableCell) {
        _state.update { state ->
            state.copy(
                bottomSheetState = TableState.BottomSheetState(
                    selectedCell = cell,
                )
            )
        }
    }

    fun onEditSheetInputChange(inputValue: String) {
        _state.update { state ->
            state.copy(
                bottomSheetState = state.bottomSheetState?.copy(
                    inputValue = inputValue,
                )
            )
        }
    }

    fun onEditCellSaveClick() {
        val bottomSheetState = _state.value.bottomSheetState ?: return
        val newCell = bottomSheetState.selectedCell.copy(
            value = bottomSheetState.inputValue,
        )

        saveNewCell(newCell)
    }

    fun onEditSheetDismiss() {
        _state.update { state ->
            state.copy(
                bottomSheetState = null,
            )
        }
    }

    private fun saveNewCell(
        cell: TableCell,
    ) {
        viewModelScope.launch {
            repository.editCell(
                position = cell.position,
                value = cell.value,
            ).doOnFailure {
                Timber.e(it, "Failed to edit cell")
                // Also show error toast or whatever will be the requirement
            }

            // on success the repository invalidates its paging source and the table reloads on
            // its own. the sheet closes either way, edited or not
            _closeEditSheetEvent.send(Unit)
        }
    }
}