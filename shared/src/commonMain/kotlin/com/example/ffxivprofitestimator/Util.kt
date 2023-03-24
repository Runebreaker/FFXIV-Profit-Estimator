package com.example.ffxivprofitestimator

object Util {
    class LRUCache<K, V>(private val size: Int) {
        private val map: LinkedHashMap<K, V> = LinkedHashMap(size*10/7 + 2, 0.7f)

        operator fun get(key: K): V? = map[key]

        operator fun set(key: K, value: V)
        {
            map[key] = value
            if (map.entries.size > size) map.remove(map.entries.last().key)
        }

        fun size() = map.size

        fun clear() = map.clear()

        fun contains(key: K) = map.containsKey(key)
    }
}