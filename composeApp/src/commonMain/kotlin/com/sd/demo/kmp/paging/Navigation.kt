package com.sd.demo.kmp.paging

import kotlinx.serialization.Serializable

sealed interface AppRoute {
  @Serializable
  data object Home : AppRoute

  @Serializable
  data object SampleLazyColumn : AppRoute

  @Serializable
  data object SampleLazyGrid : AppRoute

  @Serializable
  data object SampleLazyStaggeredGrid : AppRoute
}