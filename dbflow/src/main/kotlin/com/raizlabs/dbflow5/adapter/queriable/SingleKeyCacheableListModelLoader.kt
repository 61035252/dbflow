package com.raizlabs.dbflow5.adapter.queriable

import com.raizlabs.dbflow5.adapter.CacheAdapter
import com.raizlabs.dbflow5.database.DatabaseWrapper
import com.raizlabs.dbflow5.database.FlowCursor
import com.raizlabs.dbflow5.query.cache.addOrReload

/**
 * Description:
 */
class SingleKeyCacheableListModelLoader<T : Any>(tModelClass: Class<T>,
                                                 cacheAdapter: CacheAdapter<T>)
    : CacheableListModelLoader<T>(tModelClass, cacheAdapter) {

    override fun convertToData(cursor: FlowCursor, databaseWrapper: DatabaseWrapper): MutableList<T> {
        val data = arrayListOf<T>()
        var cacheValue: Any?
        // Ensure that we aren't iterating over this cursor concurrently from different threads
        if (cursor.moveToFirst()) {
            do {
                cacheValue = cacheAdapter.getCachingColumnValueFromCursor(cursor)
                val model = modelCache.addOrReload(cacheValue, cacheAdapter, modelAdapter, cursor, databaseWrapper)
                data.add(model)
            } while (cursor.moveToNext())
        }
        return data
    }

}
