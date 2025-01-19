package com.sd.lib.kmp.paging

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class PagingInModifyBlockTest : MainDispatcherTest() {

  @Test
  fun `test modify`() = runTest {
    val paging = testPaging()
    paging.modify { listOf("a") }
    assertEquals(listOf("a"), paging.state.items)
  }

  @Test
  fun `test modify in modify block`() = runTest {
    val paging = testPaging()
    paging.modify {
      runCatching {
        paging.modify { listOf("b") }
      }.also {
        assertEquals("Nested invoke", it.exceptionOrNull()!!.message)
      }
      listOf("a")
    }
    assertEquals(listOf("a"), paging.state.items)
  }

  @Test
  fun `test refresh in modify block`() = runTest {
    val paging = testPaging()
    paging.modify {
      runCatching {
        paging.refresh()
      }.also {
        assertEquals("Nested invoke", it.exceptionOrNull()!!.message)
      }
      listOf("a")
    }
    assertEquals(listOf("a"), paging.state.items)
  }

  @Test
  fun `test append in modify block`() = runTest {
    val paging = testPaging()
    paging.modify {
      runCatching {
        paging.append()
      }.also {
        assertEquals("Nested invoke", it.exceptionOrNull()!!.message)
      }
      listOf("a")
    }
    assertEquals(listOf("a"), paging.state.items)
  }
}