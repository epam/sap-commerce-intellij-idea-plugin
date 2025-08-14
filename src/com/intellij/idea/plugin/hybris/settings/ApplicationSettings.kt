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

package com.intellij.idea.plugin.hybris.settings

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.settings.state.ApplicationSettingsState
import com.intellij.idea.plugin.hybris.tools.ccv2.CCv2SettingsListener
import com.intellij.idea.plugin.hybris.tools.ccv2.settings.state.CCv2Subscription
import com.intellij.idea.plugin.hybris.tools.ccv2.settings.state.CCv2SubscriptionDto
import com.intellij.openapi.components.*
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.util.application
import org.apache.commons.lang3.StringUtils

@State(
    name = "[y] Global Settings",
    category = SettingsCategory.PLUGINS,
    storages = [Storage(value = HybrisConstants.STORAGE_HYBRIS_INTEGRATION_SETTINGS, roamingType = RoamingType.DISABLED)]
)
@Service
class ApplicationSettings : SerializablePersistentStateComponent<ApplicationSettingsState>(ApplicationSettingsState()) {

    var groupModules: Boolean
        get() = state.groupModules
        set(value) {
            updateState { it.copy(groupModules = value) }
        }
    var groupExternalModules: Boolean
        get() = state.groupExternalModules
        set(value) {
            updateState { it.copy(groupExternalModules = value) }
        }
    var hideEmptyMiddleFolders: Boolean
        get() = state.hideEmptyMiddleFolders
        set(value) {
            updateState { it.copy(hideEmptyMiddleFolders = value) }
        }
    var defaultPlatformInReadOnly: Boolean
        get() = state.defaultPlatformInReadOnly
        set(value) {
            updateState { it.copy(defaultPlatformInReadOnly = value) }
        }
    var followSymlink: Boolean
        get() = state.followSymlink
        set(value) {
            updateState { it.copy(followSymlink = value) }
        }
    var sourceZipUsed: Boolean
        get() = state.sourceZipUsed
        set(value) {
            updateState { it.copy(sourceZipUsed = value) }
        }
    var warnIfGeneratedItemsAreOutOfDate: Boolean
        get() = state.warnIfGeneratedItemsAreOutOfDate
        set(value) {
            updateState { it.copy(warnIfGeneratedItemsAreOutOfDate = value) }
        }
    var ignoreNonExistingSourceDirectories: Boolean
        get() = state.ignoreNonExistingSourceDirectories
        set(value) {
            updateState { it.copy(ignoreNonExistingSourceDirectories = value) }
        }
    var withStandardProvidedSources: Boolean
        get() = state.withStandardProvidedSources
        set(value) {
            updateState { it.copy(withStandardProvidedSources = value) }
        }
    var scanThroughExternalModule: Boolean
        get() = state.scanThroughExternalModule
        set(value) {
            updateState { it.copy(scanThroughExternalModule = value) }
        }
    var excludeTestSources: Boolean
        get() = state.excludeTestSources
        set(value) {
            updateState { it.copy(excludeTestSources = value) }
        }
    var importCustomAntBuildFiles: Boolean
        get() = state.importCustomAntBuildFiles
        set(value) {
            updateState { it.copy(importCustomAntBuildFiles = value) }
        }
    var groupHybris: String
        get() = state.groupHybris
        set(value) {
            updateState { it.copy(groupHybris = value) }
        }
    var groupOtherHybris: String
        get() = state.groupOtherHybris
        set(value) {
            updateState { it.copy(groupOtherHybris = value) }
        }
    var groupCustom: String
        get() = state.groupCustom
        set(value) {
            updateState { it.copy(groupCustom = value) }
        }
    var groupNonHybris: String
        get() = state.groupNonHybris
        set(value) {
            updateState { it.copy(groupNonHybris = value) }
        }
    var groupOtherCustom: String
        get() = state.groupOtherCustom
        set(value) {
            updateState { it.copy(groupOtherCustom = value) }
        }
    var groupPlatform: String
        get() = state.groupPlatform
        set(value) {
            updateState { it.copy(groupPlatform = value) }
        }
    var groupCCv2: String
        get() = state.groupCCv2
        set(value) {
            updateState { it.copy(groupCCv2 = value) }
        }
    var groupNameExternalModules: String
        get() = state.groupNameExternalModules
        set(value) {
            updateState { it.copy(groupNameExternalModules = value) }
        }
    var externalDbDriversDirectory: String
        get() = state.externalDbDriversDirectory
        set(value) {
            updateState { it.copy(externalDbDriversDirectory = value) }
        }
    var sourceCodeDirectory: String
        get() = state.sourceCodeDirectory
        set(value) {
            updateState { it.copy(sourceCodeDirectory = value) }
        }
    var junkDirectoryList: List<String>
        get() = state.junkDirectoryList
        set(value) {
            updateState { it.copy(junkDirectoryList = value) }
        }
    var extensionsResourcesToExclude: List<String>
        get() = state.extensionsResourcesToExclude
        set(value) {
            updateState { it.copy(extensionsResourcesToExclude = value) }
        }
    var excludedFromIndexList: List<String>
        get() = state.excludedFromIndexList
        set(value) {
            updateState { it.copy(excludedFromIndexList = value) }
        }
    var ccv2ReadTimeout: Int
        get() = state.ccv2ReadTimeout
        set(value) {
            updateState { it.copy(ccv2ReadTimeout = value) }
        }
    var ccv2Subscriptions: List<CCv2Subscription>
        get() = state.ccv2Subscriptions
        set(value) {
            updateState { it.copy(ccv2Subscriptions = value) }
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

    fun getCCv2Subscription(uuid: String) = ccv2Subscriptions
        .find { it.uuid == uuid }

    fun setCCv2Subscriptions(subscriptionDtos: List<CCv2SubscriptionDto>) {
        val subscriptions = subscriptionDtos.map { it.toModel() }

        ccv2Subscriptions = subscriptions
        application.messageBus
            .syncPublisher(CCv2SettingsListener.TOPIC)
            .onSubscriptionsChanged(subscriptions)
    }

    private fun getCredentials(subscriptionUUID: String?) = if (subscriptionUUID == null) CredentialAttributes(HybrisConstants.SECURE_STORAGE_SERVICE_NAME_SAP_CX_CCV2_TOKEN)
    else CredentialAttributes(subscriptionUUID, HybrisConstants.SECURE_STORAGE_SERVICE_NAME_SAP_CX_CCV2_TOKEN)

    companion object {
        @JvmStatic
        fun getInstance(): ApplicationSettings = application.service()

        @JvmStatic
        fun toIdeaGroup(group: String?): Array<String>? {
            if (group == null || group.trim { it <= ' ' }.isEmpty()) {
                return null
            }
            return StringUtils.split(group, " ,.;>/\\")
        }
    }
}