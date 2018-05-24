package com.raizlabs.dbflow5.rx2

import com.raizlabs.dbflow5.BaseUnitTest
import com.raizlabs.dbflow5.TestDatabase
import com.raizlabs.dbflow5.config.database
import com.raizlabs.dbflow5.database.DatabaseWrapper
import com.raizlabs.dbflow5.models.SimpleModel
import com.raizlabs.dbflow5.query.list
import com.raizlabs.dbflow5.query.result
import com.raizlabs.dbflow5.query.select
import com.raizlabs.dbflow5.rx2.transaction.asMaybe
import com.raizlabs.dbflow5.rx2.transaction.asSingle
import com.raizlabs.dbflow5.structure.save
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Description:
 */
class TransactionObservablesTest : BaseUnitTest() {

    @Test
    fun testObservableRun() {
        var successCalled = false
        var list: List<SimpleModel>? = null
        database<TestDatabase>()
            .beginTransactionAsync { db: DatabaseWrapper ->
                (0 until 10).forEach {
                    SimpleModel("$it").save(db)
                }
            }
            .asSingle()
            .doAfterSuccess {
                database<TestDatabase>()
                    .beginTransactionAsync { (select from SimpleModel::class).list }
                    .asSingle()
                    .subscribe { loadedList: MutableList<SimpleModel> ->
                        list = loadedList
                        successCalled = true
                    }
            }.subscribe()

        assertTrue(successCalled)
        assertEquals(10, list!!.size)
    }

    @Test
    fun testMaybe() {
        var simpleModel: SimpleModel? = SimpleModel()
        database<TestDatabase>()
            .beginTransactionAsync { (select from SimpleModel::class).result }
            .asMaybe()
            .subscribe {
                simpleModel = it
            }
        assertNull(simpleModel)
    }
}