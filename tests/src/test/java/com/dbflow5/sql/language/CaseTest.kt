package com.dbflow5.sql.language

import com.dbflow5.BaseUnitTest
import com.dbflow5.models.SimpleModel_Table
import com.dbflow5.query.case
import com.dbflow5.query.caseWhen
import com.dbflow5.query.property.propertyString
import org.junit.Assert.*
import org.junit.Test

class CaseTest : BaseUnitTest() {

    @Test
    fun simpleCaseTest() {
        val case = case(propertyString<String>("country"))
                .whenever("USA")
                .then("Domestic")
                .`else`("Foreign")
        assertEquals("CASE country WHEN 'USA' THEN 'Domestic' ELSE 'Foreign' END `Country`",
                case.end("Country").query.trim())
        assertTrue(case.isEfficientCase)
    }

    @Test
    fun searchedCaseTest() {
        val case = caseWhen<String>(SimpleModel_Table.name.eq("USA")).then("Domestic")
                .whenever(SimpleModel_Table.name.eq("CA")).then("Canada")
                .`else`("Foreign")
        assertEquals("CASE WHEN `name`='USA' THEN 'Domestic' WHEN `name`='CA' THEN 'Canada' ELSE 'Foreign'",
                case.query.trim())
        assertFalse(case.isEfficientCase)
    }
}