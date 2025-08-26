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

package sap.commerce.toolset.ccv2.settings

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.components.*
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.util.ModificationTracker
import com.intellij.util.application
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.ccv2.CCv2Constants
import sap.commerce.toolset.ccv2.event.CCv2SettingsListener
import sap.commerce.toolset.ccv2.settings.state.CCv2ApplicationSettingsState
import sap.commerce.toolset.ccv2.settings.state.CCv2Subscription

@State(
    name = "[y] CCv2 Project Settings",
    category = SettingsCategory.PLUGINS,
    storages = [Storage(value = HybrisConstants.STORAGE_HYBRIS_INTEGRATION_SETTINGS, roamingType = RoamingType.LOCAL)]
)
@Service
class CCv2ProjectSettings : SerializablePersistentStateComponent<CCv2ApplicationSettingsState>(CCv2ApplicationSettingsState()), ModificationTracker {

    var readTimeout: Int
        get() = state.readTimeout
        set(value) {
            updateState { it.copy(readTimeout = value) }
        }
    var subscriptions: List<CCv2Subscription>
        get() = state.subscriptions
        set(value) {
            updateState { it.copy(subscriptions = value) }

            application.messageBus
                .syncPublisher(CCv2SettingsListener.TOPIC)
                .onChange(state)
        }

    fun getCCv2Token(subscriptionUUID: String? = null) = PasswordSafe.instance.get(getCredentials(subscriptionUUID))
        ?.getPasswordAsString()
        ?.takeIf { it.isNotBlank() }

    fun loadDefaultCCv2Token(callback: (String?) -> Unit) {
        ProgressManager.getInstance().run(object : Task.Backgroundable(null, "Retrieving SAP CCv2 Token", false) {
            override fun run(indicator: ProgressIndicator) {
                callback.invoke(getCCv2Token())
            }
        })
    }

    fun loadCCv2Token(subscriptionUUID: String?, callback: (String?) -> Unit) {
        subscriptionUUID ?: return
        ProgressManager.getInstance().run(object : Task.Backgroundable(null, "Retrieving SAP CCv2 Token", false) {
            override fun run(indicator: ProgressIndicator) {
                callback.invoke(getCCv2Token(subscriptionUUID))
            }
        })
    }

    fun saveDefaultCCv2Token(token: String?, callback: ((String?) -> Unit)? = null) = saveCCv2Token(null, token, callback)

    fun saveCCv2Token(subscriptionUUID: String?, token: String?, callback: ((String?) -> Unit)? = null) {
        ProgressManager.getInstance().run(object : Task.Backgroundable(null, "Persisting SAP CCv2 Token", false) {
            override fun run(indicator: ProgressIndicator) {
                callback?.invoke(token)

                if (token.isNullOrEmpty()) PasswordSafe.instance.setPassword(getCredentials(subscriptionUUID), null)
                else PasswordSafe.instance.setPassword(getCredentials(subscriptionUUID), token)
            }
        })
    }

    fun getCCv2Subscription(uuid: String) = subscriptions
        .find { it.uuid == uuid }

    private fun getCredentials(subscriptionUUID: String?) = if (subscriptionUUID == null) CredentialAttributes(CCv2Constants.SECURE_STORAGE_SERVICE_NAME_SAP_CX_CCV2_TOKEN)
    else CredentialAttributes(subscriptionUUID, CCv2Constants.SECURE_STORAGE_SERVICE_NAME_SAP_CX_CCV2_TOKEN)

    override fun getModificationCount() = stateModificationCount

    fun mutable() = state.mutable()

    companion object {
        @JvmStatic
        fun getInstance(): CCv2ProjectSettings = application.service()
    }
}
