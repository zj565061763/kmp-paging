package com.sd.lib.kmp.paging.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

  var noneLoadingState: LoadState? by remember { mutableStateOf(null) }

  val loadState = refreshLoadState
  if (loadState is LoadState.Loading) {
    stateLoading()
  } else {
    noneLoadingState = loadState
  }

  noneLoadingState?.also { state ->
    when (state) {
      is LoadState.Error -> stateError(state.error)
      is LoadState.NotLoading -> if (state.endOfPaginationReached) stateEmpty()
      is LoadState.Loading -> error("Require none loading state")
    }
  }
}