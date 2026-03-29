/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.impex.ui.components

import com.intellij.openapi.project.Project
import com.intellij.ui.AddEditDeleteListPanel
import com.intellij.ui.ListSpeedSearch
import com.intellij.util.asSafely
import com.intellij.util.ui.JBEmptyBorder
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.impex.ui.ImpExWrapStringExclusion
import sap.commerce.toolset.typeSystem.meta.TSMetaModelAccess
import java.awt.Component
import java.io.Serial
import javax.swing.DefaultListCellRenderer
import javax.swing.JComponent
import javax.swing.JList
import javax.swing.ListCellRenderer

class ImpExWrapStringExclusionsListPanel(
    private val project: Project,
    exclusions: List<ImpExWrapStringExclusion>
) : AddEditDeleteListPanel<ImpExWrapStringExclusion>("", exclusions) {

    private var myListCellRenderer: ListCellRenderer<*>? = null

    init {
        ListSpeedSearch.installOn(myList) { it.typeName + "." + it.attributeName }
    }

    override fun editSelectedItem(item: ImpExWrapStringExclusion?): ImpExWrapStringExclusion? {
        if (item == null) return null
        return ImpExWrapStringExclusionDialog(project, item, this, "Edit Exclusion").showAndGet()
            .let { item }
    }

    override fun findItemToAdd(): ImpExWrapStringExclusion {
        val item = ImpExWrapStringExclusion("", "")

        return ImpExWrapStringExclusionDialog(project, item, this, "Define Exclusion").showAndGet()
            .let { item }
    }


    override fun getListCellRenderer(): ListCellRenderer<*> {
        if (myListCellRenderer == null) {
            myListCellRenderer = object : DefaultListCellRenderer() {

                override fun getListCellRendererComponent(list: JList<*>, value: Any, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
                    val name = value.toString()
                    val comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
                    (comp as JComponent).border = JBEmptyBorder(5)
                    icon = value.asSafely<ImpExWrapStringExclusion>()
                        ?.typeName
                        ?.let { TSMetaModelAccess.getInstance(project).findMetaItemByName(it) }
                        ?.icon
                        ?: HybrisIcons.TypeSystem.Types.UNKNOWN
                    text = name

                    return comp
                }

                @Serial
                private val serialVersionUID: Long = -7680459678226925362L
            }
        }
        return myListCellRenderer!!
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = -4193538914487200332L
    }
}