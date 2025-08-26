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

import sap.commerce.toolset.ccv2.settings.CCv2ProjectSettings
import sap.commerce.toolset.ccv2.settings.state.CCv2Subscription
import java.io.Serial
import javax.swing.DefaultComboBoxModel

class CCv2SubscriptionsComboBoxModel(
    private val allowBlank: Boolean = false,
    private val onSelectedItem: ((Any?) -> Unit)? = null
) : DefaultComboBoxModel<CCv2Subscription>() {

    override fun setSelectedItem(anObject: Any?) {
        super.setSelectedItem(anObject)
        onSelectedItem?.invoke(anObject)
    }

    fun refresh(subscriptions: List<CCv2Subscription> = CCv2ProjectSettings.getInstance().subscriptions) {
        removeAllElements()
        if (allowBlank) addElement(null)
        addAll(subscriptions.sortedBy { it.presentableName })
    }

    companion object {
        @Serial
        private val serialVersionUID: Long = 4646717472092758251L
    }
}