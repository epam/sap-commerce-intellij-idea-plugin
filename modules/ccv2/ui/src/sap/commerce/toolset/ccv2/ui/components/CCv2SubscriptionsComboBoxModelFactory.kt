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
import com.intellij.openapi.project.Project
import sap.commerce.toolset.ccv2.event.CCv2SettingsListener
import sap.commerce.toolset.ccv2.settings.CCv2DeveloperSettings
import sap.commerce.toolset.ccv2.settings.CCv2ProjectSettings
import sap.commerce.toolset.ccv2.settings.state.CCv2ApplicationSettingsState
import sap.commerce.toolset.ccv2.settings.state.CCv2Subscription

object CCv2SubscriptionsComboBoxModelFactory {

    fun create(
        project: Project,
        selectedSubscription: CCv2Subscription? = null,
        allowBlank: Boolean = false,
        disposable: Disposable? = null,
        onSelectedItem: ((Any?) -> Unit)? = null
    ) = CCv2SubscriptionsComboBoxModel(allowBlank, onSelectedItem)
        .also {
            val currentSubscriptions = CCv2ProjectSettings.getInstance().subscriptions
            initModel(project, it, selectedSubscription, currentSubscriptions, allowBlank)

            if (disposable != null) {
                with(project.messageBus.connect(disposable)) {
                    subscribe(CCv2SettingsListener.TOPIC, object : CCv2SettingsListener {
                        override fun onChange(state: CCv2ApplicationSettingsState) {
                            initModel(project, it, selectedSubscription, state.subscriptions, allowBlank)
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
        model.addAll(subscriptions.sortedBy { it.presentableName })
        model.selectedItem = selectedSubscription
            ?: CCv2DeveloperSettings.getInstance(project).getActiveCCv2Subscription()
    }
}