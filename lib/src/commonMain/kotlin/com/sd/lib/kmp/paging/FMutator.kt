package com.sd.lib.kmp.paging

import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

internal interface FMutator {
  /**
   * [mutate]是否正在修改中
   */
  suspend fun isMutating(): Boolean

  /**
   * 互斥修改，如果协程A正在修改，则协程B调用此方法时会先取消协程A，再执行[block]，
   * 如果[effect]的block正在执行则此方法会挂起直到它结束，[block]总是串行，不会并发
   *
   * 注意：[block]中嵌套调用[mutate]或者[effect]会抛异常
   */
  suspend fun <T> mutate(block: suspend MutateScope.() -> T): T

  /**
   * 执行[block]，如果[mutate]的block正在执行则此方法会挂起直到它结束，[block]总是串行，不会并发
   *
   * 注意：[block]中嵌套调用[mutate]或者[effect]会抛异常
   */
  suspend fun <T> effect(block: suspend () -> T): T

  /**
   * 取消正在执行的[mutate]修改，[effect]修改不会被取消
   */
  suspend fun cancelMutate()

  interface MutateScope {
    /** 确保[mutate]协程处于激活状态，否则抛出取消异常 */
    suspend fun ensureMutateActive()
  }
}

internal fun FMutator(): FMutator = MutatorImpl()

private class MutatorImpl : FMutator {
  private var _job: Job? = null
  private val _jobMutex = Mutex()
  private val _mutateMutex = Mutex()

  override suspend fun isMutating(): Boolean {
    return _jobMutex.withLock {
      _job?.isActive == true
    }
  }

  override suspend fun <R> mutate(block: suspend FMutator.MutateScope.() -> R): R {
    checkNestedMutate()
    return coroutineScope {
      val mutateContext = coroutineContext
      val mutateJob = checkNotNull(mutateContext[Job])

      _jobMutex.withLock {
        _job?.cancelAndJoin()
        _job = mutateJob
      }

      try {
        doMutate(MutateType.Mutate) {
          with(newMutateScope(mutateContext)) { block() }
        }
      } finally {
        if (_jobMutex.tryLock()) {
          if (_job === mutateJob) _job = null
          _jobMutex.unlock()
        }
      }
    }
  }

  override suspend fun <T> effect(block: suspend () -> T): T {
    checkNestedMutate()
    return doMutate(MutateType.Effect, block)
  }

  override suspend fun cancelMutate() {
    _jobMutex.withLock {
      _job?.cancelAndJoin()
    }
  }

  private suspend fun <T> doMutate(
    type: MutateType,
    block: suspend () -> T,
  ): T {
    return _mutateMutex.withLock {
      withContext(MutateElement(type = type, mutator = this@MutatorImpl)) {
        block()
      }
    }
  }

  private fun newMutateScope(mutateContext: CoroutineContext): FMutator.MutateScope {
    return object : FMutator.MutateScope {
      override suspend fun ensureMutateActive() {
        currentCoroutineContext().ensureActive()
        mutateContext.ensureActive()
      }
    }
  }

  private suspend fun checkNestedMutate() {
    val element = currentCoroutineContext()[MutateElement]
    if (element?.mutator === this@MutatorImpl) {
      error("Already in ${element.type}")
    }
  }

  private class MutateElement(
    val type: MutateType,
    val mutator: FMutator,
  ) : AbstractCoroutineContextElement(MutateElement) {
    companion object Key : CoroutineContext.Key<MutateElement>
  }

  private enum class MutateType {
    Mutate,
    Effect,
  }
}