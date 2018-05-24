package com.raizlabs.dbflow5.processor.definition

import com.raizlabs.dbflow5.annotation.ConflictAction
import com.raizlabs.dbflow5.annotation.UniqueGroup
import com.raizlabs.dbflow5.processor.definition.column.ColumnDefinition
import com.raizlabs.dbflow5.processor.definition.column.ReferenceColumnDefinition
import com.raizlabs.dbflow5.quote
import com.squareup.javapoet.CodeBlock

/**
 * Description:
 */
class UniqueGroupsDefinition(uniqueGroup: UniqueGroup) {

    var columnDefinitionList: MutableList<ColumnDefinition> = arrayListOf()

    var number: Int = uniqueGroup.groupNumber

    private val uniqueConflict: ConflictAction = uniqueGroup.uniqueConflict

    fun addColumnDefinition(columnDefinition: ColumnDefinition) {
        columnDefinitionList.add(columnDefinition)
    }

    val creationName: CodeBlock
        get() {
            val codeBuilder = CodeBlock.builder().add(", UNIQUE(")
            columnDefinitionList.forEachIndexed { index, columnDefinition ->
                if (index > 0) {
                    codeBuilder.add(",")
                }
                if (columnDefinition is ReferenceColumnDefinition) {
                    for (reference in columnDefinition.referenceDefinitionList) {
                        codeBuilder.add(reference.columnName.quote())
                    }
                } else {
                    codeBuilder.add(columnDefinition.columnName.quote())
                }
            }
            codeBuilder.add(") ON CONFLICT \$L", uniqueConflict)
            return codeBuilder.build()
        }
}