package com.raizlabs.dbflow5.database

/**
 * Description: Provides callbacks for [OpenHelper] methods
 */
interface DatabaseCallback {

    /**
     * Called when the DB is opened
     *
     * @param database The database that is opened
     */
    fun onOpen(database: DatabaseWrapper)

    /**
     * Called when the DB is created
     *
     * @param database The database that is created
     */
    fun onCreate(database: DatabaseWrapper)

    /**
     * Called when the DB is upgraded.
     *
     * @param database   The database that is upgraded
     * @param oldVersion The previous DB version
     * @param newVersion The new DB version
     */
    fun onUpgrade(database: DatabaseWrapper, oldVersion: Int, newVersion: Int)

    /**
     * Called when DB is downgraded. Note that this may not be supported by all implementations of the DB.
     *
     * @param databaseWrapper The database downgraded.
     * @param oldVersion      The old. higher version.
     * @param newVersion      The new lower version.
     */
    fun onDowngrade(databaseWrapper: DatabaseWrapper, oldVersion: Int, newVersion: Int)
}
