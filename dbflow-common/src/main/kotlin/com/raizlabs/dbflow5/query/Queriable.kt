package com.raizlabs.dbflow5.query

import com.raizlabs.dbflow5.config.databaseForTable
import com.raizlabs.dbflow5.database.DatabaseStatement
import com.raizlabs.dbflow5.database.DatabaseWrapper
import com.raizlabs.dbflow5.database.FlowCursor
import com.raizlabs.dbflow5.sql.Query
import com.raizlabs.dbflow5.structure.ChangeAction
import com.raizlabs.dbflow5.structure.Model

/**
 * Description: The most basic interface that some of the classes such as [Insert], [ModelQueriable],
 * [Set], and more implement for convenience.
 */
interface Queriable : Query {

    val primaryAction: ChangeAction

    /**
     * @return A cursor from the DB based on this query
     */
    fun cursor(databaseWrapper: DatabaseWrapper): FlowCursor?

    /**
     * @return A new [DatabaseStatement] from this query.
     */
    fun compileStatement(databaseWrapper: DatabaseWrapper): DatabaseStatement

    /**
     * @return the long value of the results of query.
     */
    fun longValue(databaseWrapper: DatabaseWrapper): Long

    /**
     * @return This may return the number of rows affected from a [Set] or [Delete] statement.
     * If not, returns [Model.INVALID_ROW_ID]
     */
    fun executeUpdateDelete(databaseWrapper: DatabaseWrapper): Long

    /**
     * @return This may return the number of rows affected from a [Insert]  statement.
     * If not, returns [Model.INVALID_ROW_ID]
     */
    fun executeInsert(databaseWrapper: DatabaseWrapper): Long

    /**
     * @return True if this query has data. It will run a [.count] greater than 0.
     */
    fun hasData(databaseWrapper: DatabaseWrapper): Boolean

    /**
     * Will not return a result, rather simply will execute a SQL statement. Use this for non-SELECT statements or when
     * you're not interested in the result.
     */
    fun execute(databaseWrapper: DatabaseWrapper)

}

inline val <reified T : Any> BaseQueriable<T>.cursor
    get() = cursor(databaseForTable<T>())

inline val <reified T : Any> BaseQueriable<T>.hasData
    get() = hasData(databaseForTable<T>())

inline val <reified T : Any> BaseQueriable<T>.statement
    get() = compileStatement(databaseForTable<T>())
