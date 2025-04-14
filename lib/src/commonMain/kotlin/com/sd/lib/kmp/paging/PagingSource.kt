package com.sd.lib.kmp.paging

/** 页码类型为[Int]的数据源 */
abstract class KeyIntPagingSource<Value : Any>(
  private val hasNextPageSize: Int = 1,
) : PagingSource<Int, Value>() {
  final override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Value> {
    val data = loadImpl(params) ?: return LoadResult.None()
    val hasNextPage = data.size >= hasNextPageSize
    return LoadResult.Page(
      data = data,
      nextKey = if (hasNextPage) params.key + 1 else null,
    )
  }

  /**
   * 加载并返回分页数据列表
   * - 返回null，表示本次加载无效
   * - 返回列表数量小于[hasNextPageSize]，表示没有下一页数据
   */
  protected abstract suspend fun loadImpl(params: LoadParams<Int>): List<Value>?
}

/**
 * 数据源
 * - [Key]表示页码类型
 * - [Value]表示数据类型
 */
abstract class PagingSource<Key : Any, Value : Any> {
  /**
   * 根据参数[params]加载数据，并返回结果[LoadResult]
   */
  abstract suspend fun load(params: LoadParams<Key>): LoadResult<Key, Value>
}

/** 加载参数 */
sealed interface LoadParams<Key : Any> {
  /** 页码 */
  val key: Key

  /** 刷新数据 */
  data class Refresh<Key : Any>(
    override val key: Key,
  ) : LoadParams<Key>

  /** 加载尾部数据 */
  data class Append<Key : Any>(
    override val key: Key,
  ) : LoadParams<Key>
}

/** 加载结果 */
sealed interface LoadResult<Key : Any, Value : Any> {
  data class Page<Key : Any, Value : Any>(
    /** 分页数据 */
    val data: List<Value>,
    /** 下一页的页码，null表示没有下一页 */
    val nextKey: Key?,
  ) : LoadResult<Key, Value>

  /** 加载无效 */
  class None<Key : Any, Value : Any> : LoadResult<Key, Value>
}