package com.sd.demo.kmp.paging

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sd.lib.kmp.paging.FPaging
import com.sd.lib.kmp.paging.compose.PagingPresenter
import com.sd.lib.kmp.paging.compose.UiRefreshSlot
import com.sd.lib.kmp.paging.compose.pagingItemAppend
import com.sd.lib.kmp.paging.compose.pagingItems
import com.sd.lib.kmp.paging.compose.presenter

@Composable
fun RouteSampleLazyGrid(
  onClickBack: () -> Unit,
) {
  val paging = remember {
    FPaging(
      refreshKey = 1,
      pagingSource = StringPagingSource(),
    )
  }

  RouteScaffold(
    title = "SampleLazyGrid",
    onClickBack = onClickBack,
  ) {
    Content(paging = paging.presenter())
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Content(
  modifier: Modifier = Modifier,
  paging: PagingPresenter<String>,
) {
  LaunchedEffect(paging) {
    paging.refresh()
  }

  PullToRefreshBox(
    modifier = modifier.fillMaxSize(),
    isRefreshing = paging.isRefreshing,
    onRefresh = { paging.refresh() },
    contentAlignment = Alignment.Center,
  ) {
    LazyView(
      modifier = Modifier.fillMaxSize(),
      paging = paging,
    )
    paging.UiRefreshSlot(
      stateError = { Text(text = "加载失败：$it") },
      stateEmpty = { Text(text = "暂无数据") },
    )
  }
}

@Composable
private fun LazyView(
  modifier: Modifier = Modifier,
  paging: PagingPresenter<String>,
) {
  LazyVerticalGrid(
    modifier = modifier.fillMaxSize(),
    columns = GridCells.Fixed(2),
    verticalArrangement = Arrangement.spacedBy(8.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    contentPadding = PaddingValues(8.dp),
  ) {
    pagingItems(paging) { item ->
      ItemView(text = item)
    }
    pagingItemAppend(paging)
  }
}