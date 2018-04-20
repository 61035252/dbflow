package com.raizlabs.dbflow5.query.property

import com.raizlabs.dbflow5.config.FlowManager
import com.raizlabs.dbflow5.query.NameAlias
import kotlin.reflect.KClass

expect open class WrapperProperty<T, V> : InternalWrapperProperty<T, V> {
    constructor(table: KClass<*>, nameAlias: NameAlias)

    constructor(table: KClass<*>, columnName: String)
}

/**
 * Description: Provides convenience for types that are represented in different ways in the DB.
 *
 * @author Andrew Grosner (fuzz)
 */
abstract class InternalWrapperProperty<T, V> : Property<V> {

    private var databaseProperty: WrapperProperty<V, T>? = null

    constructor(table: KClass<*>, nameAlias: NameAlias) : super(table, nameAlias)

    constructor(table: KClass<*>, columnName: String) : super(table, columnName)

    override fun withTable(): WrapperProperty<T, V> {
        val nameAlias = this.nameAlias
            .newBuilder()
            .withTable(FlowManager.getTableName(requireTable()))
            .build()
        return WrapperProperty(this.requireTable(), nameAlias)
    }

    override fun withTable(tableNameAlias: NameAlias): WrapperProperty<T, V> {
        val nameAlias = this.nameAlias
            .newBuilder()
            .withTable(tableNameAlias.tableName)
            .build()
        return WrapperProperty(this.requireTable(), nameAlias)
    }

    /**
     * @return A new [Property] that corresponds to the inverted type of the [WrapperProperty]. Convenience
     * for types that have different DB representations.
     */
    fun invertProperty(): WrapperProperty<V, T> = databaseProperty
        ?: WrapperProperty<V, T>(requireTable(), nameAlias).also { databaseProperty = it }
}
