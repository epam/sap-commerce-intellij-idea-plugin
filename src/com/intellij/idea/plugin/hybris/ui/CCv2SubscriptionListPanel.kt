/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
package com.intellij.idea.plugin.hybris.ui

import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.settings.CCv2SubscriptionDto
import com.intellij.ui.AddEditDeleteListPanel
import com.intellij.ui.ListSpeedSearch
import com.intellij.util.ui.JBEmptyBorder
import java.awt.Component
import java.io.Serial
import java.util.*
import javax.swing.DefaultListCellRenderer
import javax.swing.JComponent
import javax.swing.JList
import javax.swing.ListCellRenderer

class CCv2SubscriptionListPanel(initialList: List<CCv2SubscriptionDto>) : AddEditDeleteListPanel<CCv2SubscriptionDto>(null, initialList) {

    private var myListCellRenderer: ListCellRenderer<*>? = null

    init {
        ListSpeedSearch.installOn(myList) { it.name }
    }

    override fun findItemToAdd(): CCv2SubscriptionDto? {
        val newSubscription = CCv2SubscriptionDto()

        return if (CCv2SubscriptionDialog(this, newSubscription, "Create CCv2 Subscription").showAndGet()) newSubscription
        else null
    }

    override fun editSelectedItem(item: CCv2SubscriptionDto) = if (CCv2SubscriptionDialog(this, item, "Edit CCv2 Subscription").showAndGet()) item
    else null

    override fun getListCellRenderer(): ListCellRenderer<*> {
        if (myListCellRenderer == null) {
            myListCellRenderer = object : DefaultListCellRenderer() {

                override fun getListCellRendererComponent(list: JList<*>, value: Any, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
                    val comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
                    (comp as JComponent).border = JBEmptyBorder(5)
                    icon = HybrisIcons.MODULE_CCV2

                    return comp
                }

                @Serial
                private val serialVersionUID: Long = -7680459678226925362L
            }
        }
        return myListCellRenderer!!
    }

    var data: List<CCv2SubscriptionDto>
        get() = Collections.list(myListModel.elements())
        set(itemList) {
            myListModel.clear()
            for (itemToAdd in itemList) {
                super.addElement(itemToAdd)
            }
        }

    companion object {
        @Serial
        private val serialVersionUID: Long = 3757468168747276336L
    }

}
