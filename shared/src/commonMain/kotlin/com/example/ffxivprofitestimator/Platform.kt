package com.example.ffxivprofitestimator

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform