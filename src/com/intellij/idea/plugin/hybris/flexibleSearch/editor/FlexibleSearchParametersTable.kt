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

package com.intellij.idea.plugin.hybris.flexibleSearch.editor

import com.intellij.idea.plugin.hybris.toolwindow.ui.AbstractTable
import com.intellij.openapi.project.Project
import com.intellij.util.ui.ListTableModel
import java.io.Serial

class FlexibleSearchParametersTable(myProject: Project) :
    AbstractTable<Map<String, String>, FlexibleSearchParametersTable.Parameter>(myProject) {

    data class Parameter(val property: String, val value: String)

    override fun getSearchableColumnNames() = listOf(COLUMN_PROPERTY, COLUMN_VALUE)
    override fun getFixedWidthColumnNames() = emptyList<String>()
    override fun select(item: Parameter) = selectRowWithValue(item.property, COLUMN_PROPERTY)
    override fun getItems(owner: Map<String, String>) = owner.entries
        .map { Parameter(it.key, it.value) }
        .sortedBy { it.property }
        .toMutableList()

    override fun createModel(): ListTableModel<Parameter> = with(ListTableModel<Parameter>()) {
        columnInfos = arrayOf(
            createColumn(
                name = COLUMN_PROPERTY,
                valueProvider = { param -> param.property }
            ),
            createColumn(
                name = COLUMN_VALUE,
                valueProvider = { param -> param.value }
            )
        )

        this
    }

    companion object {
        @Serial
        private val serialVersionUID: Long = 8531724994057102568L

        private const val COLUMN_PROPERTY = "Property"
        private const val COLUMN_VALUE = "Value"

        @JvmStatic
        fun getInstance(project: Project): FlexibleSearchParametersTable = with(FlexibleSearchParametersTable(project)) {
            init()

            this
        }
    }
}