package com.softspector.testtable.presentation.table

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.softspector.testtable.presentation.R
import com.softspector.testtable.domain.model.TableCellPosition
import com.softspector.testtable.ui.theme.TestTableTheme
import com.softspector.testtable.ui.theme.spacing

private val EditContentMaxWidth = 400.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableEditSheet(
    state: TableState.BottomSheetState,
    sheetState: SheetState,
    onInputChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
    ) {
        TableEditSheetContent(
            state = state,
            // isVisible turns true once the sheet has settled at its anchor, which is the
            // point the field is laid out and can take focus
            shouldFocusInput = sheetState.isVisible,
            onInputChange = onInputChange,
            onSaveClick = onSaveClick,
        )
    }
}

@Composable
private fun TableEditSheetContent(
    state: TableState.BottomSheetState,
    shouldFocusInput: Boolean,
    onInputChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }

    // false on the first composition and only flipped by a later state change, so by the time
    // this asks for focus the field has been through a layout pass
    LaunchedEffect(shouldFocusInput) {
        if (shouldFocusInput) {
            focusRequester.requestFocus()
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .imePadding()
            .padding(
                horizontal = MaterialTheme.spacing.small,
                vertical = MaterialTheme.spacing.medium,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            // same row:column order the cells are rendered in
            text = stringResource(
                R.string.table_edit_title,
                state.selectedCell.position.row,
                state.selectedCell.position.column,
            ),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
        OutlinedTextField(
            value = state.inputValue,
            onValueChange = onInputChange,
            label = { Text(text = stringResource(R.string.table_edit_value_label)) },
            singleLine = true,
            modifier = Modifier
                .widthIn(max = EditContentMaxWidth)
                .fillMaxWidth()
                .focusRequester(focusRequester),
        )
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
        Button(
            onClick = onSaveClick,
            modifier = Modifier
                .widthIn(max = EditContentMaxWidth)
                .fillMaxWidth(),
        ) {
            Text(text = stringResource(R.string.table_edit_save))
        }
    }
}

@Preview(showBackground = true, widthDp = 800)
@Composable
private fun TableEditSheetContentPreview() {
    TestTableTheme {
        TableEditSheetContent(
            state = TableState.BottomSheetState(
                selectedCell = TableCell(
                    position = TableCellPosition(row = 12, column = 3),
                    value = "12:3",
                ),
            ),
            // nothing to focus in a preview, and no real sheet to settle first
            shouldFocusInput = false,
            onInputChange = {},
            onSaveClick = {},
        )
    }
}
