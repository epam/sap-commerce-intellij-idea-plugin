/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2025 EPAM Systems <hybrisideaplugin@epam.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.intellij.idea.plugin.hybris.toolwindow.system.type.components

import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaItemService
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaItem
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSMetaModifiers
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSMetaRelation
import com.intellij.idea.plugin.hybris.toolwindow.ui.AbstractTable
import com.intellij.openapi.project.Project
import com.intellij.util.ui.ListTableModel
import java.io.Serial

class TSMetaRelationElementsTable private constructor(myProject: Project) : AbstractTable<TSGlobalMetaItem, TSMetaRelation.TSMetaRelationElement>(myProject) {

    override fun getSearchableColumnNames() = listOf(COLUMN_QUALIFIER, COLUMN_DESCRIPTION)
    override fun getFixedWidthColumnNames() = listOf(COLUMN_CUSTOM, COLUMN_ORDERED, COLUMN_DEPRECATED)
    override fun select(item: TSMetaRelation.TSMetaRelationElement) = selectRowWithValue(item.name, COLUMN_QUALIFIER)
    override fun getItems(owner: TSGlobalMetaItem) = TSMetaItemService.getInstance(myProject).getRelationEnds(owner, true)
        .sortedWith(compareBy(
            { !it.isCustom },
            { it.moduleName },
            { it.name })
        )
        .toMutableList()

    override fun createModel(): ListTableModel<TSMetaRelation.TSMetaRelationElement> = with(ListTableModel<TSMetaRelation.TSMetaRelationElement>()) {
        columnInfos = arrayOf(
            createColumn(
                name = COLUMN_CUSTOM,
                valueProvider = { attr -> attr.isCustom },
                columnClass = Boolean::class.java,
                tooltip = "Custom"
            ),
            createColumn(
                name = COLUMN_ORDERED,
                valueProvider = { attr -> attr.isOrdered },
                columnClass = Boolean::class.java,
                tooltip = "Ordered"
            ),
            createColumn(
                name = COLUMN_DEPRECATED,
                valueProvider = { attr -> attr.isDeprecated },
                columnClass = Boolean::class.java,
                tooltip = "Deprecated"
            ),
            createColumn(
                name = COLUMN_MODIFIERS,
                valueProvider = { attr -> attr.modifiers.inlineDocumentation() },
                tooltip = TSMetaModifiers.tableHeaderTooltip
            ),
            createColumn(
                name = COLUMN_MODULE,
                valueProvider = { attr -> attr.extensionName }
            ),
            createColumn(
                name = COLUMN_OWNER,
                valueProvider = { attr -> attr.owner.name }
            ),
            createColumn(
                name = COLUMN_QUALIFIER,
                valueProvider = { attr -> attr.name }
            ),
            createColumn(
                name = COLUMN_CARDINALITY,
                valueProvider = { attr -> attr.cardinality }
            ),
            createColumn(
                name = COLUMN_TYPE,
                valueProvider = { attr -> attr.type }
            ),
            createColumn(
                name = COLUMN_COLLECTION_TYPE,
                valueProvider = { attr -> attr.collectionType }
            ),
            createColumn(
                name = COLUMN_DESCRIPTION,
                valueProvider = { attr -> attr.description }
            )
        )

        this
    }

    companion object {

        private const val COLUMN_CUSTOM = "C"
        private const val COLUMN_ORDERED = "O"
        private const val COLUMN_DEPRECATED = "D"
        private const val COLUMN_MODIFIERS = "[M]"
        private const val COLUMN_MODULE = "Module"
        private const val COLUMN_OWNER = "Owner"
        private const val COLUMN_QUALIFIER = "Qualifier"
        private const val COLUMN_TYPE = "Type"
        private const val COLUMN_COLLECTION_TYPE = "Collection Type"
        private const val COLUMN_CARDINALITY = "Cardinality"
        private const val COLUMN_DESCRIPTION = "Description"

        @Serial
        private val serialVersionUID: Long = -3549248270797403106L

        @JvmStatic
        fun getInstance(project: Project): TSMetaRelationElementsTable = with(TSMetaRelationElementsTable(project)) {
            init()

            this
        }
    }

}