package com.raizlabs.dbflow5.transaction

import com.raizlabs.dbflow5.config.DBFlowDatabase
import com.raizlabs.dbflow5.config.FlowLog
import com.raizlabs.dbflow5.runtime.DBBatchSaveQueue

/**
 * Description: The base implementation of Transaction manager.
 */
abstract class BaseTransactionManager(val queue: ITransactionQueue,
                                      databaseDefinition: DBFlowDatabase) {

    private val saveQueue: DBBatchSaveQueue = DBBatchSaveQueue(databaseDefinition)

    init {
        checkQueue()
    }

    fun getSaveQueue(): DBBatchSaveQueue {
        try {
            if (!saveQueue.isAlive) {
                saveQueue.start()
            }
        } catch (i: IllegalThreadStateException) {
            FlowLog.logError(i) // if queue is alive, will throw error. might occur in multithreading.
        }

        return saveQueue
    }

    /**
     * Checks if queue is running. If not, should be started here.
     */
    fun checkQueue() = queue.startIfNotAlive()

    /**
     * Stops the queue this manager contains.
     */
    fun stopQueue() = queue.quit()

    /**
     * Adds a transaction to the [ITransactionQueue].
     *
     * @param transaction The transaction to add.
     */
    fun addTransaction(transaction: Transaction<out Any?>) = queue.add(transaction)

    /**
     * Cancels a transaction on the [ITransactionQueue].
     *
     * @param transaction
     */
    fun cancelTransaction(transaction: Transaction<out Any?>) = queue.cancel(transaction)
}
