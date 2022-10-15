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

import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaAttribute
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaItem
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaItemService
import com.intellij.util.ui.ListTableModel

private const val COLUMN_DEPRECATED = "D"
private const val COLUMN_REDECLARE = "R"
private const val COLUMN_AUTO_CREATE = "A"
private const val COLUMN_GENERATE = "G"
private const val COLUMN_TYPE = "Type"
private const val COLUMN_DEFAULT_VALUE = "Default value"
private const val COLUMN_DESCRIPTION = "Description"
private const val COLUMN_QUALIFIER = "Qualifier"

class TSMetaItemAttributesTable : AbstractTSTable<TSMetaItem, TSMetaAttribute>() {

    override fun getSearchableColumnNames() = listOf(COLUMN_QUALIFIER, COLUMN_DESCRIPTION)
    override fun getFixedWidthColumnNames() = listOf(COLUMN_DEPRECATED, COLUMN_REDECLARE, COLUMN_AUTO_CREATE, COLUMN_GENERATE)

    override fun createModel(): ListTableModel<TSMetaAttribute> = with(ListTableModel<TSMetaAttribute>()) {
        items = TSMetaItemService.getInstance(myProject).getAttributes(myOwner, true)
            .sortedBy { it.name }

        columnInfos = arrayOf(
            createColumn(
                name = COLUMN_DEPRECATED,
                valueProvider = { attr -> attr.isDeprecated },
                columnClass = Boolean::class.java,
                tooltip = "Deprecated"
            ),
            createColumn(
                name = COLUMN_REDECLARE,
                valueProvider = { attr -> attr.isRedeclare },
                columnClass = Boolean::class.java,
                tooltip = "Redeclare"
            ),
            createColumn(
                name = COLUMN_AUTO_CREATE,
                valueProvider = { attr -> attr.isAutoCreate },
                columnClass = Boolean::class.java,
                tooltip = "Autocreate"
            ),
            createColumn(
                name = COLUMN_GENERATE,
                valueProvider = { attr -> attr.isGenerate },
                columnClass = Boolean::class.java,
                tooltip = "Generate"
            ),
            createColumn(
                name = COLUMN_QUALIFIER,
                valueProvider = { attr -> attr.name ?: "" }
            ),
            createColumn(
                name = COLUMN_TYPE,
                valueProvider = { attr -> attr.type ?: "" }
            ),
            createColumn(
                name = COLUMN_DESCRIPTION,
                valueProvider = { attr -> attr.description ?: "" }
            ),
            createColumn(
                name = COLUMN_DEFAULT_VALUE,
                valueProvider = { attr -> attr.defaultValue ?: "" }
            )
        )

        this
    }

    companion object {
        private const val serialVersionUID: Long = 6652572661218637911L
    }

}