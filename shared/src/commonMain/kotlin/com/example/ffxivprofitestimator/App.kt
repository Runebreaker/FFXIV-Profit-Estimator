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
import com.example.ffxivprofitestimator.Util.LRUCache

class App {
    private val rifleScope = CoroutineScope(SupervisorJob())
    private val platform: Platform = getPlatform()

    val testItemName = "rinascita"
    val rinascitaSwordID = 37742
    val recipeID = 35026

    fun greet(): String {
        rifleScope.launch {
            XIVAPI.getItem(rinascitaSwordID)?.let {
                it.recipeID
            } ?: println("Couldn't find item.")
        }
        return "Hello, ${platform.name}!"
    }
}

object XIVAPI : API() {
    override val baseURL = "https://xivapi.com"
    private val itemCache: LRUCache<Int, CachedItem> = LRUCache(20)

    override suspend fun request(id: Int, contentType: ContentType) {
        val filters: MutableMap<String, Any> = mutableMapOf()
        filters["columns"] = "ID,Name,GameContentLinks.Recipe.ItemResult"
        filters["snake_case"] = 1

        var requestURL = "$baseURL/${contentType.getName()}/$id"
        if (filters.isNotEmpty()) requestURL += "?" + filters.map { "${it.key}=${it.value}" }
            .joinToString("&")

        val requestedItem: Item = httpClient.get(requestURL).body()
        itemCache[id] = CachedItem(
            requestedItem.name,
            requestedItem.game_content_links.recipe.item_result[0]
        )
    }

    suspend fun getItem(id: Int): CachedItem?
    {
        if (!itemCache.contains(id)) request(id, ContentType.XIV(XIVContentType.ITEM))
        return itemCache[id]
    }

    data class CachedItem(val name: String, val recipeID: Int)

    @kotlinx.serialization.Serializable
    data class Item(val name: String, val id: Int, val game_content_links: GameContentLinks)

    @kotlinx.serialization.Serializable
    data class GameContentLinks(val recipe: Recipe)

    @kotlinx.serialization.Serializable
    data class Recipe(val item_result: List<Int>)
}

object UniversalisAPI : API() {
    override val baseURL: String = "https://universalis.app/api/v2"
    private val historyCache: LRUCache<Int, HistoryView> = LRUCache(20)
    private val dataCenters: MutableList<DataCenter> = mutableListOf()

    override suspend fun request(id: Int, contentType: ContentType) {
        when(contentType.getName())
        {
            UniversalisContentType.HISTORY.string -> {
                historyCache[id] = getHistoryView(id)
            }
            UniversalisContentType.DC.string -> {
                dataCenters.clear()
                dataCenters.addAll(getDatacenters())
            }
        }
    }

    private suspend fun getHistoryView(id: Int): HistoryView
    {
        return httpClient.get("$baseURL/${UniversalisContentType.HISTORY.string}/$id").body()
    }

    private suspend fun getDatacenters(): List<DataCenter> {
        return httpClient.get("$baseURL/${UniversalisContentType.DC.string}").body()
    }

    @kotlinx.serialization.Serializable
    data class DataCenter(
        val name: String?,
        val region: String?,
        val worlds: List<Int>?,
    )

    @kotlinx.serialization.Serializable
    data class MinimizedSaleView(
        // Whether or not the item was high-quality.
        val hq: Boolean,
        // The price per unit sold.
        val pricePerUnit: Int,
        // The stack size sold.
        val quantity: Int,
        // The buyer's character name. This may be null.
        val buyerName: String?,
        // Whether or not this was purchased from a mannequin. This may be null.
        val onMannequin: Boolean?,
        // The sale time, in seconds since the UNIX epoch.
        val timestamp: Long,
        // The world name, if applicable.
        val worldName: String?,
        // The world ID, if applicable.
        val worldID: Int?,
    )

    @kotlinx.serialization.Serializable
    data class HistoryView(
        // The item ID.
        val itemID: Int,
        // The world ID, if applicable.
        val worldID: Int?,
        // The last upload time for this endpoint, in milliseconds since the UNIX epoch.
        val lastUploadTime: Long,
        // The historical sales.
        val entries: MinimizedSaleView?,
        // The DC name, if applicable.
        val dcName: String?,
        // The region name, if applicable.
        val regionName: String?,
        // A map of quantities to sale counts, representing the number of sales of each quantity.
        val stackSizeHistogram: Map<Int, Int>?,
        // A map of quantities to NQ sale counts, representing the number of sales of each quantity.
        val stackSizeHistogramNQ: Map<Int, Int>?,
        // A map of quantities to HQ sale counts, representing the number of sales of each quantity.
        val stackSizeHistogramHQ: Map<Int, Int>?,
        // The average number of sales per day, over the past seven days (or the entirety of the shown sales, whichever comes first).
        val regularSaleVelocity: Int,
        // The average number of NQ sales per day, over the past seven days (or the entirety of the shown sales, whichever comes first).
        val nqSaleVelocity: Int,
        // The average number of HQ sales per day, over the past seven days (or the entirety of the shown sales, whichever comes first).
        val hqSaleVelocity: Int,
        // The world name, if applicable.
        val worldName: String?,
    )
}

abstract class API {
    abstract val baseURL: String
    val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                useAlternativeNames = false
            })
        }
    }

    abstract suspend fun request(
        id: Int, contentType: ContentType
    )

    enum class XIVContentType(val string: String) { ITEM("item"), RECIPE("recipe") }
    enum class UniversalisContentType(val string: String) { HISTORY("history"), DC("data-centers") }
    sealed class ContentType {
        data class XIV(val type: XIVContentType) : ContentType() {
            override fun getName() = type.string
        }

        data class Universalis(val type: UniversalisContentType) : ContentType() {
            override fun getName() = type.string
        }

        abstract fun getName(): String
    }
}