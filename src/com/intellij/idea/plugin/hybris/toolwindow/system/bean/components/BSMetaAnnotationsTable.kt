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

import com.intellij.idea.plugin.hybris.psi.util.PsiUtils
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSGlobalMetaBean
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSMetaAnnotations
import com.intellij.idea.plugin.hybris.toolwindow.components.AbstractTable
import com.intellij.openapi.project.Project
import com.intellij.util.ui.ListTableModel

private const val COLUMN_CUSTOM = "C"
private const val COLUMN_SCOPE = "Scope"
private const val COLUMN_MODULE = "Module"
private const val COLUMN_VALUE = "Value"

class BSMetaAnnotationsTable private constructor(myProject: Project) :
    AbstractTable<BSGlobalMetaBean, BSMetaAnnotations>(myProject) {

    override fun getSearchableColumnNames() = listOf(COLUMN_VALUE)
    override fun getFixedWidthColumnNames() = listOf(COLUMN_CUSTOM, COLUMN_SCOPE)
    override fun select(item: BSMetaAnnotations) = selectRowWithValue(item.value, COLUMN_VALUE)
    override fun getItems(owner: BSGlobalMetaBean) = owner.annotations.sortedWith(
        compareBy(
            { !it.isCustom },
            { it.module.name },
            { it.value })
    )
        .toMutableList()

    override fun createModel(): ListTableModel<BSMetaAnnotations> = with(ListTableModel<BSMetaAnnotations>()) {
        columnInfos = arrayOf(
            createColumn(
                name = COLUMN_CUSTOM,
                valueProvider = { attr -> attr.isCustom },
                columnClass = Boolean::class.java,
                tooltip = "Custom"
            ),
            createColumn(
                name = COLUMN_SCOPE,
                valueProvider = { attr -> attr.scope },
            ),
            createColumn(
                name = COLUMN_MODULE,
                valueProvider = { attr -> PsiUtils.getModuleName(attr.module) }
            ),
            createColumn(
                name = COLUMN_VALUE,
                valueProvider = { attr -> attr.value }
            )
        )

        this
    }

    companion object {
        private const val serialVersionUID: Long = 6752572571238631345L

        fun getInstance(project: Project): BSMetaAnnotationsTable = with(BSMetaAnnotationsTable(project)) {
            init()

            this
        }
    }

}