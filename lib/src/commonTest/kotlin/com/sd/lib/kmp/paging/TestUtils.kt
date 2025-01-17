package com.sd.lib.kmp.paging

import kotlin.test.assertEquals

fun PagingState<*>.testItemsEmpty() = assertEquals(true, items.isEmpty())
fun LoadState.testLoading() = assertEquals(LoadState.Loading, this)
fun LoadState.testError(message: String) = assertEquals(message, (this as LoadState.Error).error.message)
fun LoadState.testComplete() = assertEquals(true, (this as LoadState.NotLoading).endOfPaginationReached)
fun LoadState.testInComplete() = assertEquals(false, (this as LoadState.NotLoading).endOfPaginationReached)