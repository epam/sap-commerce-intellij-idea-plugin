/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.settings.ui

import com.intellij.idea.plugin.hybris.settings.TypeSystemDiagramSettings
import com.intellij.idea.plugin.hybris.toolwindow.ui.AbstractTable
import com.intellij.openapi.project.Project
import com.intellij.util.ui.ListTableModel
import java.io.Serial

private const val COLUMN_NAME = "Name"

class TSDiagramSettingsExcludedTypeNameTable private constructor(project: Project) : AbstractTable<TypeSystemDiagramSettings, TSTypeNameHolder>(project) {

    override fun getSearchableColumnNames() = listOf(COLUMN_NAME)
    override fun select(item: TSTypeNameHolder) {

    }

    override fun getItems(owner: TypeSystemDiagramSettings) = owner.excludedTypeNames
        .map { TSTypeNameHolder(it) }
        .sortedBy { it.typeName }
        .toMutableList()

    override fun createModel() = with(MyListTableModel()) {
        columnInfos = arrayOf(
            createColumn(
                name = COLUMN_NAME,
                valueProvider = { it.typeName },
                isCellEditable = true,
                valueSetter = { originalValue, newValue ->
                    if (newValue == null || newValue == "") {
                        getCastedModel()
                            ?.takeIf { it.indexOf(originalValue) != -1 }
                            ?.let { it.removeRow(it.indexOf(originalValue)) }
                    } else if (newValue is String) {
                        originalValue.typeName = newValue
                    }
                }
            ),
        )

        this
    }

    companion object {
        @Serial
        private val serialVersionUID: Long = -3601797303539315993L

        fun getInstance(project: Project): TSDiagramSettingsExcludedTypeNameTable = with(TSDiagramSettingsExcludedTypeNameTable(project)) {
            init()

            this
        }
    }
}

class MyListTableModel : ListTableModel<TSTypeNameHolder>() {
    override fun addRow() {
        addRow(TSTypeNameHolder(""))
    }

    companion object {
        @Serial
        private val serialVersionUID: Long = 2743342735794887335L
    }
}

data class TSTypeNameHolder(
    var typeName: String,
    var valid: Boolean = false,
    var custom: Boolean = false
)