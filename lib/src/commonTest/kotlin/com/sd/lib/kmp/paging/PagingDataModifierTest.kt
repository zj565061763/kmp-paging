package com.sd.lib.kmp.paging

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class PagingDataModifierTest : MainDispatcherTest() {

  @Test
  fun `test replaceFirst`() = runTest {
    testModifier { paging, modifier ->
      modifier.replaceFirst("1", "a")
      assertEquals(
        listOf(
          "a", "2", "3",
          "1", "2", "3",
          "1", "2", "3",
        ),
        paging.state.items,
      )
    }
  }

  @Test
  fun `test replaceLast`() = runTest {
    testModifier { paging, modifier ->
      modifier.replaceLast("1", "a")
      assertEquals(
        listOf(
          "1", "2", "3",
          "1", "2", "3",
          "a", "2", "3",
        ),
        paging.state.items,
      )
    }
  }

  @Test
  fun `test replaceAll`() = runTest {
    testModifier { paging, modifier ->
      modifier.replaceAll("1", "a")
      assertEquals(
        listOf(
          "a", "2", "3",
          "a", "2", "3",
          "a", "2", "3",
        ),
        paging.state.items,
      )
    }
  }

  @Test
  fun `test removeFirst`() = runTest {
    testModifier { paging, modifier ->
      modifier.removeFirst { it == "1" }
      assertEquals(
        listOf(
          "2", "3",
          "1", "2", "3",
          "1", "2", "3",
        ),
        paging.state.items,
      )
    }
  }

  @Test
  fun `test removeLast`() = runTest {
    testModifier { paging, modifier ->
      modifier.removeLast { it == "1" }
      assertEquals(
        listOf(
          "1", "2", "3",
          "1", "2", "3",
          "2", "3",
        ),
        paging.state.items,
      )
    }
  }

  @Test
  fun `test removeAll`() = runTest {
    testModifier { paging, modifier ->
      modifier.removeAll { it == "1" }
      assertEquals(
        listOf(
          "2", "3",
          "2", "3",
          "2", "3",
        ),
        paging.state.items,
      )
    }
  }

  @Test
  fun `test insert`() = runTest {
    testModifier { paging, modifier ->
      modifier.insert(0, "a")
      assertEquals(
        listOf(
          "a",
          "1", "2", "3",
          "1", "2", "3",
          "1", "2", "3",
        ),
        paging.state.items,
      )
    }
  }

  @Test
  fun `test insertAll`() = runTest {
    testModifier { paging, modifier ->
      modifier.insertAll(0, listOf("a", "b", "c"))
      assertEquals(
        listOf(
          "a", "b", "c",
          "1", "2", "3",
          "1", "2", "3",
          "1", "2", "3",
        ),
        paging.state.items,
      )
    }
  }
}

private suspend fun testModifier(
  block: suspend (paging: FPaging<String>, modifier: PagingDataModifier<String>) -> Unit,
) {
  val paging = FPaging(refreshKey = 1, pagingSource = PagingDataModifierTestPagingSource())
  paging.refresh()

  val modifier = paging.modifier()
  block(paging, modifier)
}

private class PagingDataModifierTestPagingSource : KeyIntPagingSource<String>() {
  override suspend fun loadImpl(params: LoadParams<Int>): List<String> {
    return listOf(
      "1", "2", "3",
      "1", "2", "3",
      "1", "2", "3",
    )
  }
}