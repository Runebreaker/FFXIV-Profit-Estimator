package com.example.ffxivprofitestimator

import kotlin.test.Test
import kotlin.test.assertTrue

class IosGreetingTest {

    @Test
    fun testExample() {
        assertTrue(App().greet().contains("iOS"), "Check iOS is mentioned")
    }
}