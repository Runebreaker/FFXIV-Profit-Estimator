package com.jetbrains.handson.kmm.shared.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

//region Universalis API Data Classes
@Serializable
data class World(
    val id: Int,
    val name: String? = null,
) {
    override fun toString(): String = name ?: "No World"
}

@Serializable
data class DataCenter(
    val name: String? = null,
    val region: String? = null,
    val worlds: List<Int>? = null,
) {
    override fun toString(): String = name ?: "No Datacenter"
}

@Serializable
data class MinimizedSaleView(
    // Whether or not the item was high-quality.
    val hq: Boolean = false,
    // The price per unit sold.
    val pricePerUnit: Int = -1,
    // The stack size sold.
    val quantity: Int = -1,
    // The buyer's character name. This may be null.
    val buyerName: String? = null,
    // Whether or not this was purchased from a mannequin. This may be null.
    val onMannequin: Boolean? = null,
    // The sale time, in seconds since the UNIX epoch.
    val timestamp: Long = -1,
    // The world name, if applicable.
    val worldName: String? = null,
    // The world ID, if applicable.
    val worldID: Int? = null,
)

@Serializable
data class HistoryView(
    // The item ID.
    val itemID: Int = -1,
    // The world ID, if applicable.
    val worldID: Int? = null,
    // The last upload time for this endpoint, in milliseconds since the UNIX epoch.
    val lastUploadTime: Long = -1,
    // The historical sales.
    val entries: List<MinimizedSaleView>? = null,
    // The DC name, if applicable.
    val dcName: String? = null,
    // The region name, if applicable.
    val regionName: String? = null,
    // A map of quantities to sale counts, representing the number of sales of each quantity.
    val stackSizeHistogram: Map<Int, Int>? = null,
    // A map of quantities to NQ sale counts, representing the number of sales of each quantity.
    val stackSizeHistogramNQ: Map<Int, Int>? = null,
    // A map of quantities to HQ sale counts, representing the number of sales of each quantity.
    val stackSizeHistogramHQ: Map<Int, Int>? = null,
    // The average number of sales per day, over the past seven days (or the entirety of the shown sales, whichever comes first).
    val regularSaleVelocity: Double = -1.0,
    // The average number of NQ sales per day, over the past seven days (or the entirety of the shown sales, whichever comes first).
    val nqSaleVelocity: Double = -1.0,
    // The average number of HQ sales per day, over the past seven days (or the entirety of the shown sales, whichever comes first).
    val hqSaleVelocity: Double = -1.0,
    // The world name, if applicable.
    val worldName: String? = null,
) {
    val valid = itemID >= 0
}
//endregion

// region XIV API Data Classes
@Serializable
data class Item(
    val name: String,
    val id: Int,
    val icon: String,
    @SerialName("game_content_links")
    val links: GameContentLinks,
) {
    val recipeIngredients = links.recipe.recipeIds
}

@Serializable
data class GameContentLinks(
    val recipe: Recipe,
)

@Serializable
data class Recipe(
    @SerialName("item_result")
    val recipeIds: List<Int>,
)
//endregion