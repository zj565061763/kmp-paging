package com.sd.demo.kmp.paging

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun RouteHome(
  modifier: Modifier = Modifier,
  onClickSampleLazyColumn: () -> Unit,
  onClickSampleLazyGrid: () -> Unit,
  onClickSampleLazyStaggeredGrid: () -> Unit,
) {
  Scaffold(modifier = modifier.fillMaxSize()) { padding ->
    Column(
      modifier = Modifier.fillMaxSize().padding(padding),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Button(onClick = onClickSampleLazyColumn) {
        Text(text = "RouteSampleLazyColumn")
      }
      Button(onClick = onClickSampleLazyGrid) {
        Text(text = "RouteSampleLazyGrid")
      }
      Button(onClick = onClickSampleLazyStaggeredGrid) {
        Text(text = "RouteSampleLazyStaggeredGrid")
      }
    }
  }
}