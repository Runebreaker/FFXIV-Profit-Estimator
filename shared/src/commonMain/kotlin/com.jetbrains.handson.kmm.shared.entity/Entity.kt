package com.jetbrains.handson.kmm.shared.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class World(
    val id: Int,
    val name: String?,
)

@Serializable
data class DataCenter(
    val name: String?,
    val region: String?,
    val worlds: List<Int>?,
)

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