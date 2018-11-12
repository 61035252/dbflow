package com.dbflow5.processor.definition

import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.processor.definition.column.ColumnDefinition
import com.squareup.javapoet.TypeName

/**
 * Defines how a class is named, db it belongs to, and other loading behaviors.
 */
data class AssociationalBehavior(
        /**
         * @return The name of this view. Default is the class name.
         */
        val name: String,
        /**
         * @return The class of the database this corresponds to.
         */
        val databaseTypeName: TypeName,
        /**
         * @return When true, all public, package-private , non-static, and non-final fields of the reference class are considered as [com.dbflow5.annotation.Column] .
         * The only required annotated field becomes The [PrimaryKey]
         * or [PrimaryKey.autoincrement].
         */
        val allFields: Boolean)


/**
 * Defines how a Cursor gets loaded from the DB.
 */
data class CursorHandlingBehavior(
        val orderedCursorLookup: Boolean = false,
        val assignDefaultValuesFromCursor: Boolean = true)

/**
 * Defines how Primary Key columns behave. If has autoincrementing column or ROWID, the [associatedColumn] is not null.
 */
data class PrimaryKeyColumnBehavior(
        val hasRowID: Boolean,
        /**
         * Either [hasRowID] or [hasAutoIncrement] or null.
         */
        val associatedColumn: ColumnDefinition?,
        val hasAutoIncrement: Boolean)

/**
 * Describes caching behavior of a [TableDefinition].
 */
data class CachingBehavior(
        val cachingEnabled: Boolean,
        val customCacheSize: Int,
        var customCacheFieldName: String?,
        var customMultiCacheFieldName: String?)