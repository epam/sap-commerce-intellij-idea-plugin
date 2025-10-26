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

import com.intellij.util.messages.Topic
import sap.commerce.toolset.ccv2.dto.CCv2EnvironmentDto
import sap.commerce.toolset.ccv2.dto.CCv2EnvironmentScalingDto

interface CCv2EnvironmentsListener : CCv2Listener<CCv2EnvironmentDto> {

    fun onEndpointUpdate(data: CCv2EnvironmentDto) = Unit
    fun onScalingFetched(environment: CCv2EnvironmentDto, data: CCv2EnvironmentScalingDto?) = Unit
    fun onScalingFetchingError(environment: CCv2EnvironmentDto, e: Throwable) = Unit

    companion object {
        val TOPIC = Topic(CCv2EnvironmentsListener::class.java)
    }
}