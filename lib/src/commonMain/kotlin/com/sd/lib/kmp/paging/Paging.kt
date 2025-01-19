package com.sd.lib.kmp.paging

import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException

interface Paging<T : Any>

interface FPaging<T : Any> : Paging<T> {
  /** 状态 */
  val state: PagingState<T>

  /** 状态流 */
  val stateFlow: StateFlow<PagingState<T>>

  /**
   * 刷新数据，如果当前正在[refresh]或者[append]，会先取消再刷新
   */
  suspend fun refresh()

  /**
   * 加载尾部数据，如果当前正在[refresh]或者[append]，会抛出[CancellationException]，
   * 如果调用时数据为空，会转发到[refresh]
   */
  suspend fun append()

  /**
   * 修改数据
   */
  suspend fun modify(block: suspend (List<T>) -> List<T>)
}

/**
 * 创建[FPaging]
 *
 * @param refreshKey 刷新数据的页码
 * @param pagingDataHandler [PagingDataHandler]
 */
fun <Key : Any, Value : Any> FPaging(
  refreshKey: Key,
  pagingSource: PagingSource<Key, Value>,
  pagingDataHandler: PagingDataHandler<Key, Value> = DefaultPagingDataHandler(),
): FPaging<Value> {
  return PagingImpl(
    refreshKey = refreshKey,
    pagingSource = pagingSource,
    pagingDataHandler = pagingDataHandler,
  )
}

//-------------------- impl --------------------

private class PagingImpl<Key : Any, Value : Any>(
  private val refreshKey: Key,
  private val pagingSource: PagingSource<Key, Value>,
  private val pagingDataHandler: PagingDataHandler<Key, Value>,
) : FPaging<Value> {
  private val _mutator = Mutator()
  private val _stateFlow = MutableStateFlow(PagingState<Value>())

  private var _nextKey: Key? = null

  override val state: PagingState<Value> get() = _stateFlow.value
  override val stateFlow: StateFlow<PagingState<Value>> get() = _stateFlow.asStateFlow()

  override suspend fun refresh() {
    _mutator.mutate {
      doRefresh()
    }
  }

  override suspend fun append() {
    _mutator.tryMutate {
      if (state.items.isEmpty()) {
        doRefresh()
      } else {
        doAppend()
      }
    }
  }

  override suspend fun modify(block: suspend (List<Value>) -> List<Value>) {
    _mutator.effect {
      val newItems = block(state.items)
      _stateFlow.update { it.copy(items = newItems) }
    }
  }

  private suspend fun Mutator.MutateScope.doRefresh() {
    val oldLoadState = state.refreshLoadState.also { check(it !is LoadState.Loading) }
    _stateFlow.update { it.copy(refreshLoadState = LoadState.Loading) }
    loadAndHandle(LoadParams.Refresh(refreshKey))
      .onSuccess { data ->
        val (loadResult, items) = data
        _nextKey = loadResult.nextKey
        _stateFlow.update {
          it.copy(
            items = items,
            refreshLoadState = LoadState.NotLoading.Complete,
            appendLoadState = if (loadResult.nextKey == null) LoadState.NotLoading.Complete else LoadState.NotLoading.Incomplete,
          )
        }
      }
      .onFailure { error ->
        if (error is CancellationException) {
          _stateFlow.update { it.copy(refreshLoadState = oldLoadState) }
          throw error
        } else {
          _stateFlow.update { it.copy(refreshLoadState = LoadState.Error(error)) }
        }
      }
  }

  private suspend fun Mutator.MutateScope.doAppend() {
    val appendKey = _nextKey ?: return
    val oldLoadState = state.appendLoadState.also { check(it !is LoadState.Loading) }
    _stateFlow.update { it.copy(appendLoadState = LoadState.Loading) }
    loadAndHandle(LoadParams.Append(appendKey))
      .onSuccess { data ->
        val (loadResult, items) = data
        loadResult.nextKey?.also { _nextKey = it }
        _stateFlow.update {
          it.copy(
            items = items,
            appendLoadState = if (loadResult.nextKey == null) LoadState.NotLoading.Complete else LoadState.NotLoading.Incomplete,
          )
        }
      }
      .onFailure { error ->
        if (error is CancellationException) {
          _stateFlow.update { it.copy(appendLoadState = oldLoadState) }
          throw error
        } else {
          _stateFlow.update { it.copy(appendLoadState = LoadState.Error(error)) }
        }
      }
  }

  /** 加载分页数据，并返回总数据 */
  private suspend fun Mutator.MutateScope.loadAndHandle(loadParams: LoadParams<Key>)
    : Result<Pair<LoadResult.Page<Key, Value>, List<Value>>> {
    return runCatching {
      val loadResult = pagingSource.load(loadParams).also { ensureMutateActive() }
      when (loadResult) {
        is LoadResult.None -> throw CancellationException()
        is LoadResult.Page -> {
          pagingDataHandler.handlePageData(
            totalData = state.items,
            params = loadParams,
            pageData = loadResult.data,
          ).let { items ->
            ensureMutateActive()
            loadResult to items
          }
        }
      }
    }
  }
}

private class Mutator {
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