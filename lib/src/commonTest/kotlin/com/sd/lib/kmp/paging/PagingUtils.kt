package com.sd.lib.kmp.paging

import kotlinx.coroutines.delay

fun testPaging(): FPaging<String> {
  return FPaging(
    refreshKey = 1,
    pagingSource = TestPagingSource(),
  )
}

fun testPagingError(errorPage: Int): FPaging<String> {
  require(errorPage > 0)
  return FPaging(
    refreshKey = 1,
    pagingSource = TestErrorPagingSource(errorPage = errorPage),
  )
}

fun testPagingNoMoreData(noMoreDataPage: Int): FPaging<String> {
  require(noMoreDataPage > 0)
  return FPaging(
    refreshKey = 1,
    pagingSource = TestNoMoreDataPagingSource(noMoreDataPage = noMoreDataPage),
  )
}

fun testPagingLoadNone(loadNonePage: Int): FPaging<String> {
  require(loadNonePage > 0)
  return FPaging(
    refreshKey = 1,
    pagingSource = TestLoadNonePagingSource(loadNonePage = loadNonePage),
  )
}

private class TestPagingSource : KeyIntPagingSource<String>() {
  override suspend fun loadImpl(params: LoadParams<Int>): List<String> {
    delay(5_000)
    return listOf(params.key.toString())
  }
}

private class TestErrorPagingSource(
  private val errorPage: Int,
) : KeyIntPagingSource<String>() {
  override suspend fun loadImpl(params: LoadParams<Int>): List<String> {
    delay(5_000)
    if (params.key == errorPage) error("error")
    return listOf(params.key.toString())
  }
}

private class TestNoMoreDataPagingSource(
  private val noMoreDataPage: Int,
) : KeyIntPagingSource<String>() {
  override suspend fun loadImpl(params: LoadParams<Int>): List<String> {
    delay(5_000)
    return if (params.key >= noMoreDataPage) {
      emptyList()
    } else {
      listOf(params.key.toString())
    }
  }
}

private class TestLoadNonePagingSource(
  private val loadNonePage: Int,
) : KeyIntPagingSource<String>() {
  override suspend fun loadImpl(params: LoadParams<Int>): List<String>? {
    delay(5_000)
    return if (params.key == loadNonePage) {
      null
    } else {
      listOf(params.key.toString())
    }
  }
}