/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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
package com.intellij.idea.plugin.hybris.settings.components

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.HybrisConstants.STORAGE_HYBRIS_INTEGRATION_SETTINGS
import com.intellij.idea.plugin.hybris.settings.ApplicationSettings
import com.intellij.idea.plugin.hybris.settings.CCv2Subscription
import com.intellij.idea.plugin.hybris.tools.ccv2.CCv2Service
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.*
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.util.xmlb.XmlSerializerUtil
import org.apache.commons.lang3.StringUtils

@State(
    name = "[y] Global Settings",
    category = SettingsCategory.PLUGINS,
    storages = [Storage(value = STORAGE_HYBRIS_INTEGRATION_SETTINGS, roamingType = RoamingType.DISABLED)]
)
@Service
class ApplicationSettingsComponent : PersistentStateComponent<ApplicationSettings> {

    private val hybrisApplicationSettings = ApplicationSettings()
    val ccv2Token: String?
        get() = PasswordSafe.instance.get(CredentialAttributes(HybrisConstants.SECURE_STORAGE_SERVICE_NAME_SAP_CX_CCV2_TOKEN))
            ?.getPasswordAsString()

    override fun getState() = this.hybrisApplicationSettings

    override fun loadState(state: ApplicationSettings) {
        XmlSerializerUtil.copyBean(state, this.hybrisApplicationSettings)
    }

    fun loadSAPCLIToken(callback: (String?) -> Unit) {
        ProgressManager.getInstance().run(object : Task.Backgroundable(null, "Retrieving SAP CCv2 Token", false) {
            override fun run(indicator: ProgressIndicator) {
                callback.invoke(ccv2Token)
            }
        })
    }

    fun saveSAPCLIToken(token: String, callback: ((String?) -> Unit)? = null) {
        ProgressManager.getInstance().run(object : Task.Backgroundable(null, "Persisting SAP CCv2 Token", false) {
            override fun run(indicator: ProgressIndicator) {
                val credentialAttributes = CredentialAttributes(HybrisConstants.SECURE_STORAGE_SERVICE_NAME_SAP_CX_CCV2_TOKEN)

                callback?.invoke(token)

                PasswordSafe.instance.setPassword(credentialAttributes, token)
            }
        })
    }

    fun getCCv2Subscription(id: String) = state.ccv2Subscriptions
        .find { it.id == id }

    fun setCCv2Subscriptions(subscriptions: List<CCv2Subscription>) {
        state.ccv2Subscriptions = subscriptions
        ApplicationManager.getApplication().messageBus
            .syncPublisher(CCv2Service.TOPIC_CCV2_SETTINGS)
            .onSubscriptionsChanged(subscriptions)
    }

    companion object {
        @JvmStatic
        fun getInstance(): ApplicationSettingsComponent = ApplicationManager.getApplication().getService(ApplicationSettingsComponent::class.java)

        @JvmStatic
        fun toIdeaGroup(group: String?): Array<String>? {
            if (group == null || group.trim { it <= ' ' }.isEmpty()) {
                return null
            }
            return StringUtils.split(group, " ,.;>/\\")
        }
    }
}
