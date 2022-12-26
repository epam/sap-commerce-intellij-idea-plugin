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
import com.intellij.idea.plugin.hybris.toolwindow.components.AbstractTable
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSMetaCustomProperty
import com.intellij.openapi.project.Project
import com.intellij.util.ui.ListTableModel

private const val COLUMN_CUSTOM = "C"
private const val COLUMN_NAME = "Name"
private const val COLUMN_VALUE = "Value"
private const val COLUMN_MODULE = "Module"

abstract class AbstractTSMetaCustomPropertiesTable<T : Any>(myProject: Project) : AbstractTable<T, TSMetaCustomProperty>(myProject) {

    override fun getSearchableColumnNames() = listOf(COLUMN_NAME, COLUMN_VALUE)
    override fun getFixedWidthColumnNames() = listOf(COLUMN_CUSTOM)
    override fun select(meta: TSMetaCustomProperty) = selectRowWithValue(meta.name, COLUMN_NAME)

    override fun createModel(): ListTableModel<TSMetaCustomProperty> = with(ListTableModel<TSMetaCustomProperty>()) {
        columnInfos = arrayOf(
            createColumn(
                name = COLUMN_CUSTOM,
                valueProvider = { attr -> attr.isCustom },
                columnClass = Boolean::class.java,
                tooltip = "Custom"
            ),
            createColumn(
                name = COLUMN_MODULE,
                valueProvider = { attr -> PsiUtils.getModuleName(attr.module) }
            ),
            createColumn(
                name = COLUMN_NAME,
                valueProvider = { attr -> attr.name },
                columnClass = String::class.java,
            ),
            createColumn(
                name = COLUMN_VALUE,
                valueProvider = { attr -> attr.rawValue ?: "" },
                tooltip = "Value"
            )
        )

        this
    }

    companion object {
        private const val serialVersionUID: Long = -6204398733396273020L
    }

}