package com.softspector.testtable.common

import androidx.annotation.CheckResult
import kotlinx.coroutines.CancellationException

sealed class SuspendResult<out T> {
    data class Success<T>(
        val value: T,
    ) : SuspendResult<T>()

    data class Failure(
        val throwable: Throwable,
    ) : SuspendResult<Nothing>()

    companion object {
        @CheckResult
        inline operator fun <T> invoke(block: () -> T): SuspendResult<T> {
            return try {
                success(block())
            } catch (e: Error) {
                throw e
            } catch (e: CancellationException) {
                throw e
            } catch (t: Throwable) {
                failure(t)
            }
        }

        @CheckResult
        fun <T> success(value: T) = Success(value)

        @CheckResult
        fun <T> failure(throwable: Throwable): SuspendResult<T> = Failure(throwable)
    }
}

suspend fun <T> SuspendResult<T>.doOnSuccessSuspend(block: suspend (T) -> Unit): SuspendResult<T> {
    if (this is SuspendResult.Success) {
        block(this.value)
    }

    return this
}

fun <T> SuspendResult<T>.doOnSuccess(block: (T) -> Unit): SuspendResult<T> {
    if (this is SuspendResult.Success) {
        block(this.value)
    }

    return this
}

fun <T> SuspendResult<T>.doOnFailure(block: (Throwable) -> Unit): SuspendResult<T> {
    if (this is SuspendResult.Failure) {
        block(this.throwable)
    }

    return this
}

suspend fun <T> SuspendResult<T>.doOnFailureSuspend(block: suspend (Throwable) -> Unit): SuspendResult<T> {
    if (this is SuspendResult.Failure) {
        block(this.throwable)
    }

    return this
}

suspend fun <T, R> SuspendResult<T>.flatMap(block: suspend (T) -> SuspendResult<R>): SuspendResult<R> {
    return when (this) {
        is SuspendResult.Success -> {
            block(this.value)
        }

        is SuspendResult.Failure -> {
            SuspendResult.failure(this.throwable)
        }
    }
}