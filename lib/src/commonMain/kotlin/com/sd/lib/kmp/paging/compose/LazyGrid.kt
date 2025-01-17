package com.sd.lib.kmp.paging.compose

import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable

fun LazyGridScope.pagingItemAppend(
  paging: PagingPresenter<*>,
  key: Any? = "paging append ui state",
  contentType: Any? = "paging append ui state",
  span: (LazyGridItemSpanScope.() -> GridItemSpan)? = { GridItemSpan(maxLineSpan) },
  content: @Composable LazyGridItemScope.() -> Unit = { paging.UiAppend() },
) {
  if (paging.isEmpty) return
  item(
    key = key,
    contentType = contentType,
    span = span,
    content = content,
  )
}

fun <T : Any> LazyGridScope.pagingItems(
  paging: PagingPresenter<T>,
  key: ((item: T) -> Any)? = null,
  contentType: (item: T) -> Any? = { null },
  span: (LazyGridItemSpanScope.(item: T) -> GridItemSpan)? = null,
  itemContent: @Composable LazyGridItemScope.(item: T) -> Unit,
) {
  pagingItemsIndexed(
    paging = paging,
    key = if (key == null) null else { _, item -> key(item) },
    contentType = { _, item -> contentType(item) },
    span = if (span == null) null else { _, item -> span(item) },
    itemContent = { _, item -> itemContent(item) },
  )
}

fun <T : Any> LazyGridScope.pagingItemsIndexed(
  paging: PagingPresenter<T>,
  key: ((index: Int, item: T) -> Any)? = null,
  contentType: (index: Int, item: T) -> Any? = { _, _ -> null },
  span: (LazyGridItemSpanScope.(index: Int, item: T) -> GridItemSpan)? = null,
  itemContent: @Composable LazyGridItemScope.(index: Int, item: T) -> Unit,
) {
  itemsIndexed(
    items = paging.items,
    key = key,
    contentType = contentType,
    span = span,
  ) { index, item ->
    itemContent(index, item)
    PagingAppendIfLastIndex(paging, index)
  }
}