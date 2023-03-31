package com.example.ffxivprofitestimator

import com.example.ffxivprofitestimator.Util.LRUCache
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class APITests {
    private val testAPI = TestAPI()
    val testItemName = "rinascita"
    val rinascitaSwordID = 37742
    val recipeID = 35026

    @Test
    fun testRateLimit() {
        runBlocking {
            println("Rate limit: ${testAPI.rateLimit}")
            for (i in 1..25)
            {
                testAPI.request { println("Request $i done at time ${testAPI.referenceTime}") }
                assertTrue(testAPI.requestsThisSecond <= testAPI.rateLimit)
            }
        }
    }

    @Test
    fun testUniversalisAPICalls() {
        runBlocking {
            val worlds = UniversalisAPI.getWorlds()
            assertNotNull(worlds)
            println("Found worlds: $worlds")
            val testingKeyPair = Pair(rinascitaSwordID, worlds.first().id)
            val dcs = UniversalisAPI.getDatacenters()
            assertNotNull(dcs)
            println("Found datacenters: $dcs")
            val historyView = UniversalisAPI.getHistoryView(testingKeyPair)
            assertNotNull(historyView)
            assertTrue(historyView.valid)
            println("Found history view: $historyView")
            val item = UniversalisAPI.getItem(testingKeyPair.first, testingKeyPair.second)
            assertNotNull(item)
            println("Found item: $item")
            assertContains(UniversalisAPI.getCachedItems(), testingKeyPair)
            assertNotNull(UniversalisAPI.getCachedItems()[testingKeyPair])
            println("Found item in cache: ${UniversalisAPI.getCachedItems()[testingKeyPair]}")
        }
    }

    @Test
    fun testXIVAPICalls() {
        runBlocking {
            val item = XIVAPI.getItem(rinascitaSwordID)
            assertNotNull(item)
            println("Found item $item")
            val imageByteArray = XIVAPI.getIconAsByteArray(item.icon)
            assertNotNull(imageByteArray)
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