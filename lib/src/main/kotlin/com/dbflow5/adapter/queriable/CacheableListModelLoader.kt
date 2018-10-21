package com.dbflow5.adapter.queriable

import com.dbflow5.adapter.CacheAdapter
import com.dbflow5.adapter.ModelAdapter
import com.dbflow5.annotation.Table
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.database.FlowCursor
import com.dbflow5.query.cache.ModelCache
import com.dbflow5.query.cache.addOrReload

/**
 * Description: Loads a [List] of [T] with [Table.cachingEnabled] true.
 */
open class CacheableListModelLoader<T : Any>(modelClass: Class<T>,
                                             protected val cacheAdapter: CacheAdapter<T>)
    : ListModelLoader<T>(modelClass) {

    protected val modelAdapter: ModelAdapter<T> by lazy {
        if (instanceAdapter !is ModelAdapter<*>) {
            throw IllegalArgumentException("A non-Table type was used.")
        }
        val modelAdapter = instanceAdapter as ModelAdapter<T>
        if (!modelAdapter.cachingEnabled()) {
            throw IllegalArgumentException("You cannot call this method for a table that has" +
                    " no caching id. Either use one Primary Key or use the MultiCacheKeyConverter")
        }
        return@lazy modelAdapter
    }

    protected val modelCache: ModelCache<T, *> = cacheAdapter.modelCache

    override fun convertToData(cursor: FlowCursor, databaseWrapper: DatabaseWrapper): MutableList<T> {
        val data = mutableListOf<T>()
        val cacheValues = arrayOfNulls<Any>(cacheAdapter.cachingColumnSize)
        // Ensure that we aren't iterating over this cursor concurrently from different threads
        if (cursor.moveToFirst()) {
            do {
                val values = cacheAdapter.getCachingColumnValuesFromCursor(cacheValues, cursor)
                val model = modelCache.addOrReload(cacheAdapter.getCachingId(values),
                        cacheAdapter, modelAdapter, cursor, databaseWrapper)
                data.add(model)
            } while (cursor.moveToNext())
        }
        return data
    }

}
