package com.example.ffxivprofitestimator

import com.example.ffxivprofitestimator.Util.LRUCache
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class APITests {
    @Test
    fun testRateLimit() {
        val testAPI = TestAPI()
        runBlocking {
            println("Rate limit: ${testAPI.rateLimit}")
            for (i in 1..25)
            {
                testAPI.request { println("Request $i done at time ${testAPI.referenceTime}") }
                assertTrue(testAPI.requestsThisSecond <= testAPI.rateLimit)
            }
        }
    }
}

class CacheTests {
    private val cacheSize = 5
    private val testCache: LRUCache<Int, String> = LRUCache(cacheSize)

    @Test
    fun testLRUCache() {
        for (i in 1..12)
        {
            testCache[i] = i.toString()
            assertTrue(testCache.size() <= cacheSize)
            assertEquals(testCache.first(), i.toString())
            val list = testCache.getEntries().entries.mapIndexed { index, mutableEntry ->
                index + 1 to mutableEntry.value
            }
            println("Item $i in: ${list.map { "${it.first}=${it.second}" }}")
            println("Actual size: ${testCache.size()}")
        }

        println(testCache.getEntries().entries)
    }

    @Test
    fun testLRUFunctions() {
        for (i in 1..5) {
            testCache[i] = i.toString()
        }
        val elements = testCache.getEntries().entries
        println("Before: $elements")
        val lastElement = elements.first()
        testCache.moveToFront(lastElement.key)
        assertEquals(testCache.first(), lastElement.value)
        println("After: ${testCache.getEntries().entries}")
    }
}

class TestAPI: API() {
    override val baseURL: String = "test"
    override val rateLimit: Int = 20
}