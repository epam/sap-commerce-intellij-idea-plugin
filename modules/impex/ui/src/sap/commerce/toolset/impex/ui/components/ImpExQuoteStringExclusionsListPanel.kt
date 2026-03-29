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
import sap.commerce.toolset.settings.state.ImpExQuoteStringExclusion
import sap.commerce.toolset.typeSystem.meta.TSMetaModelAccess
import sap.commerce.toolset.ui.ifOk
import java.awt.Component
import java.io.Serial
import javax.swing.DefaultListCellRenderer
import javax.swing.JComponent
import javax.swing.JList
import javax.swing.ListCellRenderer

class ImpExQuoteStringExclusionsListPanel(
    private val project: Project,
) : AddEditDeleteListPanel<ImpExQuoteStringExclusion>("", emptyList()) {

    private var myListCellRenderer: ListCellRenderer<*>? = null

    init {
        ListSpeedSearch.installOn(myList) { it.typeName + "." + it.attributeName }
    }

    override fun editSelectedItem(item: ImpExQuoteStringExclusion) = ImpExQuoteStringExclusionDialog(
        project = project,
        exclusion = item,
        parentComponent = this,
        dialogTitle = "Edit Exclusion"
    )
        .ifOk { item }

    override fun findItemToAdd() = ImpExQuoteStringExclusion("", "").let { item ->
        ImpExQuoteStringExclusionDialog(
            project = project,
            exclusion = item,
            parentComponent = this,
            dialogTitle = "Define Exclusion"
        )
            .ifOk { item }
    }

    override fun getListCellRenderer(): ListCellRenderer<*> {
        if (myListCellRenderer == null) {
            myListCellRenderer = object : DefaultListCellRenderer() {

                override fun getListCellRendererComponent(list: JList<*>, value: Any, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
                    val name = value.toString()
                    val comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
                    (comp as JComponent).border = JBEmptyBorder(5)
                    icon = value.asSafely<ImpExQuoteStringExclusion>()
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

    var data: List<ImpExQuoteStringExclusion>
        get() = myListModel.elements().toList()
        set(itemList) {
            myListModel.clear()
            for (itemToAdd in itemList) {
                super.addElement(itemToAdd)
            }
        }

    companion object {
        @Serial
        private const val serialVersionUID: Long = -4193538914487200332L
    }
}
