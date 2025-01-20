package com.sd.lib.kmp.paging.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sd.lib.kmp.paging.LoadState
import com.sd.lib.kmp.paging.generated.resources.Res
import com.sd.lib.kmp.paging.generated.resources.lib_paging_append_load_error
import com.sd.lib.kmp.paging.generated.resources.lib_paging_append_no_more_data
import org.jetbrains.compose.resources.stringResource

@Composable
fun PagingPresenter<*>.UiAppend(
  modifier: Modifier = Modifier,
  /** 加载中 */
  stateLoading: @Composable () -> Unit = { StateLoading() },
  /** 加载错误 */
  stateError: @Composable (Throwable) -> Unit = { StateError { append() } },
  /** 没有更多数据 */
  stateNoMoreData: @Composable () -> Unit = { StateNoMoreData { append() } },
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .heightIn(48.dp)
      .padding(5.dp),
    contentAlignment = Alignment.Center,
  ) {
    UiAppendSlot(
      stateLoading = stateLoading,
      stateError = stateError,
      stateNoMoreData = stateNoMoreData,
    )
  }
}

@Composable
fun PagingPresenter<*>.UiAppendSlot(
  /** 加载中 */
  stateLoading: @Composable () -> Unit = {},
  /** 加载错误 */
  stateError: @Composable (Throwable) -> Unit = {},
  /** 没有更多数据 */
  stateNoMoreData: @Composable () -> Unit = {},
) {
  if (!isEmpty) {
    when (val loadState = appendLoadState) {
      is LoadState.Loading -> stateLoading()
      is LoadState.Error -> stateError(loadState.error)
      is LoadState.NotLoading -> if (loadState.endOfPaginationReached) stateNoMoreData()
    }
  }
}

@Composable
private fun StateLoading(
  modifier: Modifier = Modifier,
) {
  CircularProgressIndicator(
    modifier = modifier.size(24.dp),
    strokeWidth = 2.dp,
    color = MaterialTheme.colorScheme.onSurface,
  )
}

@Composable
private fun StateError(
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
) {
  Text(
    modifier = modifier
      .clickable { onClick() }
      .padding(horizontal = 8.dp, vertical = 4.dp),
    text = stringResource(Res.string.lib_paging_append_load_error),
    fontSize = 12.sp,
    color = MaterialTheme.colorScheme.onSurface.copy(0.5f),
  )
}

@Composable
private fun StateNoMoreData(
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
) {
  Text(
    modifier = modifier
      .clickable { onClick() }
      .padding(horizontal = 8.dp, vertical = 4.dp),
    text = stringResource(Res.string.lib_paging_append_no_more_data),
    fontSize = 12.sp,
    color = MaterialTheme.colorScheme.onSurface.copy(0.5f),
  )
}