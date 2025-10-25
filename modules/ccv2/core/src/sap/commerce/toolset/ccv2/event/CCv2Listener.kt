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

package sap.commerce.toolset.ccv2.event

import sap.commerce.toolset.ccv2.dto.CCv2Dto
import sap.commerce.toolset.ccv2.dto.CCv2EnvironmentDto
import sap.commerce.toolset.ccv2.settings.state.CCv2Subscription

sealed interface CCv2Listener<T : CCv2Dto> {
    fun onFetchingStarted(data: Collection<CCv2Subscription>) = Unit
    fun onFetchingCompleted(data: Map<CCv2Subscription, Collection<T>> = emptyMap()) = Unit

    fun onFetchingBuildDetailsStarted(data: Map<CCv2Subscription, Collection<T>>) = Unit
    fun onFetchingBuildDetailsCompleted(data: Map<CCv2Subscription, Collection<T>> = emptyMap()) = Unit

    fun onEndpointUpdate(data: CCv2EnvironmentDto) = Unit
}