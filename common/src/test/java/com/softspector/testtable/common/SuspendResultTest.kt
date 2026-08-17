package com.softspector.testtable.common

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class SuspendResultTest {

    @Test
    fun `invoke wraps a successful block in Success`() {
        val result = SuspendResult { 42 }

        assertEquals(SuspendResult.Success(42), result)
    }

    @Test
    fun `invoke wraps a throwing block in Failure`() {
        val exception = IllegalStateException("boom")

        val result = SuspendResult<Int> { throw exception }

        assertEquals(SuspendResult.Failure(exception), result)
    }

    @Test(expected = CancellationException::class)
    fun `invoke rethrows CancellationException instead of wrapping it`() {
        SuspendResult<Int> { throw CancellationException() }
    }

    @Test(expected = OutOfMemoryError::class)
    fun `invoke rethrows Error instead of wrapping it`() {
        SuspendResult<Int> { throw OutOfMemoryError() }
    }

    @Test
    fun `success creates a Success`() {
        assertEquals(SuspendResult.Success("value"), SuspendResult.success("value"))
    }

    @Test
    fun `failure creates a Failure`() {
        val exception = RuntimeException()

        assertEquals(SuspendResult.Failure(exception), SuspendResult.failure<Unit>(exception))
    }

    @Test
    fun `doOnSuccess runs the block only for Success and returns the same instance`() {
        val result = SuspendResult.success(1)
        var seen: Int? = null

        val returned = result.doOnSuccess { seen = it }

        assertEquals(1, seen)
        assertSame(result, returned)
    }

    @Test
    fun `doOnSuccess does not run for Failure`() {
        val result = SuspendResult.failure<Int>(RuntimeException())
        var called = false

        result.doOnSuccess { called = true }

        assertFalse(called)
    }

    @Test
    fun `doOnFailure runs the block only for Failure and returns the same instance`() {
        val exception = RuntimeException()
        val result = SuspendResult.failure<Int>(exception)
        var seen: Throwable? = null

        val returned = result.doOnFailure { seen = it }

        assertSame(exception, seen)
        assertSame(result, returned)
    }

    @Test
    fun `doOnFailure does not run for Success`() {
        val result = SuspendResult.success(1)
        var called = false

        result.doOnFailure { called = true }

        assertFalse(called)
    }

    @Test
    fun `doOnSuccessSuspend runs only for Success`() = runTest {
        var called = false

        SuspendResult.success(1).doOnSuccessSuspend { called = true }
        assertTrue(called)

        called = false
        SuspendResult.failure<Int>(RuntimeException()).doOnSuccessSuspend { called = true }
        assertFalse(called)
    }

    @Test
    fun `doOnFailureSuspend runs only for Failure`() = runTest {
        var called = false

        SuspendResult.failure<Int>(RuntimeException()).doOnFailureSuspend { called = true }
        assertTrue(called)

        called = false
        SuspendResult.success(1).doOnFailureSuspend { called = true }
        assertFalse(called)
    }

    @Test
    fun `flatMap chains Success into the next block`() = runTest {
        val result = SuspendResult.success(1).flatMap { SuspendResult.success(it + 1) }

        assertEquals(SuspendResult.success(2), result)
    }

    @Test
    fun `flatMap short-circuits on Failure without running the block`() = runTest {
        val exception = RuntimeException()
        var called = false

        val result = SuspendResult.failure<Int>(exception).flatMap {
            called = true
            SuspendResult.success(it + 1)
        }

        assertFalse(called)
        assertEquals(SuspendResult.Failure(exception), result)
    }

    @Test
    fun `flatMap propagates a Failure returned by the block`() = runTest {
        val exception = RuntimeException()

        val result = SuspendResult.success(1).flatMap { SuspendResult.failure<Int>(exception) }

        assertEquals(SuspendResult.Failure(exception), result)
    }
}
