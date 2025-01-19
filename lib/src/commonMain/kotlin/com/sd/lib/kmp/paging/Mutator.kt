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
import kotlin.coroutines.cancellation.CancellationException

internal class Mutator {
  private var _job: Job? = null
  private val _jobMutex = Mutex()
  private val _mutateMutex = Mutex()

  suspend fun <R> mutate(block: suspend MutateScope.() -> R): R {
    checkNested()
    return mutate(
      onStart = {},
      block = block,
    )
  }

  suspend fun <T> tryMutate(block: suspend MutateScope.() -> T): T {
    checkNested()
    return mutate(
      onStart = { if (_job?.isActive == true) throw CancellationException() },
      block = block,
    )
  }

  suspend fun <T> effect(block: suspend () -> T): T {
    checkNested()
    return doMutate(block)
  }

  suspend fun cancelMutate() {
    _jobMutex.withLock {
      _job?.cancelAndJoin()
    }
  }

  private suspend fun <R> mutate(
    onStart: () -> Unit,
    block: suspend MutateScope.() -> R,
  ): R {
    return coroutineScope {
      val mutateContext = coroutineContext
      val mutateJob = checkNotNull(mutateContext[Job])

      _jobMutex.withLock {
        onStart()
        _job?.cancelAndJoin()
        _job = mutateJob
      }

      try {
        doMutate {
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

  private suspend fun <T> doMutate(block: suspend () -> T): T {
    return _mutateMutex.withLock {
      withContext(MutateElement(mutator = this@Mutator)) {
        block()
      }
    }
  }

  private fun newMutateScope(mutateContext: CoroutineContext): MutateScope {
    return object : MutateScope {
      override suspend fun ensureMutateActive() {
        currentCoroutineContext().ensureActive()
        mutateContext.ensureActive()
      }
    }
  }

  private suspend fun checkNested() {
    val element = currentCoroutineContext()[MutateElement]
    if (element?.mutator === this@Mutator) error("Nested invoke")
  }

  private class MutateElement(val mutator: Mutator) : AbstractCoroutineContextElement(MutateElement) {
    companion object Key : CoroutineContext.Key<MutateElement>
  }

  interface MutateScope {
    suspend fun ensureMutateActive()
  }
}