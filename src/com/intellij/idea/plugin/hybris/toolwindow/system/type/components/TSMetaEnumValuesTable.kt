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

import com.intellij.idea.plugin.hybris.system.type.meta.model.TSGlobalMetaEnum
import com.intellij.idea.plugin.hybris.system.type.meta.model.TSMetaEnum
import com.intellij.idea.plugin.hybris.toolwindow.ui.AbstractTable
import com.intellij.openapi.project.Project
import com.intellij.util.ui.ListTableModel
import java.io.Serial

class TSMetaEnumValuesTable private constructor(myProject: Project) : AbstractTable<TSGlobalMetaEnum, TSMetaEnum.TSMetaEnumValue>(myProject) {

    override fun getSearchableColumnNames() = listOf(COLUMN_VALUE, COLUMN_DESCRIPTION)
    override fun getFixedWidthColumnNames() = listOf(COLUMN_CUSTOM)
    override fun select(item: TSMetaEnum.TSMetaEnumValue) = selectRowWithValue(item.name, COLUMN_VALUE)
    override fun getItems(owner: TSGlobalMetaEnum) = owner.values.values.sortedWith(compareBy(
        { !it.isCustom },
        { it.moduleName },
        { it.name })
    )
        .toMutableList()

    override fun createModel(): ListTableModel<TSMetaEnum.TSMetaEnumValue> = with(ListTableModel<TSMetaEnum.TSMetaEnumValue>()) {
        columnInfos = arrayOf(
            createColumn(
                name = COLUMN_CUSTOM,
                valueProvider = { attr -> attr.isCustom },
                columnClass = Boolean::class.java,
                tooltip = "Custom"
            ),
            createColumn(
                name = COLUMN_MODULE,
                valueProvider = { attr -> attr.extensionName }
            ),
            createColumn(
                name = COLUMN_VALUE,
                valueProvider = { attr -> attr.name }
            ),
            createColumn(
                name = COLUMN_DESCRIPTION,
                valueProvider = { attr -> attr.description ?: "" }
            )
        )

        this
    }

    companion object {
        @Serial
        private val serialVersionUID: Long = 6652572661218637911L

        private const val COLUMN_CUSTOM = "C"
        private const val COLUMN_MODULE = "Module"
        private const val COLUMN_VALUE = "Value"
        private const val COLUMN_DESCRIPTION = "Description"

        @JvmStatic
        fun getInstance(project: Project): TSMetaEnumValuesTable = with(TSMetaEnumValuesTable(project)) {
            init()

            this
        }
    }

}