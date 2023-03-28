package com.example.ffxivprofitestimator

object Util {
    class LRUCache<K, V>(private val size: Int) {
        private val map: LinkedHashMap<K, V> = LinkedHashMap(size*10/7 + 2, 0.7f)

        operator fun get(key: K): V? = map[key]

        operator fun set(key: K, value: V)
        {
            map.remove(key)
            map[key] = value
            if (map.entries.size > size) map.remove(map.entries.first().key)
        }

        fun moveToFront(key: K): Boolean {
            val tempVal = map[key] ?: return false
            map.remove(key)
            map[key] = tempVal
            return true
        }

        fun first(): V? {
            if (map.entries.isEmpty()) return null
            return map.entries.last().value
        }

        fun last(): V? {
            if (map.entries.isEmpty()) return null
            return map.entries.first().value
        }

        fun getEntries(): LinkedHashMap<K, V> = map

        fun getOrDefault(key: K, default: V): V = map[key] ?: default

        fun size() = map.size

        fun clear() = map.clear()

        fun containsKey(key: K) = map.containsKey(key)

        fun containsValue(value: V) = map.containsValue(value)
    }
}