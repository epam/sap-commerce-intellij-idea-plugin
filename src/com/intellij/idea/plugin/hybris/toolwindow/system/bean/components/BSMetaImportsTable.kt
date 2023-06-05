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

package com.intellij.idea.plugin.hybris.toolwindow.system.bean.components

import com.intellij.idea.plugin.hybris.kotlin.yExtensionName
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSGlobalMetaBean
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSMetaImport
import com.intellij.idea.plugin.hybris.toolwindow.components.AbstractTable
import com.intellij.openapi.project.Project
import com.intellij.util.ui.ListTableModel

private const val COLUMN_CUSTOM = "C"
private const val COLUMN_STATIC = "S"
private const val COLUMN_MODULE = "Module"
private const val COLUMN_TYPE = "Type"

class BSMetaImportsTable private constructor(myProject: Project) :
    AbstractTable<BSGlobalMetaBean, BSMetaImport>(myProject) {

    override fun getSearchableColumnNames() = listOf(COLUMN_TYPE)
    override fun getFixedWidthColumnNames() = listOf(COLUMN_CUSTOM, COLUMN_STATIC)
    override fun select(item: BSMetaImport) = selectRowWithValue(item.type, COLUMN_TYPE)
    override fun getItems(owner: BSGlobalMetaBean) = owner.imports.sortedWith(
        compareBy(
            { !it.isCustom },
            { it.module.name },
            { it.type })
    )
        .toMutableList()

    override fun createModel(): ListTableModel<BSMetaImport> = with(ListTableModel<BSMetaImport>()) {
        columnInfos = arrayOf(
            createColumn(
                name = COLUMN_CUSTOM,
                valueProvider = { attr -> attr.isCustom },
                columnClass = Boolean::class.java,
                tooltip = "Custom"
            ),
            createColumn(
                name = COLUMN_STATIC,
                valueProvider = { attr -> attr.isStatic },
                columnClass = Boolean::class.java,
                tooltip = "Static"
            ),
            createColumn(
                name = COLUMN_MODULE,
                valueProvider = { attr -> attr.module.yExtensionName() }
            ),
            createColumn(
                name = COLUMN_TYPE,
                valueProvider = { attr -> attr.type }
            )
        )

        this
    }

    companion object {
        private const val serialVersionUID: Long = 6752572571238631111L

        fun getInstance(project: Project): BSMetaImportsTable = with(BSMetaImportsTable(project)) {
            init()

            this
        }
    }

}