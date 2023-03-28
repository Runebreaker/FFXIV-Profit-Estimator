package com.jetbrains.handson.kmm.shared.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

//region Universalis API Data Classes
@Serializable
data class World(
    val id: Int,
    val name: String?,
) {
    override fun toString(): String = name ?: "Unnamed World"
}

@Serializable
data class DataCenter(
    val name: String?,
    val region: String?,
    val worlds: List<Int>?,
) {
    override fun toString(): String = name ?: "Unnamed Datacenter"
}

@Serializable
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

@Serializable
data class HistoryView(
    // The item ID.
    val itemID: Int = -1,
    // The world ID, if applicable.
    val worldID: Int? = null,
    // The last upload time for this endpoint, in milliseconds since the UNIX epoch.
    val lastUploadTime: Long = -1,
    // The historical sales.
    val entries: MinimizedSaleView? = null,
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
    val regularSaleVelocity: Int = -1,
    // The average number of NQ sales per day, over the past seven days (or the entirety of the shown sales, whichever comes first).
    val nqSaleVelocity: Int = -1,
    // The average number of HQ sales per day, over the past seven days (or the entirety of the shown sales, whichever comes first).
    val hqSaleVelocity: Int = -1,
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
    val recipeIngredients = links.recipe.ingredients
}

@Serializable
data class GameContentLinks(
    val recipe: Recipe,
)

@Serializable
data class Recipe(
    @SerialName("item_result")
    val ingredients: List<Int>,
)
//endregion