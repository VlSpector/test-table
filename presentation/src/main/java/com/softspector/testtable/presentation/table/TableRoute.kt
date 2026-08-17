package com.softspector.testtable.presentation.table

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class TableRoute(
    val rows: Int,
    val columns: Int,
) : NavKey