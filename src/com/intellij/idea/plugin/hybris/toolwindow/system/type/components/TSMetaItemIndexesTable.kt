/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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

import com.intellij.idea.plugin.hybris.psi.utils.PsiUtils
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaItem
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSMetaItem.TSMetaItemIndex
import com.intellij.idea.plugin.hybris.toolwindow.components.AbstractTable
import com.intellij.openapi.project.Project
import com.intellij.util.ui.ListTableModel

private const val COLUMN_NAME = "Name"
private const val COLUMN_CUSTOM = "C"
private const val COLUMN_REMOVE = "D"
private const val COLUMN_REPLACE = "R"
private const val COLUMN_UNIQUE = "U"
private const val COLUMN_CREATION_MODE = "Creation mode"
private const val COLUMN_KEYS = "Keys"
private const val COLUMN_INCLUDES = "Includes"
private const val COLUMN_MODULE = "Module"

class TSMetaItemIndexesTable private constructor(myProject: Project) : AbstractTable<TSGlobalMetaItem, TSMetaItemIndex>(myProject) {

    override fun getSearchableColumnNames() = listOf(COLUMN_NAME, COLUMN_KEYS, COLUMN_INCLUDES)
    override fun select(item: TSMetaItemIndex) = selectRowWithValue(item.name, COLUMN_NAME)
    override fun getFixedWidthColumnNames() = listOf(
        COLUMN_CUSTOM,
        COLUMN_REMOVE,
        COLUMN_REPLACE,
        COLUMN_UNIQUE,
        COLUMN_CREATION_MODE
    )

    override fun getItems(owner: TSGlobalMetaItem): MutableList<TSMetaItemIndex> = owner.allIndexes
        .sortedWith(compareBy(
            { !it.isCustom },
            { it.module.name },
            { it.name })
        )
        .toMutableList()

    override fun createModel(): ListTableModel<TSMetaItemIndex> = with(ListTableModel<TSMetaItemIndex>()) {
        columnInfos = arrayOf(
            createColumn(
                name = COLUMN_CUSTOM,
                valueProvider = { attr -> attr.isCustom },
                columnClass = Boolean::class.java,
                tooltip = "Custom"
            ),
            createColumn(
                name = COLUMN_REMOVE,
                valueProvider = { attr -> attr.isRemove },
                columnClass = Boolean::class.java,
                tooltip = "Remove"
            ),
            createColumn(
                name = COLUMN_REPLACE,
                valueProvider = { attr -> attr.isReplace },
                columnClass = Boolean::class.java,
                tooltip = "Replace"
            ),
            createColumn(
                name = COLUMN_UNIQUE,
                valueProvider = { attr -> attr.isUnique },
                columnClass = Boolean::class.java,
                tooltip = "Unique"
            ),
            createColumn(
                name = COLUMN_MODULE,
                valueProvider = { attr -> PsiUtils.getModuleName(attr.module) }
            ),
            createColumn(
                name = COLUMN_NAME,
                valueProvider = { attr -> attr.name },
                columnClass = String::class.java
            ),
            createColumn(
                name = COLUMN_CREATION_MODE,
                valueProvider = { attr -> attr.creationMode }
            ),
            createColumn(
                name = COLUMN_KEYS,
                valueProvider = { attr -> attr.keys.joinToString() }
            ),
            createColumn(
                name = COLUMN_INCLUDES,
                valueProvider = { attr -> attr.includes.joinToString() }
            )
        )

        this
    }

    companion object {
        private const val serialVersionUID: Long = -6854917148686972681L

        fun getInstance(project: Project): TSMetaItemIndexesTable = with(TSMetaItemIndexesTable(project)) {
            init()

            this
        }
    }

}