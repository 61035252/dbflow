package com.raizlabs.dbflow5.adapter.saveable

import com.raizlabs.dbflow5.adapter.ModelAdapter
import com.raizlabs.dbflow5.database.DatabaseStatement
import com.raizlabs.dbflow5.database.DatabaseWrapper
import com.raizlabs.dbflow5.runtime.NotifyDistributor
import com.raizlabs.dbflow5.structure.ChangeAction

/**
 * Description: Defines how models get saved into the DB. It will bind values to [DatabaseStatement]
 * for all CRUD operations as they are wildly faster and more efficient than ContentValues.
 */
open class ModelSaver<T : Any> {

    lateinit var modelAdapter: ModelAdapter<T>

    @Synchronized
    fun save(model: T, wrapper: DatabaseWrapper): Boolean {
        var exists = modelAdapter.exists(model, wrapper)

        if (exists) {
            exists = update(model, wrapper)
        }

        if (!exists) {
            exists = insert(model, wrapper) > INSERT_FAILED
        }

        // return successful store into db.
        return exists
    }

    @Synchronized
    fun save(model: T,
             insertStatement: DatabaseStatement,
             updateStatement: DatabaseStatement,
             wrapper: DatabaseWrapper): Boolean {
        var exists = modelAdapter.exists(model, wrapper)

        if (exists) {
            exists = update(model, updateStatement, wrapper)
        }

        if (!exists) {
            exists = insert(model, insertStatement, wrapper) > INSERT_FAILED
        }

        // return successful store into db.
        return exists
    }

    @Synchronized
    fun update(model: T, wrapper: DatabaseWrapper): Boolean {
        val updateStatement = modelAdapter.getUpdateStatement(wrapper)
        return updateStatement.use { update(model, it, wrapper) }
    }

    @Synchronized
    fun update(model: T, databaseStatement: DatabaseStatement, wrapper: DatabaseWrapper): Boolean {
        modelAdapter.saveForeignKeys(model, wrapper)
        modelAdapter.bindToUpdateStatement(databaseStatement, model)
        val successful = databaseStatement.executeUpdateDelete() != 0L
        if (successful) {
            NotifyDistributor().notifyModelChanged(model, modelAdapter, ChangeAction.UPDATE)
        }
        return successful
    }

    @Synchronized
    open fun insert(model: T, wrapper: DatabaseWrapper): Long {
        val insertStatement = modelAdapter.getInsertStatement(wrapper)
        return insertStatement.use { insert(model, it, wrapper) }
    }

    @Synchronized
    open fun insert(model: T,
                    insertStatement: DatabaseStatement,
                    wrapper: DatabaseWrapper): Long {
        modelAdapter.saveForeignKeys(model, wrapper)
        modelAdapter.bindToInsertStatement(insertStatement, model)
        val id = insertStatement.executeInsert()
        if (id > INSERT_FAILED) {
            modelAdapter.updateAutoIncrement(model, id)
            NotifyDistributor().notifyModelChanged(model, modelAdapter, ChangeAction.INSERT)
        }
        return id
    }

    @Synchronized
    fun delete(model: T, wrapper: DatabaseWrapper): Boolean {
        val deleteStatement = modelAdapter.getDeleteStatement(wrapper)
        return deleteStatement.use { delete(model, it, wrapper) }
    }

    @Synchronized
    fun delete(model: T,
               deleteStatement: DatabaseStatement,
               wrapper: DatabaseWrapper): Boolean {
        modelAdapter.deleteForeignKeys(model, wrapper)
        modelAdapter.bindToDeleteStatement(deleteStatement, model)

        val success = deleteStatement.executeUpdateDelete() != 0L
        if (success) {
            NotifyDistributor().notifyModelChanged(model, modelAdapter, ChangeAction.DELETE)
        }
        modelAdapter.updateAutoIncrement(model, 0)
        return success
    }

    companion object {

        const val INSERT_FAILED = -1
    }
}

