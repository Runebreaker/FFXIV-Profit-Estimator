package com.example.ffxivprofitestimator

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CommonGreetingTest {

    @Test
    fun testExample() {
        assertTrue(App().greet().contains("Hello"), "Check 'Hello' is mentioned")
    }

    @Test
    fun secondTest() {
        assertEquals(2,2)
    }
}