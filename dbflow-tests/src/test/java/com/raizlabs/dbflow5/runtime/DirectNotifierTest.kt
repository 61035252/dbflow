package com.raizlabs.dbflow5.runtime

import android.content.Context
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.raizlabs.dbflow5.ImmediateTransactionManager2
import com.raizlabs.dbflow5.TestDatabase
import com.raizlabs.dbflow5.config.DatabaseConfig
import com.raizlabs.dbflow5.config.FlowConfig
import com.raizlabs.dbflow5.config.FlowManager
import com.raizlabs.dbflow5.config.databaseForTable
import com.raizlabs.dbflow5.database.AndroidSQLiteOpenHelper
import com.raizlabs.dbflow5.models.SimpleModel
import com.raizlabs.dbflow5.query.delete
import com.raizlabs.dbflow5.query.insert
import com.raizlabs.dbflow5.query.set
import com.raizlabs.dbflow5.query.update
import com.raizlabs.dbflow5.structure.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class DirectNotifierTest {

    val context: Context
        get() = RuntimeEnvironment.application

    @Before
    fun setupTest() {
        FlowManager.init(FlowConfig.Builder(context)
                .database(DatabaseConfig.Builder(TestDatabase::class,
                        AndroidSQLiteOpenHelper.createHelperCreator(context))
                        .transactionManagerCreator(::ImmediateTransactionManager2)
                        .build()).build())
    }

    @Test
    fun validateCanNotifyDirect() {
        val simpleModel = SimpleModel("Name")

        val modelChange = mock<DirectModelNotifier.OnModelStateChangedListener<SimpleModel>>()
        DirectModelNotifier.get().registerForModelStateChanges(SimpleModel::class, modelChange)

        simpleModel.insert()
        verify(modelChange).onModelChanged(simpleModel, ChangeAction.INSERT)

        simpleModel.update()
        verify(modelChange).onModelChanged(simpleModel, ChangeAction.UPDATE)

        simpleModel.save()
        verify(modelChange).onModelChanged(simpleModel, ChangeAction.CHANGE)

        simpleModel.delete()
        verify(modelChange).onModelChanged(simpleModel, ChangeAction.DELETE)
    }

    @Test
    fun validateCanNotifyWrapperClasses() {
        databaseForTable<SimpleModel> {
            val modelChange = Mockito.mock(OnTableChangedListener::class.java)
            DirectModelNotifier.get().registerForTableChanges(SimpleModel::class, modelChange)

            insert<SimpleModel>().columnValues(SimpleModel_Table.name to "name").executeInsert(this)

            verify(modelChange).onTableChanged(SimpleModel::class, ChangeAction.INSERT)

            (update<SimpleModel>() set SimpleModel_Table.name.eq("name2")).executeUpdateDelete(this)

            verify(modelChange).onTableChanged(SimpleModel::class, ChangeAction.UPDATE)

            delete<SimpleModel>().executeUpdateDelete(this)

            verify(modelChange).onTableChanged(SimpleModel::class, ChangeAction.DELETE)
        }
    }

    @After
    fun teardown() {
        FlowManager.destroy()
    }
}
