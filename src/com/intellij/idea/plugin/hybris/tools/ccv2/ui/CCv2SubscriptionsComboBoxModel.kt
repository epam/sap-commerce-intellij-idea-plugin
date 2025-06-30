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

package com.intellij.idea.plugin.hybris.tools.ccv2.ui

import com.intellij.idea.plugin.hybris.settings.CCv2Subscription
import com.intellij.idea.plugin.hybris.settings.components.ApplicationSettingsComponent
import com.intellij.idea.plugin.hybris.settings.components.DeveloperSettingsComponent
import com.intellij.idea.plugin.hybris.tools.ccv2.CCv2Service
import com.intellij.idea.plugin.hybris.tools.ccv2.CCv2SettingsListener
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import java.io.Serial
import javax.swing.DefaultComboBoxModel

class CCv2SubscriptionsComboBoxModel(
    private val onSelectedItem: ((Any?) -> Unit)? = null
) : DefaultComboBoxModel<CCv2Subscription>() {

    override fun setSelectedItem(anObject: Any?) {
        super.setSelectedItem(anObject)
        onSelectedItem?.invoke(anObject)
    }

    fun refresh() {
        removeAllElements()
        val subscriptions = ApplicationSettingsComponent.getInstance().state.ccv2Subscriptions
        addAll(subscriptions.sortedBy { it.toString() })
    }

    companion object {
        @Serial
        private val serialVersionUID: Long = 4646717472092758251L
    }
}


object CCv2SubscriptionsComboBoxModelFactory {

    fun create(
        project: Project,
        selectedSubscription: CCv2Subscription? = null,
        allowBlank: Boolean = false,
        disposable: Disposable? = null,
        onSelectedItem: ((Any?) -> Unit)? = null
    ) = CCv2SubscriptionsComboBoxModel(onSelectedItem)
        .also {
            val currentSubscriptions = ApplicationSettingsComponent.getInstance().state.ccv2Subscriptions
            initModel(project, it, selectedSubscription, currentSubscriptions, allowBlank)

            if (disposable != null) {
                with(project.messageBus.connect(disposable)) {
                    subscribe(CCv2Service.TOPIC_CCV2_SETTINGS, object : CCv2SettingsListener {
                        override fun onSubscriptionsChanged(subscriptions: List<CCv2Subscription>) {
                            initModel(project, it, selectedSubscription, subscriptions, allowBlank)
                        }
                    })
                }
            }
        }

    private fun initModel(
        project: Project,
        model: CCv2SubscriptionsComboBoxModel,
        selectedSubscription: CCv2Subscription?,
        subscriptions: List<CCv2Subscription>,
        allowBlank: Boolean
    ) {
        model.removeAllElements()
        if (allowBlank) model.addElement(null)
        model.addAll(subscriptions.sortedBy { it.toString() })
        model.selectedItem = selectedSubscription
            ?: DeveloperSettingsComponent.getInstance(project).getActiveCCv2Subscription()
    }
}