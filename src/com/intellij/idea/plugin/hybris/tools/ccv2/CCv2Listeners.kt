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

package com.intellij.idea.plugin.hybris.tools.ccv2

import com.intellij.idea.plugin.hybris.settings.CCv2Subscription
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2Build
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2DTO
import com.intellij.idea.plugin.hybris.tools.ccv2.dto.CCv2Environment

sealed interface CCv2Listener<T : CCv2DTO> {
    fun fetchingStarted() = Unit
    fun fetchingCompleted(data: Map<CCv2Subscription, Collection<T>>) = Unit
}

interface CCv2EnvironmentsListener : CCv2Listener<CCv2Environment>

interface CCv2BuildsListener : CCv2Listener<CCv2Build> {
    fun buildStarted() = Unit
    fun buildRequested(subscription: CCv2Subscription, build: CCv2Build? = null) = Unit
    fun buildRemovalStarted(subscription: CCv2Subscription, build: CCv2Build) = Unit
    fun buildRemovalRequested(subscription: CCv2Subscription, build: CCv2Build) = Unit
}
