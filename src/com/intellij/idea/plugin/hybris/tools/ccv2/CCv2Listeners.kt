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

package com.intellij.idea.plugin.hybris.tools.ccv2

import com.intellij.idea.plugin.hybris.tools.ccv2.dto.*
import com.intellij.idea.plugin.hybris.tools.ccv2.settings.state.CCv2Subscription
import com.intellij.util.messages.Topic

sealed interface CCv2Listener<T : CCv2DTO> {
    fun onFetchingStarted(subscriptions: Collection<CCv2Subscription>) = Unit
    fun onFetchingCompleted(data: Map<CCv2Subscription, Collection<T>> = emptyMap()) = Unit

    fun onFetchingBuildDetailsStarted(data: Map<CCv2Subscription, Collection<T>>) = Unit
    fun onFetchingBuildDetailsCompleted(data: Map<CCv2Subscription, Collection<T>> = emptyMap()) = Unit
}

interface CCv2SettingsListener {
    fun onSubscriptionsChanged(subscriptions: List<CCv2Subscription>) = Unit
    fun onActiveSubscriptionChanged(subscription: CCv2Subscription?) = Unit

    companion object {
        val TOPIC = Topic( CCv2SettingsListener::class.java)
    }
}

interface CCv2EnvironmentsListener : CCv2Listener<CCv2EnvironmentDto> {
    companion object {
        val TOPIC = Topic(CCv2EnvironmentsListener::class.java)
    }
}

interface CCv2DeploymentsListener : CCv2Listener<CCv2DeploymentDto> {
    companion object {
        val TOPIC = Topic( CCv2DeploymentsListener::class.java)
    }
}

interface CCv2BuildsListener : CCv2Listener<CCv2BuildDto> {
    fun onBuildStarted() = Unit
    fun onBuildCompleted(subscription: CCv2Subscription, buildCode: String, deploymentRequests: Collection<CCv2DeploymentRequest>) = Unit
    fun onBuildRemovalStarted(subscription: CCv2Subscription, build: CCv2BuildDto) = Unit
    fun onBuildRemovalRequested(subscription: CCv2Subscription, build: CCv2BuildDto) = Unit

    fun onBuildDeploymentStarted(subscription: CCv2Subscription, build: CCv2BuildDto) = Unit
    fun onBuildDeploymentRequested(subscription: CCv2Subscription, build: CCv2BuildDto) = Unit

    companion object {
        val TOPIC = Topic( CCv2BuildsListener::class.java)
    }
}
