package com.raizlabs.dbflow5.database

/**
 * Description: Abstracts out the [DatabaseHelperDelegate] into the one used in this library.
 */
interface OpenHelper {

    val database: DatabaseWrapper

    val delegate: DatabaseHelperDelegate?

    val isDatabaseIntegrityOk: Boolean

    fun setWriteAheadLoggingEnabled(enabled: Boolean)

    fun performRestoreFromBackup()

    fun backupDB()

    fun setDatabaseListener(callback: DatabaseCallback?)

    fun closeDB()

    fun deleteDB()
}
