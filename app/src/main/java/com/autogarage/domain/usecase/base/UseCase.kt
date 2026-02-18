package com.autogarage.domain.usecase.base

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

abstract class UseCase<in Params, out Result> {

    protected open val dispatcher: CoroutineDispatcher = Dispatchers.IO

    protected abstract suspend fun execute(params: Params): Result

    suspend operator fun invoke(params: Params): kotlin.Result<Result> {
        return try {
            withContext(dispatcher) {
                kotlin.Result.success(execute(params))
            }
        } catch (e: Exception) {
            kotlin.Result.failure(e)
        }
    }
}

// ===========================================================================
// FlowUseCase (for functions that return Flow)
// ===========================================================================
abstract class FlowUseCase<in Params, out Result> {

    protected abstract fun execute(params: Params): Flow<Result>

    operator fun invoke(params: Params): Flow<Result> {
        return execute(params)
    }
}
