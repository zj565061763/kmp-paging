package com.sd.demo.kmp.paging

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun App() {
  MaterialTheme {
    val navController = rememberNavController()
    NavHost(
      navController = navController,
      startDestination = AppRoute.Home,
    ) {
      composable<AppRoute.Home> {
        RouteHome(
          onClickSampleLazyColumn = { navController.navigate(AppRoute.SampleLazyColumn) },
          onClickSampleLazyGrid = { navController.navigate(AppRoute.SampleLazyGrid) },
          onClickSampleLazyStaggeredGrid = { navController.navigate(AppRoute.SampleLazyStaggeredGrid) },
        )
      }
      composable<AppRoute.SampleLazyColumn> {
        RouteSampleLazyColumn(
          onClickBack = { navController.popBackStack() }
        )
      }
      composable<AppRoute.SampleLazyGrid> {
        RouteSampleLazyGrid(
          onClickBack = { navController.popBackStack() }
        )
      }
      composable<AppRoute.SampleLazyStaggeredGrid> {
        RouteSampleLazyStaggeredGrid(
          onClickBack = { navController.popBackStack() }
        )
      }
    }
  }
}

expect fun logMsg(tag: String = "kmp-paging", block: () -> String)