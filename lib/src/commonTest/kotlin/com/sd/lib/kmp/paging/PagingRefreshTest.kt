package com.sd.lib.kmp.paging

import app.cash.turbine.test
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class PagingRefreshTest : MainDispatcherTest() {

  @Test
  fun `test refresh success`() = runTest {
    val paging = testPaging()

    paging.stateFlow.test {
      // initial
      awaitItem().testInitial()

      paging.refresh()

      // refreshing
      awaitItem().testInitialRefreshing()

      // refresh success
      with(awaitItem()) {
        assertEquals(listOf("1"), items)
        refreshLoadState.testComplete()
        appendLoadState.testInComplete()
      }
    }
  }

  @Test
  fun `test refresh error`() = runTest {
    val paging = testPagingError(errorPage = 1)
    paging.stateFlow.test {
      // initial
      awaitItem().testInitial()

      paging.refresh()

      // refreshing
      awaitItem().testInitialRefreshing()

      // refresh error
      with(awaitItem()) {
        testItemsEmpty()
        refreshLoadState.testError("error")
        appendLoadState.testInComplete()
      }
    }
  }

  @Test
  fun `test refresh no more data`() = runTest {
    val paging = testPagingNoMoreData(noMoreDataPage = 1)
    paging.stateFlow.test {
      // initial
      awaitItem().testInitial()

      paging.refresh()

      // refreshing
      awaitItem().testInitialRefreshing()

      // refresh success
      with(awaitItem()) {
        testItemsEmpty()
        refreshLoadState.testComplete()
        appendLoadState.testComplete()
      }
    }
  }

  @Test
  fun `test refresh load none`() = runTest {
    val paging = testPagingLoadNone(loadNonePage = 1)
    paging.stateFlow.test {
      // initial
      awaitItem().testInitial()

      runCatching { paging.refresh() }.also {
        assertEquals(true, it.exceptionOrNull() is CancellationException)
      }

      // refreshing
      awaitItem().testInitialRefreshing()

      // refresh success
      awaitItem().testInitial()
    }
  }

  @Test
  fun `test refresh when refreshing`() = runTest {
    val paging = testPaging()

    paging.stateFlow.test {
      // initial
      awaitItem().testInitial()

      launch { paging.refresh() }.also { runCurrent() }
      paging.refresh()

      // refreshing
      awaitItem().testInitialRefreshing()

      // initial
      awaitItem().testInitial()

      // refreshing
      awaitItem().testInitialRefreshing()

      // refresh success
      with(awaitItem()) {
        assertEquals(listOf("1"), items)
        refreshLoadState.testComplete()
        appendLoadState.testInComplete()
      }
    }
  }

  @Test
  fun `test refresh when appending`() = runTest {
    val paging = testPaging()

    paging.stateFlow.test {
      // initial
      awaitItem().testInitial()

      paging.refresh()
      launch { paging.append() }.also { runCurrent() }
      paging.refresh()

      // refreshing
      awaitItem().testInitialRefreshing()
      // refresh success
      with(awaitItem()) {
        assertEquals(listOf("1"), items)
        refreshLoadState.testComplete()
        appendLoadState.testInComplete()
      }

      // appending
      with(awaitItem()) {
        assertEquals(listOf("1"), items)
        refreshLoadState.testComplete()
        appendLoadState.testLoading()
      }

      // refresh success
      with(awaitItem()) {
        assertEquals(listOf("1"), items)
        refreshLoadState.testComplete()
        appendLoadState.testInComplete()
      }

      // refreshing
      with(awaitItem()) {
        assertEquals(listOf("1"), items)
        refreshLoadState.testLoading()
        appendLoadState.testInComplete()
      }

      // refresh success
      with(awaitItem()) {
        assertEquals(listOf("1"), items)
        refreshLoadState.testComplete()
        appendLoadState.testInComplete()
      }
    }
  }
}

private fun <T : Any> PagingState<T>.testInitial() {
  testItemsEmpty()
  refreshLoadState.testInComplete()
  appendLoadState.testInComplete()
}

private fun <T : Any> PagingState<T>.testInitialRefreshing() {
  testItemsEmpty()
  refreshLoadState.testLoading()
  appendLoadState.testInComplete()
}