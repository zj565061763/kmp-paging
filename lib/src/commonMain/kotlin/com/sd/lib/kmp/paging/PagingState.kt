package com.sd.lib.kmp.paging

data class PagingState<T>(
  /** 数据 */
  val items: List<T> = emptyList(),

  /** Refresh加载状态 */
  val refreshLoadState: LoadState = LoadState.NotLoading.Incomplete,

  /** Append加载状态 */
  val appendLoadState: LoadState = LoadState.NotLoading.Incomplete,
)

/** 加载状态 */
sealed interface LoadState {
  data class NotLoading(
    /** 是否已到达末页 */
    val endOfPaginationReached: Boolean,
  ) : LoadState {
    internal companion object {
      val Incomplete = NotLoading(endOfPaginationReached = false)
      val Complete = NotLoading(endOfPaginationReached = true)
    }
  }

  /** 加载中 */
  data object Loading : LoadState

  /** 加载失败 */
  data class Error(
    val error: Throwable,
  ) : LoadState
}