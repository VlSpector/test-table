package com.softspector.testtable

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Installs [testDispatcher] as `Dispatchers.Main` for the duration of a test, so ViewModels
 * relying on `viewModelScope` (which dispatches on Main) can be tested. Pass [testDispatcher]
 * into `runTest(...)` so both share the same scheduler.
 */
@OptIn(ExperimentalCoroutinesApi::class)

class MainDispatcherRule(
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
