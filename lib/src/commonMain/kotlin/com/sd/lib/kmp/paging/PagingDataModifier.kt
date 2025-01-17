package com.sd.lib.kmp.paging

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

interface PagingDataModifier<T : Any> {
  suspend fun replaceFirst(block: (T) -> T)
  suspend fun replaceLast(block: (T) -> T)
  suspend fun replaceAll(block: (T) -> T)

  suspend fun removeFirst(predicate: (T) -> Boolean)
  suspend fun removeLast(predicate: (T) -> Boolean)
  suspend fun removeAll(predicate: (T) -> Boolean)

  suspend fun insert(index: Int, item: T)
  suspend fun insertAll(index: Int, items: Collection<T>)

  suspend fun modify(block: suspend (List<T>) -> List<T>)
}

fun <T : Any> FPaging<T>.modifier(
  dispatcher: CoroutineDispatcher = Dispatchers.IO,
): PagingDataModifier<T> {
  return PagingDataModifierImpl(
    paging = this,
    dispatcher = dispatcher,
  )
}

suspend fun <T : Any> PagingDataModifier<T>.replaceFirst(oldItem: T, newItem: T) =
  replaceFirst { if (it == oldItem) newItem else it }

suspend fun <T : Any> PagingDataModifier<T>.replaceLast(oldItem: T, newItem: T) =
  replaceLast { if (it == oldItem) newItem else it }

suspend fun <T : Any> PagingDataModifier<T>.replaceAll(oldItem: T, newItem: T) =
  replaceAll { if (it == oldItem) newItem else it }

private class PagingDataModifierImpl<T : Any>(
  private val paging: FPaging<T>,
  private val dispatcher: CoroutineDispatcher,
) : PagingDataModifier<T> {

  override suspend fun replaceFirst(block: (T) -> T) {
    modify { list ->
      var changed = false
      val mutableList = list.toMutableList()
      for (index in mutableList.indices) {
        val item = mutableList[index]
        val newItem = block(item)
        if (newItem != item) {
          mutableList[index] = newItem
          changed = true
          break
        }
      }
      if (changed) mutableList.toList() else list
    }
  }

  override suspend fun replaceLast(block: (T) -> T) {
    modify { list ->
      var changed = false
      val mutableList = list.toMutableList()
      for (index in mutableList.indices.reversed()) {
        val item = mutableList[index]
        val newItem = block(item)
        if (newItem != item) {
          mutableList[index] = newItem
          changed = true
          break
        }
      }
      if (changed) mutableList.toList() else list
    }
  }

  override suspend fun replaceAll(block: (T) -> T) {
    modify { list ->
      var changed = false
      val mutableList = list.toMutableList()
      for (index in mutableList.indices) {
        val item = mutableList[index]
        val newItem = block(item)
        if (newItem != item) {
          mutableList[index] = newItem
          changed = true
        }
      }
      if (changed) mutableList.toList() else list
    }
  }

  override suspend fun removeFirst(predicate: (T) -> Boolean) {
    modify { list ->
      val index = list.indexOfFirst(predicate)
      if (index < 0) {
        list
      } else {
        list.toMutableList()
          .apply { removeAt(index) }
          .toList()
      }
    }
  }

  override suspend fun removeLast(predicate: (T) -> Boolean) {
    modify { list ->
      val index = list.indexOfLast(predicate)
      if (index < 0) {
        list
      } else {
        list.toMutableList()
          .apply { removeAt(index) }
          .toList()
      }
    }
  }

  override suspend fun removeAll(predicate: (T) -> Boolean) {
    modify { list ->
      val mutableList = list.toMutableList()
      if (mutableList.removeAll(predicate)) mutableList.toList() else list
    }
  }

  override suspend fun insert(index: Int, item: T) {
    modify { list ->
      list.toMutableList()
        .apply { add(index, item) }
        .toList()
    }
  }

  override suspend fun insertAll(index: Int, items: Collection<T>) {
    modify { list ->
      list.toMutableList()
        .apply { addAll(index, items) }
        .toList()
    }
  }

  override suspend fun modify(block: suspend (List<T>) -> List<T>) {
    paging.modify { list ->
      withContext(dispatcher) {
        block(list)
      }
    }
  }
}