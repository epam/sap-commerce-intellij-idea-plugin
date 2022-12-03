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

import com.intellij.idea.plugin.hybris.beans.meta.model.BeansGlobalMetaEnum
import com.intellij.idea.plugin.hybris.beans.meta.model.BeansMetaEnum
import com.intellij.idea.plugin.hybris.psi.utils.PsiUtils
import com.intellij.idea.plugin.hybris.toolwindow.components.AbstractTSTable
import com.intellij.openapi.project.Project
import com.intellij.util.ui.ListTableModel

private const val COLUMN_CUSTOM = "C"
private const val COLUMN_MODULE = "Module"
private const val COLUMN_VALUE = "Value"

class BeansMetaEnumValuesTable private constructor(myProject: Project) :
    AbstractTSTable<BeansGlobalMetaEnum, BeansMetaEnum.BeansMetaEnumValue>(myProject) {

    override fun getSearchableColumnNames() = listOf(COLUMN_VALUE)
    override fun getFixedWidthColumnNames() = listOf(COLUMN_CUSTOM)
    override fun select(meta: BeansMetaEnum.BeansMetaEnumValue) = selectRowWithValue(meta.name, COLUMN_VALUE)
    override fun getItems(meta: BeansGlobalMetaEnum) = meta.values.values.sortedWith(
        compareBy(
            { !it.isCustom },
            { it.module.name },
            { it.name })
    )

    override fun createModel(): ListTableModel<BeansMetaEnum.BeansMetaEnumValue> = with(ListTableModel<BeansMetaEnum.BeansMetaEnumValue>()) {
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
                name = COLUMN_VALUE,
                valueProvider = { attr -> attr.name }
            )
        )

        this
    }

    companion object {
        private const val serialVersionUID: Long = 6612572661238637911L

        fun getInstance(project: Project): BeansMetaEnumValuesTable = with(BeansMetaEnumValuesTable(project)) {
            init()

            this
        }
    }

}