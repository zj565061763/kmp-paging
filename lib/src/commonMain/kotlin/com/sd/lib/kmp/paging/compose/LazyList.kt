package com.sd.lib.kmp.paging.compose

import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable

fun LazyListScope.pagingItemAppend(
  paging: PagingPresenter<*>,
  key: Any? = "paging append ui state",
  contentType: Any? = "paging append ui state",
  content: @Composable LazyItemScope.() -> Unit = { paging.UiAppend() },
) {
  if (paging.isEmpty) return
  item(
    key = key,
    contentType = contentType,
    content = content,
  )
}

fun <T : Any> LazyListScope.pagingItems(
  paging: PagingPresenter<T>,
  key: ((item: T) -> Any)? = null,
  contentType: (item: T) -> Any? = { null },
  itemContent: @Composable LazyItemScope.(item: T) -> Unit,
) {
  pagingItemsIndexed(
    paging = paging,
    key = if (key == null) null else { _, item -> key(item) },
    contentType = { _, item -> contentType(item) },
    itemContent = { _, item -> itemContent(item) },
  )
}

fun <T : Any> LazyListScope.pagingItemsIndexed(
  paging: PagingPresenter<T>,
  key: ((index: Int, item: T) -> Any)? = null,
  contentType: (index: Int, item: T) -> Any? = { _, _ -> null },
  itemContent: @Composable LazyItemScope.(index: Int, item: T) -> Unit,
) {
  itemsIndexed(
    items = paging.items,
    key = key,
    contentType = contentType,
  ) { index, item ->
    itemContent(index, item)
    PagingAppendIfLastIndex(paging, index)
  }
}