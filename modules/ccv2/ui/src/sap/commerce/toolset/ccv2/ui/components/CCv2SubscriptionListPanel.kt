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

package sap.commerce.toolset.ccv2.ui.components

import com.intellij.openapi.Disposable
import com.intellij.openapi.observable.util.whenListChanged
import com.intellij.openapi.project.Project
import com.intellij.ui.AddEditDeleteListPanel
import com.intellij.ui.ListSpeedSearch
import com.intellij.util.asSafely
import com.intellij.util.ui.JBEmptyBorder
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.ccv2.api.CCv2AuthToken
import sap.commerce.toolset.ccv2.settings.state.CCv2Subscription
import sap.commerce.toolset.ccv2.ui.CCv2SubscriptionDialog
import java.awt.Component
import java.io.Serial
import javax.swing.DefaultListCellRenderer
import javax.swing.JComponent
import javax.swing.JList
import javax.swing.ListCellRenderer
import javax.swing.event.ListDataEvent

internal class CCv2SubscriptionListPanel(
    private val project: Project,
    disposable: Disposable?,
    private val ccv2LegacyTokenSupplier: () -> CCv2AuthToken?,
    private val ccv2ClientTokenSupplier: () -> CCv2AuthToken?,
    listener: (ListDataEvent) -> Unit
) : AddEditDeleteListPanel<CCv2Subscription.Mutable>(null, emptyList()) {

    private var myListCellRenderer: ListCellRenderer<*>? = null

    init {
        ListSpeedSearch.installOn(myList) { it.name }

        myListModel.whenListChanged(disposable) {
            listener(it)
        }
    }

    override fun findItemToAdd(): CCv2Subscription.Mutable? {
        val mutable = CCv2Subscription().mutable().apply {
            modified = true
        }
        return if (CCv2SubscriptionDialog(project,this, mutable, "Create CCv2 Subscription", ccv2LegacyTokenSupplier, ccv2ClientTokenSupplier).showAndGet()) mutable
        else null
    }

    override fun editSelectedItem(item: CCv2Subscription.Mutable): CCv2Subscription.Mutable? {
        val copy = item.copy(
            authentication = item.authentication.copy(),
        ).apply {
            ccv2LegacyTokenLoaded = item.ccv2LegacyTokenLoaded
            ccv2ClientTokenLoaded = item.ccv2ClientTokenLoaded
        }
        return if (CCv2SubscriptionDialog(project,this, copy, "Edit CCv2 Subscription", ccv2LegacyTokenSupplier, ccv2ClientTokenSupplier).showAndGet()) {
            item.apply {
                modified = copy.modified
                ccv2LegacyTokenLoaded = copy.ccv2LegacyTokenLoaded
                ccv2ClientTokenLoaded = copy.ccv2ClientTokenLoaded
                id = copy.id
                name = copy.name
                ccv2Token = copy.ccv2Token
                authenticationMode = copy.authenticationMode
                authentication.apply {
                    tokenEndpoint = copy.authentication.tokenEndpoint
                    resource = copy.authentication.resource
                    clientId = copy.authentication.clientId
                    clientSecret = copy.authentication.clientSecret
                }
            }
        }
        else null
    }

    override fun getListCellRenderer(): ListCellRenderer<*> {
        if (myListCellRenderer == null) {
            myListCellRenderer = object : DefaultListCellRenderer() {

                override fun getListCellRendererComponent(list: JList<*>, value: Any, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
                    val name = value.asSafely<CCv2Subscription.Mutable>()
                        ?.presentableName
                        ?: value.toString()
                    val comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
                    (comp as JComponent).border = JBEmptyBorder(5)
                    icon = HybrisIcons.Module.CCV2
                    text = name

                    return comp
                }

                @Serial
                private val serialVersionUID: Long = -7680459678226925362L
            }
        }
        return myListCellRenderer!!
    }

    var data: List<CCv2Subscription.Mutable>
        get() = myListModel.elements().toList()
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