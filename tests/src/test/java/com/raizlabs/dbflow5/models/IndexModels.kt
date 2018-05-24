package com.raizlabs.dbflow5.models

import com.raizlabs.dbflow5.TestDatabase
import com.raizlabs.dbflow5.annotation.Column
import com.raizlabs.dbflow5.annotation.Index
import com.raizlabs.dbflow5.annotation.IndexGroup
import com.raizlabs.dbflow5.annotation.PrimaryKey
import com.raizlabs.dbflow5.annotation.Table
import java.util.*

/**
 * Description:
 */

@Table(database = TestDatabase::class, indexGroups = arrayOf(IndexGroup(number = 1, name = "firstIndex"),
        IndexGroup(number = 2, name = "secondIndex"),
        IndexGroup(number = 3, name = "thirdIndex")))
class IndexModel {
    @Index(indexGroups = intArrayOf(1, 2, 3))
    @PrimaryKey
    var id: Int = 0

    @Index(indexGroups = intArrayOf(1))
    @Column
    var first_name: String? = null

    @Index(indexGroups = intArrayOf(2))
    @Column
    var last_name: String? = null

    @Index(indexGroups = intArrayOf(3))
    @Column
    var created_date: Date? = null

    @Index(indexGroups = intArrayOf(2, 3))
    @Column
    var isPro: Boolean = false
}