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