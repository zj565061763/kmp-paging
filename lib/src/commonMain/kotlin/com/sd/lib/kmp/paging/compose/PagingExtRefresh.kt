package com.sd.lib.kmp.paging.compose

import androidx.compose.runtime.Composable
import com.sd.lib.kmp.paging.LoadState

@Composable
fun PagingPresenter<*>.UiRefreshSlot(
  /** 加载中 */
  stateLoading: @Composable () -> Unit = {},
  /** 加载错误 */
  stateError: @Composable (Throwable) -> Unit = {},
  /** 数据为空 */
  stateEmpty: @Composable () -> Unit = {},
) {
  if (!isEmpty) return
  when (val loadState = refreshLoadState) {
    is LoadState.Loading -> stateLoading()
    is LoadState.Error -> stateError(loadState.error)
    is LoadState.NotLoading -> if (loadState.endOfPaginationReached) stateEmpty()
  }
}