package com.sd.lib.kmp.paging

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

@OptIn(ExperimentalCoroutinesApi::class)
open class MainDispatcherTest {
  @BeforeTest
  open fun beforeTest() {
    Dispatchers.setMain(StandardTestDispatcher())
  }

  @AfterTest
  open fun afterTest() {
    Dispatchers.resetMain()
  }
}