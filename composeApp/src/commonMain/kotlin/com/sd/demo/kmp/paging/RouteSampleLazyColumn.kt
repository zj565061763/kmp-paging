package com.sd.demo.kmp.paging

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sd.lib.kmp.paging.FPaging
import com.sd.lib.kmp.paging.compose.PagingPresenter
import com.sd.lib.kmp.paging.compose.UiRefreshSlot
import com.sd.lib.kmp.paging.compose.pagingItemAppend
import com.sd.lib.kmp.paging.compose.pagingItems
import com.sd.lib.kmp.paging.compose.presenter
import com.sd.lib.kmp.paging.modifier
import com.sd.lib.kmp.paging.replaceAll
import kotlinx.coroutines.launch

@Composable
fun RouteSampleLazyColumn(
  onClickBack: () -> Unit,
) {
  val paging = remember {
    FPaging(
      refreshKey = 1,
      pagingSource = StringPagingSource(),
    )
  }

  val coroutineScope = rememberCoroutineScope()

  RouteScaffold(
    title = "SampleLazyColumn",
    onClickBack = onClickBack,
  ) {
    Content(
      paging = paging.presenter(),
      onClickItem = { item ->
        coroutineScope.launch {
          paging.modifier().replaceAll(oldItem = item, newItem = "modify")
        }
      },
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Content(
  modifier: Modifier = Modifier,
  paging: PagingPresenter<String>,
  onClickItem: (String) -> Unit,
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
      onClickItem = onClickItem,
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
  onClickItem: (String) -> Unit,
) {
  LazyColumn(
    modifier = modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(8.dp),
    contentPadding = PaddingValues(8.dp),
  ) {
    pagingItems(paging) { item ->
      ItemView(
        text = item,
        modifier = Modifier.clickable { onClickItem(item) },
      )
    }
    pagingItemAppend(paging)
  }
}