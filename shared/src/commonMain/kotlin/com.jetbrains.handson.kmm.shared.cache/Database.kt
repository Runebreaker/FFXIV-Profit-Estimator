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

    internal fun getWorlds(): List<World> {
        return dbQuery.getWorlds(mapper = ::mapWorld).executeAsList()
    }

    internal fun getDatacenters(): List<DataCenter> {
        return dbQuery.getDatacenters(mapper = ::mapDatacenter).executeAsList()
    }

    internal fun getWorldsOfDatacenter(dataCenterName: String): List<World>
    {
        return dbQuery.getWorldsOfDatacenter(name = dataCenterName, mapper = ::mapWorld).executeAsList()
    }

    private fun mapDatacenter(name: String, region: String?): DataCenter =
        DataCenter(name, region, getWorldsOfDatacenter(name).map { it.id })

    private fun mapWorld(id: Int, name: String?, datacenter: String?): World =
        World(id, name)
}