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
class PagingAppendTest : MainDispatcherTest() {

  @Test
  fun `test append success`() = runTest {
    val paging = testPaging()
    paging.refresh()

    paging.stateFlow.test {
      // initial
      awaitItem().testInitialRefreshSuccess()

      paging.append()

      // appending
      awaitItem().testInitialAppending()

      // append success
      with(awaitItem()) {
        assertEquals(listOf("1", "2"), items)
        appendLoadState.testInComplete()
        refreshLoadState.testComplete()
      }
    }
  }

  @Test
  fun `test append error`() = runTest {
    val paging = testPagingError(errorPage = 2)
    paging.refresh()

    paging.stateFlow.test {
      // initial
      awaitItem().testInitialRefreshSuccess()

      paging.append()

      // appending
      awaitItem().testInitialAppending()

      // append error
      with(awaitItem()) {
        assertEquals(listOf("1"), items)
        appendLoadState.testError("error")
        refreshLoadState.testComplete()
      }
    }
  }

  @Test
  fun `test append no more data`() = runTest {
    val paging = testPagingNoMoreData(noMoreDataPage = 2)
    paging.refresh()

    paging.stateFlow.test {
      // initial
      awaitItem().testInitialRefreshSuccess()

      paging.append()

      // appending
      awaitItem().testInitialAppending()

      // append success
      with(awaitItem()) {
        assertEquals(listOf("1"), items)
        appendLoadState.testComplete()
        refreshLoadState.testComplete()
      }
    }
  }

  @Test
  fun `test append load none`() = runTest {
    val paging = testPagingLoadNone(loadNonePage = 2)
    paging.refresh()

    paging.stateFlow.test {
      // initial
      awaitItem().testInitialRefreshSuccess()

      runCatching { paging.append() }.also {
        assertEquals(true, it.exceptionOrNull() is CancellationException)
      }

      // appending
      awaitItem().testInitialAppending()

      // initial
      awaitItem().testInitialRefreshSuccess()
    }
  }

  @Test
  fun `test append when appending`() = runTest {
    val paging = testPaging()
    paging.refresh()

    paging.stateFlow.test {
      // initial
      awaitItem().testInitialRefreshSuccess()

      launch { paging.append() }.also { runCurrent() }
      runCatching { paging.append() }.also {
        assertEquals(true, it.exceptionOrNull() is CancellationException)
      }

      // appending
      awaitItem().testInitialAppending()

      // append success
      with(awaitItem()) {
        assertEquals(listOf("1", "2"), items)
        refreshLoadState.testComplete()
        appendLoadState.testInComplete()
      }
    }
  }

  @Test
  fun `test append when refreshing`() = runTest {
    val paging = testPaging()
    launch { paging.refresh() }.also { runCurrent() }

    paging.stateFlow.test {
      runCatching { paging.append() }.also {
        assertEquals(true, it.exceptionOrNull() is CancellationException)
      }

      // refreshing
      with(awaitItem()) {
        testItemsEmpty()
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

  @Test
  fun `test append to refresh`() = runTest {
    val paging = testPaging()

    paging.stateFlow.test {
      paging.append()

      // initial
      with(awaitItem()) {
        testItemsEmpty()
        refreshLoadState.testInComplete()
        appendLoadState.testInComplete()
      }

      // refreshing
      with(awaitItem()) {
        testItemsEmpty()
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

fun PagingState<String>.testInitialRefreshSuccess() {
  assertEquals(listOf("1"), items)
  refreshLoadState.testComplete()
  appendLoadState.testInComplete()
}

fun PagingState<String>.testInitialAppending() {
  assertEquals(listOf("1"), items)
  refreshLoadState.testComplete()
  appendLoadState.testLoading()
}