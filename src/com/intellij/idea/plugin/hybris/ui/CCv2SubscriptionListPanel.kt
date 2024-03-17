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

import com.intellij.idea.plugin.hybris.settings.CCv2Subscription
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.AddEditDeleteListPanel
import com.intellij.ui.ListSpeedSearch
import com.intellij.ui.dsl.builder.*
import java.awt.Component
import java.io.Serial
import java.util.*

class CCv2SubscriptionListPanel(initialList: List<CCv2Subscription>) : AddEditDeleteListPanel<CCv2Subscription>(null, initialList) {

    init {
        ListSpeedSearch.installOn(myList) { it.name }
    }

    override fun findItemToAdd(): CCv2Subscription? {
        val newSubscription = CCv2Subscription()

        return if (CCv2SubscriptionDialog(this, newSubscription, "Create CCv2 Subscription").showAndGet()) newSubscription
        else null
    }

    override fun editSelectedItem(item: CCv2Subscription) = if (CCv2SubscriptionDialog(this, item, "Edit CCv2 Subscription").showAndGet()) item
    else null

    var data: List<CCv2Subscription>
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

    class CCv2SubscriptionDialog(
        parentComponent: Component,
        private val subscription: CCv2Subscription,
        dialogTitle: String
    ) : DialogWrapper(null, parentComponent, false, IdeModalityType.IDE) {

        init {
            title = dialogTitle
            super.init()
        }

        override fun createCenterPanel() = panel {
            row {
                textField()
                    .label("ID:")
                    .align(AlignX.FILL)
                    .addValidationRule("ID cannot be blank.") { it.text.isBlank() }
                    .bindText(subscription::id.toNonNullableProperty(""))
            }.layout(RowLayout.PARENT_GRID)

            row {
                textField()
                    .label("Name:")
                    .align(AlignX.FILL)
                    .bindText(subscription::name.toNonNullableProperty(""))
            }.layout(RowLayout.PARENT_GRID)
        }
    }

}
