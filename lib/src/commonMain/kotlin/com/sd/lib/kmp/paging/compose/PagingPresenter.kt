package com.sd.lib.kmp.paging.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.sd.lib.kmp.paging.FPaging
import com.sd.lib.kmp.paging.LoadState
import com.sd.lib.kmp.paging.Paging
import com.sd.lib.kmp.paging.PagingState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Stable
interface PagingPresenter<T : Any> {
  /** 数据 */
  val items: List<T>

  /** Refresh加载状态 */
  val refreshLoadState: LoadState

  /** Append加载状态 */
  val appendLoadState: LoadState

  /** 是否正在[refresh] */
  val isRefreshing: Boolean

  /** 数据是否为空 */
  val isEmpty: Boolean

  /** [FPaging.refresh] */
  fun refresh()

  /** [FPaging.append] */
  fun append()
}

@Composable
fun <T : Any> Paging<T>.presenter(): PagingPresenter<T> {
  val paging = this
  require(paging is FPaging<T>) { "Require FPaging" }
  val coroutineScope = rememberCoroutineScope()
  return remember(paging, coroutineScope) {
    PagingPresenterImpl(
      paging = paging,
      coroutineScope = coroutineScope,
    )
  }.apply { Init() }
}

private class PagingPresenterImpl<T : Any>(
  private val paging: FPaging<T>,
  private val coroutineScope: CoroutineScope,
) : PagingPresenter<T> {
  private lateinit var _pagingState: State<PagingState<T>>
  private val _obsState: PagingState<T> get() = _pagingState.value

  private val _items by lazy { derivedStateOf { _obsState.items } }
  private val _refreshLoadState by lazy { derivedStateOf { _obsState.refreshLoadState } }
  private val _appendLoadState by lazy { derivedStateOf { _obsState.appendLoadState } }
  private val _isRefreshing by lazy { derivedStateOf { _obsState.refreshLoadState is LoadState.Loading } }
  private val _isEmpty by lazy { derivedStateOf { _obsState.items.isEmpty() } }

  override val items: List<T> get() = _items.value
  override val refreshLoadState: LoadState get() = _refreshLoadState.value
  override val appendLoadState: LoadState get() = _appendLoadState.value
  override val isRefreshing: Boolean get() = _isRefreshing.value
  override val isEmpty: Boolean get() = _isEmpty.value

  override fun refresh() {
    coroutineScope.launch { paging.refresh() }
  }

  override fun append() {
    coroutineScope.launch { paging.append() }
  }

  @Composable
  fun Init() {
    _pagingState = paging.stateFlow.collectAsState()
  }
}