package com.example.ffxivprofitestimator

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.datetime.*
import com.example.ffxivprofitestimator.Util.LRUCache
import com.jetbrains.handson.kmm.shared.entity.*
import com.jetbrains.handson.kmm.shared.cache.Database
import com.jetbrains.handson.kmm.shared.cache.DatabaseDriverFactory
import io.ktor.http.*
import kotlinx.coroutines.delay

class App(databaseDriverFactory: DatabaseDriverFactory) {
    private val rifleScope = CoroutineScope(SupervisorJob())
    private val platform: Platform = getPlatform()

    private val database = Database(databaseDriverFactory)

    val testItemName = "rinascita"
    val rinascitaSwordID = 37742
    val recipeID = 35026

    fun greet(): String {
        rifleScope.launch {
            XIVAPI.getItem(rinascitaSwordID)?.icon
                ?: println("Couldn't find item.")
        }
        return "Hello, ${platform.name}!"
    }

    /**
     * Updates the database by retrieving new information from Universalis.
     * @return True, if DB was updated. False, if some error occurred retrieving the info.
     */
    suspend fun updateDB(force: Boolean = false): Boolean {
        if (database.getDatacenters().isNotEmpty() && database.getWorlds().isNotEmpty() && !force) return false
        database.clearDB()
        val datacenters = UniversalisAPI.getDatacenters() ?: return false
        val worlds = UniversalisAPI.getWorlds() ?: return false
        database.fillDB(datacenters, worlds)
        return true
    }

    fun getWorldsOfDatacenter(dcName: String): List<World> =
        database.getWorldsOfDatacenter(dcName)

    fun getDatacenters(): List<DataCenter> =
        database.getDatacenters()

    fun getWorlds(): List<World> =
        database.getWorlds()
}

object XIVAPI : API() {
    override val baseURL = "https://xivapi.com"
    override val rateLimit: Int = 20
    private val itemCache: LRUCache<Int, Item> = LRUCache(20)
    private val iconCache: LRUCache<Int, ByteArray> = LRUCache(20)

    private val filters: Map<String, Any> = mapOf(
        "columns" to "ID,Icon,Name,GameContentLinks.Recipe.ItemResult",
        "snake_case" to 1
    )

    /**
     * Get the item with the specified id from the cache, or from XIVAPI, if not present.
     * @return The Item, if it exists or null otherwise.
     * @param id The specified ID of the Item you want to retrieve.
     */
    suspend fun getItem(id: Int): Item? {
        if (!itemCache.containsKey(id)) {
            var requestURL = "$baseURL/item/$id"
            if (filters.isNotEmpty()) requestURL += "?" + filters.map { "${it.key}=${it.value}" }
                .joinToString("&")

            itemCache[id] = request {
                    httpClient ->
                httpClient.get(requestURL).body()
            } ?: return null
            iconCache[id] = request { httpClient ->
                httpClient.get("$baseURL${itemCache[id]?.icon ?: return@request null}").body()
            } ?: return null
        }
        return itemCache[id]
    }

    /**
     * Retrieves an image at a given URL as a ByteArray.
     * @return The byte array, if successful.
     * @param url The url of the image.
     */
    suspend fun getIconAsByteArray(url: Url): ByteArray? =
        request { httpClient ->
            httpClient.get("").body()
        }
}

object UniversalisAPI : API() {
    override val baseURL: String = "https://universalis.app/api/v2"
    override val rateLimit: Int = 25
    private val historyCache: LRUCache<Int, HistoryView> = LRUCache(20)

    suspend fun getItem(id: Int): HistoryView? {
        if (!historyCache.moveToFront(id)) return null
        getHistoryView(id)?.let { historyView ->
            historyCache[id] = historyView
        } ?: return null
        return historyCache.first()
    }

    fun getCachedItems(): LinkedHashMap<Int, HistoryView> =
        historyCache.getEntries()

    private suspend fun getHistoryView(id: Int): HistoryView? =
        request { httpClient ->
            httpClient.get("$baseURL/history/$id").body()
        }

    suspend fun getDatacenters(): List<DataCenter>? =
        request { httpClient ->
            httpClient.get("$baseURL/data-centers").body()
        }

    suspend fun getWorlds(): List<World>? =
        request { httpClient ->
            httpClient.get("$baseURL/worlds").body()
        }
}

/**
 * A small baseline for API implementations. Includes rate limiting. To make requests,
 * just use request { httpClient -> ...code... } to get access to the http client
 * and to have automatic enforcement of the rate limit.
 */
abstract class API {
    /**
     * Base URL of the used API
     */
    abstract val baseURL: String

    /**
     * API Rate limit in requests per second
     */
    abstract val rateLimit: Int

    var requestsThisSecond: Int = 0
        private set
    var referenceTime: Long = 0

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                useAlternativeNames = false
            })
        }
    }

    suspend fun <T> request(requestLambda: suspend (httpClient: HttpClient) -> T?): T? {
        //region rate limitation
        var currentTime = Clock.System.now().toEpochMilliseconds()
        var millisUntilReset = referenceTime + 1000 - currentTime
        // Delay until new rate limit window with a small buffer
        if (requestsThisSecond >= rateLimit) {
            delay(millisUntilReset + 100)
            currentTime = Clock.System.now().toEpochMilliseconds()
            millisUntilReset = referenceTime + 1000 - currentTime
        }
        if (millisUntilReset <= 0) {
            requestsThisSecond = 0
            referenceTime = currentTime
        }
        requestsThisSecond++
        //endregion
        return requestLambda(httpClient)
    }
}