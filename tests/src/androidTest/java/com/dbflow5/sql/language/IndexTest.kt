package com.dbflow5.sql.language

import com.dbflow5.BaseUnitTest
import com.dbflow5.config.databaseForTable
import com.dbflow5.models.SimpleModel
import com.dbflow5.models.SimpleModel_Table
import com.dbflow5.query.indexOn
import com.dbflow5.query.nameAlias
import org.junit.Assert.assertEquals
import org.junit.Test

class IndexTest : BaseUnitTest() {

    @Test
    fun validateBasicIndex() {
        databaseForTable<SimpleModel> { dbFlowDatabase ->
            assertEquals("CREATE INDEX IF NOT EXISTS `index` ON `SimpleModel`(`name`)",
                    indexOn<SimpleModel>("index", SimpleModel_Table.name).query)
        }
    }

    @Test
    fun validateUniqueIndex() {
        databaseForTable<SimpleModel> { dbFlowDatabase ->
            assertEquals("CREATE UNIQUE INDEX IF NOT EXISTS `index` ON `SimpleModel`(`name`, `test`)",
                    indexOn<SimpleModel>("index").unique(true).and(SimpleModel_Table.name)
                            .and("test".nameAlias).query)
        }
    }

    @Test
    fun validateBasicIndexNameAlias() {
        databaseForTable<SimpleModel> { dbFlowDatabase ->
            assertEquals("CREATE INDEX IF NOT EXISTS `index` ON `SimpleModel`(`name`, `test`)",
                    indexOn<SimpleModel>("index", "name".nameAlias, "test".nameAlias).query)
        }
    }
}