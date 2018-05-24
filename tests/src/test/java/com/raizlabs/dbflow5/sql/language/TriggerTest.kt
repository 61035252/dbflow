package com.raizlabs.dbflow5.sql.language

import com.raizlabs.dbflow5.BaseUnitTest
import com.raizlabs.dbflow5.assertEquals
import com.raizlabs.dbflow5.config.databaseForTable
import com.raizlabs.dbflow5.models.SimpleModel
import com.raizlabs.dbflow5.models.SimpleModel_Table.name
import com.raizlabs.dbflow5.models.TwoColumnModel
import com.raizlabs.dbflow5.models.TwoColumnModel_Table.id
import com.raizlabs.dbflow5.query.NameAlias
import com.raizlabs.dbflow5.query.cast
import com.raizlabs.dbflow5.query.columnValues
import com.raizlabs.dbflow5.query.createTempTrigger
import com.raizlabs.dbflow5.query.createTrigger
import com.raizlabs.dbflow5.query.insert
import com.raizlabs.dbflow5.query.insertOn
import com.raizlabs.dbflow5.query.property.property
import com.raizlabs.dbflow5.query.select
import com.raizlabs.dbflow5.query.updateOn
import com.raizlabs.dbflow5.sql.SQLiteType
import com.raizlabs.dbflow5.structure.insert
import org.junit.Assert.assertNotNull
import org.junit.Test

class TriggerTest : BaseUnitTest() {

    @Test
    fun validateBasicTrigger() {
        databaseForTable<SimpleModel> {
            ("CREATE TRIGGER IF NOT EXISTS `MyTrigger` AFTER INSERT ON `SimpleModel` " +
                "\nBEGIN" +
                "\nINSERT INTO `TwoColumnModel`(`name`) VALUES(`new`.`name`);" +
                "\nEND").assertEquals(createTrigger("MyTrigger").after() insertOn SimpleModel::class begin
                insert(TwoColumnModel::class).columnValues(name to NameAlias.ofTable("new", "name")))
        }
    }

    @Test
    fun validateUpdateTriggerMultiline() {
        databaseForTable<SimpleModel> {
            ("CREATE TEMP TRIGGER IF NOT EXISTS `MyTrigger` BEFORE UPDATE ON `SimpleModel` " +
                "\nBEGIN" +
                "\nINSERT INTO `TwoColumnModel`(`name`) VALUES(`new`.`name`);" +
                "\nINSERT INTO `TwoColumnModel`(`id`) VALUES(CAST(`new`.`name` AS INTEGER));" +
                "\nEND")
                .assertEquals(
                    (
                        createTempTrigger("MyTrigger").before() updateOn SimpleModel::class
                            begin
                            insert(TwoColumnModel::class, name).values(NameAlias.ofTable("new", "name"))
                            and
                            insert(TwoColumnModel::class, id).values(
                                cast(NameAlias.ofTable("new", "name").property).`as`(SQLiteType.INTEGER))))
        }
    }

    @Test
    fun validateTriggerWorks() {
        databaseForTable<SimpleModel> {
            val trigger = createTrigger("MyTrigger").after() insertOn SimpleModel::class begin
                insert(TwoColumnModel::class).columnValues(name to NameAlias.ofTable("new", "name"))
            trigger.enable(this)
            SimpleModel("Test").insert()

            val result = select from TwoColumnModel::class where (name eq "Test")
            assertNotNull(result)
        }
    }
}