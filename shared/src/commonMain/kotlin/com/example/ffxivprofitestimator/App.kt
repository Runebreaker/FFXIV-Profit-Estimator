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
            XIVAPI.getItem(rinascitaSwordID)?.icon ?: println("Couldn't find item.")
        }
        return "Hello, ${platform.name}!"
    }

    /**
     * Updates the database by retrieving new information from Universalis.
     * @return True, if DB was updated. False, if some error occurred retrieving the info.
     */
    suspend fun updateDB(force: Boolean = false): Boolean {
        if (database.getDatacenters().isNotEmpty() && database.getWorlds()
                .isNotEmpty() && !force
        ) return false
        database.clearDB()
        val datacenters = UniversalisAPI.getDatacenters() ?: return false
        val worlds = UniversalisAPI.getWorlds() ?: return false
        database.fillDB(datacenters, worlds)
        return true
    }

    fun getWorldsOfDatacenter(dcName: String): List<World> = database.getWorldsOfDatacenter(dcName)

    fun getDatacenters(): List<DataCenter> = database.getDatacenters()

    fun getWorlds(): List<World> = database.getWorlds()
}

object XIVAPI : API() {
    override val baseURL = "https://xivapi.com"
    override val rateLimit: Int = 20
    private val itemCache: LRUCache<Int, Item> = LRUCache(20)
    private val iconCache: LRUCache<Int, ByteArray?> = LRUCache(20)

    private val filters: Map<String, Any> = mapOf(
        "columns" to "ID,Icon,Name,GameContentLinks.Recipe.ItemResult", "snake_case" to 1
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

            itemCache[id] = request { httpClient ->
                httpClient.get(requestURL).body()
            } ?: return null
            iconCache[id] = itemCache[id]?.icon?.let { getIconAsByteArray(it) }
        }
        return itemCache[id]
    }

    /**
     * Retrieves the cached icon of the item ID.
     * @return The Icon as a ByteArray or null
     * @param itemId The ID of the item
     */
    fun getCachedIcon(itemId: Int): ByteArray? = iconCache[itemId]

    /**
     * Retrieves an image at a given URL as a ByteArray. Should not be used, since it creates
     * an API call. Use getCachedIcon(itemId) instead, if the item is already in cache. If it
     * is NOT in cache yet, request it via getItem(itemId) first.
     * @return The byte array, if successful.
     * @param iconUrl The location of the image based on the baseUrl (e.g. /i/20005)
     */
    suspend fun getIconAsByteArray(iconUrl: String): ByteArray? = request { httpClient ->
        println("$baseURL$iconUrl")
        httpClient.get("$baseURL$iconUrl").body()
    }
}

object UniversalisAPI : API() {
    override val baseURL: String = "https://universalis.app/api/v2"
    override val rateLimit: Int = 25
    private val historyCache: LRUCache<Pair<Int, Any>, HistoryView> = LRUCache(20)

    /**
     * Gets the item by ID from world(ID: Int), datacenter(name: String) or region(name: String).
     * @return The item as HistoryView
     * @param itemId The ID of the item
     * @param worldDcRegion The World, Datacenter or Region to retrieve the data from
     */
    suspend fun getItem(itemId: Int, worldDcRegion: Any): HistoryView? {
        val keyPair = Pair(itemId, worldDcRegion)
        if (historyCache.moveToFront(keyPair)) return historyCache.first()
        getHistoryView(keyPair)?.let { historyView ->
            historyCache[keyPair] = historyView
        } ?: return null
        return historyCache.first()
    }

    /**
     * Retrieves the items from the history cache.
     * @return The entries as a LinkedHashMap
     */
    fun getCachedItems(): LinkedHashMap<Pair<Int, Any>, HistoryView> = historyCache.getEntries()

    /**
     * Requests the HistoryView of the Item from the specified world, datacenter or region.
     * @return The HistoryView of the specified item from the specified world
     * @param keyPair The pair of itemId (Int) and world(Int)/datacenter(String)/region(String)
     */
    suspend fun getHistoryView(keyPair: Pair<Int, Any>): HistoryView? = request { httpClient ->
        httpClient.get("$baseURL/history/${keyPair.second}/${keyPair.first}").body()
    }

    /**
     * Retrieves a list of available Datacenters.
     * @return A list of datacenters or null
     */
    suspend fun getDatacenters(): List<DataCenter>? = request { httpClient ->
        httpClient.get("$baseURL/data-centers").body()
    }

    /**
     * Retrieves a list of available Worlds.
     * @return A list of worlds or null
     */
    suspend fun getWorlds(): List<World>? = request { httpClient ->
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
        private set

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                useAlternativeNames = false
            })
        }
    }

    /**
     * Using this request, you can make your http requests via a preconfigured HttpClient,
     * which is automatically rate-limited according to the specified value in requests/sec.
     * When the number of requests in a second is greater or equal to the specified value
     * (should only be smaller or equal), the coroutine waits for the remaining time
     * in the current second (plus a small buffer for safety).
     */
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