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

package com.intellij.idea.plugin.hybris.toolwindow.beans.components

import com.intellij.idea.plugin.hybris.beans.meta.model.BeansGlobalMetaBean
import com.intellij.idea.plugin.hybris.beans.meta.model.BeansMetaHint
import com.intellij.idea.plugin.hybris.psi.utils.PsiUtils
import com.intellij.idea.plugin.hybris.toolwindow.components.AbstractTable
import com.intellij.openapi.project.Project
import com.intellij.util.ui.ListTableModel

private const val COLUMN_CUSTOM = "C"
private const val COLUMN_MODULE = "Module"
private const val COLUMN_NAME = "Name"
private const val COLUMN_VALUE = "Value"

class BeansMetaHintsTable private constructor(myProject: Project) :
    AbstractTable<BeansGlobalMetaBean, BeansMetaHint>(myProject) {

    override fun getSearchableColumnNames() = listOf(COLUMN_NAME)
    override fun getFixedWidthColumnNames() = listOf(COLUMN_CUSTOM)
    override fun select(meta: BeansMetaHint) = selectRowWithValue(meta.name, COLUMN_NAME)
    override fun getItems(meta: BeansGlobalMetaBean) = meta.hints.values.sortedWith(
        compareBy(
            { !it.isCustom },
            { it.module.name },
            { it.name })
    )

    override fun createModel(): ListTableModel<BeansMetaHint> = with(ListTableModel<BeansMetaHint>()) {
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
                valueProvider = { attr -> attr.name }
            ),
            createColumn(
                name = COLUMN_VALUE,
                valueProvider = { attr -> attr.value }
            )
        )

        this
    }

    companion object {
        private const val serialVersionUID: Long = 6752572571238637911L

        fun getInstance(project: Project): BeansMetaHintsTable = with(BeansMetaHintsTable(project)) {
            init()

            this
        }
    }

}