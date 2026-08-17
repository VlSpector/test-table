package com.softspector.testtable.presentation.table

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.softspector.testtable.domain.model.TableCellPosition
import com.softspector.testtable.domain.model.TableRow
import com.softspector.testtable.presentation.R
import com.softspector.testtable.presentation.util.SingleEventEffect
import com.softspector.testtable.ui.theme.TestTableTheme
import com.softspector.testtable.ui.theme.spacing
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

private val CellMinHeight = 48.dp
private val AppendProgressSize = 24.dp

private const val APPEND_LOADING_KEY = "append_loading"
private const val APPEND_ERROR_KEY = "append_error"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableScreen(
    route: TableRoute,
    onBackClick: () -> Unit,
    viewModel: TableViewModel = hiltViewModel(
        creationCallback = { factory: TableViewModel.Factory -> factory.create(route) },
    ),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val tableRows = viewModel.tableRows.collectAsLazyPagingItems()
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    SingleEventEffect(viewModel.closeEditSheetEvent) {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            viewModel.onEditSheetDismiss()
        }
    }

    TableContent(
        state = state,
        tableRows = tableRows,
        sheetState = sheetState,
        onBackClick = onBackClick,
        onCellClick = viewModel::onCellClick,
        onCellDoubleClick = viewModel::onCellDoubleClick,
        onEditSheetInputChange = viewModel::onEditSheetInputChange,
        onEditSheetSaveClick = viewModel::onEditCellSaveClick,
        onEditSheetDismiss = viewModel::onEditSheetDismiss,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TableContent(
    state: TableState,
    tableRows: LazyPagingItems<TableRow>,
    sheetState: SheetState,
    onBackClick: () -> Unit,
    onCellClick: (TableCell) -> Unit,
    onCellDoubleClick: (TableCell) -> Unit,
    onEditSheetInputChange: (String) -> Unit,
    onEditSheetSaveClick: () -> Unit,
    onEditSheetDismiss: () -> Unit,
) {
    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = { Text(text = stringResource(R.string.table_title)) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.table_back),
                            )
                        }
                    },
                )
                HorizontalDivider()
            }
        },
    ) { paddings ->
        val contentModifier = Modifier
            .padding(paddings)
            .fillMaxSize()

        when (tableRows.loadState.refresh) {
            is LoadState.Loading -> TableLoading(modifier = contentModifier)

            is LoadState.Error -> TableError(
                onRetryClick = tableRows::retry,
                modifier = contentModifier,
            )

            is LoadState.NotLoading -> TableRowsList(
                selectedCellPosition = state.selectedCellPosition,
                tableRows = tableRows,
                onCellClick = onCellClick,
                onCellDoubleClick = onCellDoubleClick,
                modifier = contentModifier,
            )
        }

        if (state.bottomSheetState != null) {
            TableEditSheet(
                state = state.bottomSheetState,
                sheetState = sheetState,
                onInputChange = onEditSheetInputChange,
                onSaveClick = onEditSheetSaveClick,
                onDismiss = onEditSheetDismiss,
            )
        }
    }
}

@Composable
private fun TableRowsList(
    selectedCellPosition: TableCellPosition?,
    tableRows: LazyPagingItems<TableRow>,
    onCellClick: (TableCell) -> Unit,
    onCellDoubleClick: (TableCell) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            horizontal = MaterialTheme.spacing.small,
            vertical = MaterialTheme.spacing.extraSmall2,
        ),
    ) {
        items(
            count = tableRows.itemCount,
            key = tableRows.itemKey { row -> row.index },
        ) { rowIndex ->
            val row = tableRows[rowIndex]
            if (row == null) {
                // unreachable while placeholders are off, kept so turning them on
                // cannot collapse the list into zero height rows
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(CellMinHeight),
                )
                return@items
            }

            val cells = remember(row) { row.toCells() }

            Row(modifier = Modifier.fillMaxWidth()) {
                cells.forEach { cell ->
                    TableCellItem(
                        value = cell.value,
                        isSelected = selectedCellPosition == cell.position,
                        onClick = { onCellClick(cell) },
                        onDoubleClick = { onCellDoubleClick(cell) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        when (tableRows.loadState.append) {
            is LoadState.Loading -> item(key = APPEND_LOADING_KEY) {
                AppendLoading()
            }

            is LoadState.Error -> item(key = APPEND_ERROR_KEY) {
                AppendError(onRetryClick = tableRows::retry)
            }

            is LoadState.NotLoading -> Unit
        }
    }
}

@Composable
private fun TableLoading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun TableError(
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(MaterialTheme.spacing.small),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.table_error),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
        Button(onClick = onRetryClick) {
            Text(text = stringResource(R.string.table_retry))
        }
    }
}

@Composable
private fun AppendLoading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(MaterialTheme.spacing.small),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(modifier = Modifier.size(AppendProgressSize))
    }
}

@Composable
private fun AppendError(
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(MaterialTheme.spacing.extraSmall2),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = stringResource(R.string.table_append_error))
        TextButton(onClick = onRetryClick) {
            Text(text = stringResource(R.string.table_retry))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TableCellItem(
    value: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDoubleClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = value,
        fontSize = 18.sp,
        textAlign = TextAlign.Center,
        modifier = modifier
            .defaultMinSize(minHeight = CellMinHeight)
            .background(
                if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surface
                }
            )
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant))
            .combinedClickable(
                onClick = onClick,
                onDoubleClick = onDoubleClick,
            )
            .padding(MaterialTheme.spacing.extraSmall2)
            .wrapContentHeight(Alignment.CenterVertically),
    )
}

// the mock api never fails, so this state has no way to show up at runtime
@Preview(showBackground = true)
@Composable
private fun TableErrorPreview() {
    TestTableTheme {
        TableError(
            onRetryClick = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun TableScreenPreview() {
    TestTableTheme {
        val previewRows = List(4) { rowIndex ->
            TableRow(
                index = rowIndex,
                columnValues = List(4) { columnIndex -> "$rowIndex:$columnIndex" },
            )
        }
        TableContent(
            state = TableState(),
            tableRows = flowOf(PagingData.from(previewRows)).collectAsLazyPagingItems(),
            sheetState = rememberModalBottomSheetState(),
            onBackClick = {},
            onCellClick = {},
            onCellDoubleClick = {},
            onEditSheetDismiss = {},
            onEditSheetSaveClick = {},
            onEditSheetInputChange = {},
        )
    }
}