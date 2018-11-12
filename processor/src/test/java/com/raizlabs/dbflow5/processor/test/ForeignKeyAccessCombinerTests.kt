package com.dbflow5.processor.test

import com.dbflow5.processor.definition.column.Combiner
import com.dbflow5.processor.definition.column.ContentValuesCombiner
import com.dbflow5.processor.definition.column.ForeignKeyAccessCombiner
import com.dbflow5.processor.definition.column.ForeignKeyAccessField
import com.dbflow5.processor.definition.column.ForeignKeyLoadFromCursorCombiner
import com.dbflow5.processor.definition.column.PackagePrivateScopeColumnAccessor
import com.dbflow5.processor.definition.column.PartialLoadFromCursorAccessCombiner
import com.dbflow5.processor.definition.column.PrimaryReferenceAccessCombiner
import com.dbflow5.processor.definition.column.PrivateScopeColumnAccessor
import com.dbflow5.processor.definition.column.SqliteStatementAccessCombiner
import com.dbflow5.processor.definition.column.TypeConverterScopeColumnAccessor
import com.dbflow5.processor.definition.column.VisibleScopeColumnAccessor
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.NameAllocator
import com.squareup.javapoet.TypeName
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * Description:
 *
 * @author Andrew Grosner (fuzz)
 */


class ForeignKeyAccessCombinerTest {

    @Test
    fun test_canCombineSimpleCase() {
        val foreignKeyAccessCombiner = ForeignKeyAccessCombiner(VisibleScopeColumnAccessor("name"))
        foreignKeyAccessCombiner.fieldAccesses += ForeignKeyAccessField("test",
                ContentValuesCombiner(Combiner(VisibleScopeColumnAccessor("test"), TypeName.get(String::class.java))))
        foreignKeyAccessCombiner.fieldAccesses += ForeignKeyAccessField("test2",
                ContentValuesCombiner(Combiner(PrivateScopeColumnAccessor("test2"), TypeName.get(Int::class.java))))

        val builder = CodeBlock.builder()
        foreignKeyAccessCombiner.addCode(builder, AtomicInteger(4))

        assertEquals("if (model.name != null) {" +
                "\n  values.put(\"`test`\", model.name.test);" +
                "\n  values.put(\"`test2`\", model.name.getTest2());" +
                "\n} else {" +
                "\n  values.putNull(\"`test`\");" +
                "\n  values.putNull(\"`test2`\");" +
                "\n}",
                builder.build().toString().trim())
    }

    @Test
    fun test_canCombineSimplePrivateCase() {
        val foreignKeyAccessCombiner = ForeignKeyAccessCombiner(PrivateScopeColumnAccessor("name"))
        foreignKeyAccessCombiner.fieldAccesses += ForeignKeyAccessField("",
                SqliteStatementAccessCombiner(Combiner(VisibleScopeColumnAccessor("test"), TypeName.get(String::class.java))))

        val builder = CodeBlock.builder()
        foreignKeyAccessCombiner.addCode(builder, AtomicInteger(4))

        assertEquals("if (model.getName() != null) {" +
                "\n  statement.bindStringOrNull(4, model.getName().test);" +
                "\n} else {" +
                "\n  statement.bindNull(4);" +
                "\n}",
                builder.build().toString().trim())
    }

    @Test
    fun test_canCombinePackagePrivateCase() {
        val foreignKeyAccessCombiner = ForeignKeyAccessCombiner(PackagePrivateScopeColumnAccessor("name",
            "com.fuzz.android", "TestHelper"))
        foreignKeyAccessCombiner.fieldAccesses += ForeignKeyAccessField("test",
                PrimaryReferenceAccessCombiner(Combiner(PackagePrivateScopeColumnAccessor("test",
                    "com.fuzz.android", "TestHelper2"),
                        TypeName.get(String::class.java))))

        val builder = CodeBlock.builder()
        foreignKeyAccessCombiner.addCode(builder, AtomicInteger(4))

        assertEquals("if (com.fuzz.android.TestHelper_Helper.getName(model) != null) {" +
                "\n  clause.and(test.eq(com.fuzz.android.TestHelper2_Helper.getTest(com.fuzz.android.TestHelper_Helper.getName(model))));" +
                "\n} else {" +
                "\n  clause.and(test.eq((com.dbflow5.sql.language.IConditional) null));" +
                "\n}",
                builder.build().toString().trim())
    }

    @Test
    fun test_canDoComplexCase() {
        val foreignKeyAccessCombiner = ForeignKeyAccessCombiner(VisibleScopeColumnAccessor("modem"))
        foreignKeyAccessCombiner.fieldAccesses += ForeignKeyAccessField("number",
                ContentValuesCombiner(Combiner(PackagePrivateScopeColumnAccessor("number",
                    "com.fuzz", "AnotherHelper"),
                        TypeName.INT)))
        foreignKeyAccessCombiner.fieldAccesses += ForeignKeyAccessField("date",
                ContentValuesCombiner(Combiner(TypeConverterScopeColumnAccessor("global_converter", "date"),
                        TypeName.get(Date::class.java))))

        val builder = CodeBlock.builder()
        foreignKeyAccessCombiner.addCode(builder, AtomicInteger(1))

        assertEquals("if (model.modem != null) {" +
                "\n  values.put(\"`number`\", com.fuzz.AnotherHelper\$Helper.getNumber(model.modem));" +
                "\n  values.put(\"`date`\", global_converter.getDBValue(model.modem.date));" +
                "\n} else {" +
                "\n  values.putNull(\"`number`\");" +
                "\n  values.putNull(\"`date`\");" +
                "\n}",
                builder.build().toString().trim())
    }

    @Test
    fun test_canLoadFromCursor() {
        val foreignKeyAccessCombiner = ForeignKeyLoadFromCursorCombiner(VisibleScopeColumnAccessor("testModel1"),
                ClassName.get("com.dbflow5.test.container", "ParentModel"),
                ClassName.get("com.dbflow5.test.container", "ParentModel_Table"), false,
                NameAllocator())
        foreignKeyAccessCombiner.fieldAccesses += PartialLoadFromCursorAccessCombiner("testmodel_id",
                "name", TypeName.get(String::class.java), false, null)
        foreignKeyAccessCombiner.fieldAccesses += PartialLoadFromCursorAccessCombiner("testmodel_type",
                "type", TypeName.get(String::class.java), false, null)

        val builder = CodeBlock.builder()
        foreignKeyAccessCombiner.addCode(builder, AtomicInteger(0))

        assertEquals("int index_testmodel_id_ParentModel_Table = cursor.getColumnIndex(\"testmodel_id\");" +
                "\nint index_testmodel_type_ParentModel_Table = cursor.getColumnIndex(\"testmodel_type\");" +
                "\nif (index_testmodel_id_ParentModel_Table != -1 && !cursor.isNull(index_testmodel_id_ParentModel_Table) && index_testmodel_type_ParentModel_Table != -1 && !cursor.isNull(index_testmodel_type_ParentModel_Table)) {" +
                "\n  model.testModel1 = com.dbflow5.sql.language.SQLite.select().from(com.dbflow5.test.container.ParentModel.class).where()" +
                "\n      .and(com.dbflow5.test.container.ParentModel_Table.name.eq(cursor.getString(index_testmodel_id_ParentModel_Table)))" +
                "\n      .and(com.dbflow5.test.container.ParentModel_Table.type.eq(cursor.getString(index_testmodel_type_ParentModel_Table)))" +
                "\n      .querySingle();" +
                "\n} else {" +
                "\n  model.testModel1 = null;" +
                "\n}", builder.build().toString().trim())
    }

    @Test
    fun test_canLoadFromCursorStubbed() {
        val foreignKeyAccessCombiner = ForeignKeyLoadFromCursorCombiner(VisibleScopeColumnAccessor("testModel1"),
                ClassName.get("com.dbflow5.test.container", "ParentModel"),
                ClassName.get("com.dbflow5.test.container", "ParentModel_Table"), true,
                NameAllocator())
        foreignKeyAccessCombiner.fieldAccesses += PartialLoadFromCursorAccessCombiner("testmodel_id",
                "name", TypeName.get(String::class.java), false, VisibleScopeColumnAccessor("name"))
        foreignKeyAccessCombiner.fieldAccesses += PartialLoadFromCursorAccessCombiner("testmodel_type",
                "type", TypeName.get(String::class.java), false, VisibleScopeColumnAccessor("type"))

        val builder = CodeBlock.builder()
        foreignKeyAccessCombiner.addCode(builder, AtomicInteger(0))

        assertEquals("int index_testmodel_id_ParentModel_Table = cursor.getColumnIndex(\"testmodel_id\");" +
                "\nint index_testmodel_type_ParentModel_Table = cursor.getColumnIndex(\"testmodel_type\");" +
                "\nif (index_testmodel_id_ParentModel_Table != -1 && !cursor.isNull(index_testmodel_id_ParentModel_Table) && index_testmodel_type_ParentModel_Table != -1 && !cursor.isNull(index_testmodel_type_ParentModel_Table)) {" +
                "\n  model.testModel1 = new com.dbflow5.test.container.ParentModel();" +
                "\n  model.testModel1.name = cursor.getString(index_testmodel_id_ParentModel_Table);" +
                "\n  model.testModel1.type = cursor.getString(index_testmodel_type_ParentModel_Table);" +
                "\n} else {" +
                "\n  model.testModel1 = null;" +
                "\n}", builder.build().toString().trim())
    }
}