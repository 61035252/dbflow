package com.raizlabs.dbflow5.rx.query

import com.raizlabs.dbflow5.config.FlowLog
import com.raizlabs.dbflow5.database.DatabaseWrapper
import com.raizlabs.dbflow5.query.list.FlowCursorList
import rx.Observable
import rx.Producer
import rx.Subscriber
import rx.functions.Action1
import rx.internal.operators.BackpressureUtils
import java.util.concurrent.atomic.AtomicLong

/**
 * Description: Wraps a [RXModelQueriable] into a [Observable.OnSubscribe]
 * for each element represented by the query.
 */
class CursorResultSubscriber<T : Any>(private val modelQueriable: RXModelQueriable<T>,
                                      private val databaseWrapper: DatabaseWrapper) : Observable.OnSubscribe<T> {

    override fun call(subscriber: Subscriber<in T>) {
        subscriber.setProducer(ElementProducer(subscriber))
    }

    private inner class ElementProducer internal constructor(private val subscriber: Subscriber<in T>) : Producer {
        private val emitted: AtomicLong = AtomicLong()
        private val requested: AtomicLong = AtomicLong()

        override fun request(n: Long) {
            if (n == Long.MAX_VALUE
                && requested.compareAndSet(0, Long.MAX_VALUE)
                || n > 0 && BackpressureUtils.getAndAddRequest(requested, n) == 0L) {
                // emitting all elements
                modelQueriable.cursorList(databaseWrapper).subscribe(CursorListAction(n))
            }
        }

        private inner class CursorListAction
        internal constructor(private val limit: Long) : Action1<FlowCursorList<T>> {

            override fun call(ts: FlowCursorList<T>) {
                val starting: Long = when {
                    limit == Long.MAX_VALUE
                        && requested.compareAndSet(0, Long.MAX_VALUE) -> 0
                    else -> emitted.toLong()
                }
                var limit = this.limit + starting

                while (limit > 0) {
                    val iterator = ts.iterator(starting, limit)
                    try {
                        var i: Long = 0
                        while (!subscriber.isUnsubscribed && iterator.hasNext() && i++ < limit) {
                            subscriber.onNext(iterator.next())
                        }
                        emitted.addAndGet(i)
                        // no more items
                        if (!subscriber.isUnsubscribed && i < limit) {
                            subscriber.onCompleted()
                            break
                        }
                        limit = requested.addAndGet(-limit)
                    } catch (e: Exception) {
                        FlowLog.logError(e)
                        subscriber.onError(e)
                    } finally {
                        try {
                            iterator.close()
                        } catch (e: Exception) {
                            FlowLog.logError(e)
                            subscriber.onError(e)
                        }

                    }
                }
            }
        }
    }
}
