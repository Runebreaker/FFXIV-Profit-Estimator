package com.jetbrains.handson.kmm.shared.cache

import com.jetbrains.handson.kmm.shared.entity.DataCenter
import com.jetbrains.handson.kmm.shared.entity.World

internal class Database(databaseDriverFactory: DatabaseDriverFactory) {
    private val database = AppDatabase(databaseDriverFactory.createDriver())
    private val dbQuery = database.appDatabaseQueries

    internal fun clearDB()
    {
        dbQuery.transaction {
            dbQuery.deleteDatacenters()
            dbQuery.deleteWorlds()
        }
    }

    internal fun fillDB(dcList: List<DataCenter>, worldList: List<World>)
    {
        dbQuery.transaction {
            val worldMap = worldList.associateBy { it.id }
            dcList.forEach { dc ->
                if (dc.name == null) return@forEach
                dbQuery.insertDatacenter(dc.name, dc.region)
                dc.worlds?.forEach { worldId ->
                    worldMap[worldId]?.let { world ->
                        dbQuery.insertWorld(world.id, world.name, dc.name)
                    }
                }
            }
        }
    }

    internal fun getWorldsFromDatacenter(dataCenter: DataCenter): List<World>?
    {
        if (dataCenter.name == null) return null
        return dbQuery.getWorldsOfDatacenter(name = dataCenter.name, mapper = ::mapWorld).executeAsList()
    }

    private fun mapWorld(id: Int, name: String?, datacenter: String?): World
    {
        return World(id, name)
    }
}