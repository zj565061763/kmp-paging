package com.sd.demo.kmp.paging

import com.sd.lib.kmp.paging.KeyIntPagingSource
import com.sd.lib.kmp.paging.LoadParams
import kotlinx.coroutines.delay

class StringPagingSource(
  /** 加载错误的页码 */
  private val errorPage: Int = 5,
) : KeyIntPagingSource<String>() {
  private var _isFirstRefresh = true
  private var _errorLoad: Load = Load.Error

  init {
    require(errorPage > 1)
  }

  override suspend fun loadImpl(params: LoadParams<Int>): List<String> {
    logMsg { "load $params" }
    delay(500)
    return when (params.key) {
      1 -> getRefreshLoad()
      errorPage -> getErrorLoad()
      (errorPage + 1) -> Load.Empty.getList()
      else -> Load.List.getList()
    }
  }

  private fun getRefreshLoad(): List<String> {
    return when {
      _isFirstRefresh -> {
        _isFirstRefresh = false
        listOf(Load.Error, Load.Empty).random().getList()
      }
      else -> List(10) { "A" }
    }
  }

  private fun getErrorLoad(): List<String> {
    val errorLoad = _errorLoad
    _errorLoad = when (errorLoad) {
      Load.Error -> Load.List
      Load.List -> Load.Error
      else -> errorLoad
    }
    return errorLoad.getList()
  }

  private fun Load.getList(): List<String> {
    return when (this) {
      Load.List -> List(10) { (it + 1).toString() }
      Load.Error -> error("load error")
      Load.Empty -> emptyList()
    }
  }
}

private enum class Load {
  List,
  Error,
  Empty,
}