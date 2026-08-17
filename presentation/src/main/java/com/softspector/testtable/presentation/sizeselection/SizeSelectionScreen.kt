package com.softspector.testtable.presentation.sizeselection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.softspector.testtable.presentation.R
import com.softspector.testtable.presentation.util.SingleEventEffect
import com.softspector.testtable.presentation.sizeselection.SizeSelectionViewModel.Companion.MAX_COLUMNS
import com.softspector.testtable.presentation.sizeselection.SizeSelectionViewModel.Companion.MAX_ROWS
import com.softspector.testtable.presentation.table.TableRoute
import com.softspector.testtable.ui.theme.TestTableTheme
import com.softspector.testtable.ui.theme.spacing

@Composable
fun SizeSelectionScreen(
    onNavigateToTable: (TableRoute) -> Unit,
    viewModel: SizeSelectionViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    SingleEventEffect(viewModel.navigateToTableEvent) { route ->
        onNavigateToTable(route)
    }

    SizeSelectionContent(
        state = state,
        onRowsTextChanged = viewModel::onRowsTextChanged,
        onColumnsTextChanged = viewModel::onColumnsTextChanged,
        onConfirmClicked = viewModel::onConfirmClicked,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SizeSelectionContent(
    state: SizeSelectionState,
    onRowsTextChanged: (String) -> Unit,
    onColumnsTextChanged: (String) -> Unit,
    onConfirmClicked: () -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text(text = stringResource(R.string.size_selection_title)) })
        },
    ) { paddings ->
        Box(
            modifier = Modifier
                .padding(paddings)
                .fillMaxSize()
                .imePadding(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .width(IntrinsicSize.Min)
                    .padding(MaterialTheme.spacing.small),
                verticalArrangement = Arrangement.Center,
            ) {
                OutlinedTextField(
                    value = state.columnsText,
                    onValueChange = onColumnsTextChanged,
                    label = { Text(text = stringResource(R.string.size_selection_columns_label, MAX_COLUMNS)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                )
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall2))
                OutlinedTextField(
                    value = state.rowsText,
                    onValueChange = onRowsTextChanged,
                    label = { Text(text = stringResource(R.string.size_selection_rows_label, MAX_ROWS)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                )
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
                Button(
                    onClick = onConfirmClicked,
                    enabled = state.isConfirmEnabled,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = stringResource(R.string.size_selection_confirm))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SizeSelectionScreenPreview() {
    TestTableTheme {
        SizeSelectionContent(
            state = SizeSelectionState(),
            onRowsTextChanged = {},
            onColumnsTextChanged = {},
            onConfirmClicked = {},
        )
    }
}
