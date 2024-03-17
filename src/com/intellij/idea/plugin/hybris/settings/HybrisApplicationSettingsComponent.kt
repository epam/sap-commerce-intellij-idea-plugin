/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
package com.intellij.idea.plugin.hybris.settings

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.HybrisConstants.STORAGE_HYBRIS_INTEGRATION_SETTINGS
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
class HybrisApplicationSettingsComponent : PersistentStateComponent<HybrisApplicationSettings> {

    private val hybrisApplicationSettings = HybrisApplicationSettings()
    val sapCLIToken: String?
        get() = PasswordSafe.instance.get(CredentialAttributes(HybrisConstants.SECURE_STORAGE_SERVICE_NAME_SAP_CX_CLI_TOKEN))
            ?.getPasswordAsString()

    override fun getState() = this.hybrisApplicationSettings

    override fun loadState(state: HybrisApplicationSettings) {
        XmlSerializerUtil.copyBean(state, this.hybrisApplicationSettings)
    }

    fun loadSAPCLIToken(callback: (String?) -> Unit) {
        ProgressManager.getInstance().run(object : Task.Backgroundable(null, "Retrieving SAP CLI Token", false) {
            override fun run(indicator: ProgressIndicator) {
                callback.invoke(sapCLIToken)
            }
        })
    }

    fun saveSAPCLIToken(token: String, callback: ((String?) -> Unit)? = null) {
        ProgressManager.getInstance().run(object : Task.Backgroundable(null, "Persisting SAP CLI Token", false) {
            override fun run(indicator: ProgressIndicator) {
                val credentialAttributes = CredentialAttributes(HybrisConstants.SECURE_STORAGE_SERVICE_NAME_SAP_CX_CLI_TOKEN)

                callback?.invoke(token)

                PasswordSafe.instance.setPassword(credentialAttributes, token)
            }
        })
    }

    companion object {
        @JvmStatic
        fun getInstance(): HybrisApplicationSettingsComponent = ApplicationManager.getApplication().getService(HybrisApplicationSettingsComponent::class.java)

        @JvmStatic
        fun toIdeaGroup(group: String?): Array<String>? {
            if (group == null || group.trim { it <= ' ' }.isEmpty()) {
                return null
            }
            return StringUtils.split(group, " ,.;>/\\")
        }
    }
}
