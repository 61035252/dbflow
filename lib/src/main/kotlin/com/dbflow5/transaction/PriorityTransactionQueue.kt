package com.dbflow5.transaction

import android.os.Looper
import android.os.Process.THREAD_PRIORITY_BACKGROUND
import android.os.Process.setThreadPriority
import com.dbflow5.config.FlowLog
import java.util.concurrent.PriorityBlockingQueue

/**
 * Description: Orders [Transaction] in a priority-based queue, enabling you perform higher priority
 * tasks first (such as retrievals) if you are attempting many DB operations at once.
 */
class PriorityTransactionQueue
/**
 * Creates a queue with the specified name to ID it.
 *
 * @param name
 */
(name: String) : Thread(name), ITransactionQueue {

    private val queue = PriorityBlockingQueue<PriorityEntry<Transaction<out Any?>>>()

    private var isQuitting = false

    override fun run() {
        Looper.prepare()
        setThreadPriority(THREAD_PRIORITY_BACKGROUND)
        var transaction: PriorityEntry<Transaction<out Any?>>
        while (true) {
            try {
                transaction = queue.take()
            } catch (e: InterruptedException) {
                if (isQuitting) {
                    synchronized(queue) {
                        queue.clear()
                    }
                    return
                }
                continue
            }

            transaction.entry.executeSync()
        }
    }

    override fun add(transaction: Transaction<out Any?>) {
        synchronized(queue) {
            val priorityEntry = PriorityEntry(transaction)
            if (!queue.contains(priorityEntry)) {
                queue.add(priorityEntry)
            }
        }
    }

    /**
     * Cancels the specified request.
     *
     * @param transaction The transaction to cancel (if still in the queue).
     */
    override fun cancel(transaction: Transaction<out Any?>) {
        synchronized(queue) {
            val priorityEntry = PriorityEntry(transaction)
            if (queue.contains(priorityEntry)) {
                queue.remove(priorityEntry)
            }
        }
    }

    /**
     * Cancels all requests by a specific tag
     *
     * @param name
     */
    override fun cancel(name: String) {
        synchronized(queue) {
            val it = queue.iterator()
            while (it.hasNext()) {
                val next = it.next().entry
                if (next.name != null && next.name == name) {
                    it.remove()
                }
            }
        }
    }

    override fun startIfNotAlive() {
        synchronized(this) {
            if (!isAlive) {
                try {
                    start()
                } catch (i: IllegalThreadStateException) {
                    // log if failure from thread is still alive.
                    FlowLog.log(FlowLog.Level.E, throwable = i)
                }

            }
        }
    }

    /**
     * Quits this process
     */
    override fun quit() {
        isQuitting = true
        interrupt()
    }

    internal data class PriorityEntry<out E : Transaction<out Any?>>(val entry: E)
        : Comparable<PriorityEntry<Transaction<out Any?>>> {
        private val transactionWrapper: PriorityTransactionWrapper =
                when {
                    entry.transaction is PriorityTransactionWrapper -> entry.transaction
                    else -> entry.transaction.withPriority()
                }

        override fun compareTo(other: PriorityEntry<Transaction<out Any?>>): Int =
                transactionWrapper.compareTo(other.transactionWrapper)

    }
}
