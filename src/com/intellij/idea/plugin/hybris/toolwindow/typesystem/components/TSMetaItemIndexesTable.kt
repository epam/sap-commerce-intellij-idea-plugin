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

package com.intellij.idea.plugin.hybris.toolwindow.typesystem.components

import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaIndex
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaItem
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaItemService
import com.intellij.util.ui.ListTableModel

private const val COLUMN_NAME = "Name"
private const val COLUMN_REMOVE = "D"
private const val COLUMN_REPLACE = "R"
private const val COLUMN_UNIQUE = "U"
private const val COLUMN_CREATION_MODE = "Creation mode"
private const val COLUMN_KEYS = "Keys"

class TSMetaItemIndexesTable : AbstractTSTable<TSMetaItem, TSMetaIndex>() {

    override fun getSearchableColumnNames() = listOf(COLUMN_NAME, COLUMN_KEYS)
    override fun getFixedWidthColumnNames() = listOf(
        COLUMN_REMOVE,
        COLUMN_REPLACE,
        COLUMN_UNIQUE,
        COLUMN_CREATION_MODE
    )

    override fun createModel(): ListTableModel<TSMetaIndex> = with(ListTableModel<TSMetaIndex>()) {
        items = TSMetaItemService.getInstance(myProject).getIndexes(myOwner, true)
            .sortedBy { it.name }

        columnInfos = arrayOf(
            createColumn(
                name = COLUMN_NAME,
                valueProvider = { attr -> attr.name ?: "" },
                columnClass = String::class.java
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
                name = COLUMN_CREATION_MODE,
                valueProvider = { attr -> attr.creationMode }
            ),
            createColumn(
                name = COLUMN_KEYS,
                valueProvider = { attr -> attr.keys.joinToString() }
            )
        )

        this
    }

}