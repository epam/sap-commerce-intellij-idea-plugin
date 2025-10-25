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

package sap.commerce.toolset.ccv2.dto

import sap.commerce.toolset.ccv2.CCv2Constants
import sap.commerce.toolset.ccv2.model.EndpointDetailDTO
import sap.commerce.toolset.ccv2.settings.state.CCv2Subscription

data class CCv2EndpointDto(
    val code: String,
    val name: String,
    val webProxy: String,
    val service: String,
    val url: String,
    val link: String,
    val maintenanceMode: Boolean,
    var actionsAllowed: Boolean = true,
) {
    internal data class MappingDto(
        val subscription: CCv2Subscription,
        var environment: CCv2EnvironmentDto,
        var dto: EndpointDetailDTO,
    )

    companion object {
        internal fun map(mappingDto: MappingDto): CCv2EndpointDto {
            val subscription = mappingDto.subscription
            val environment = mappingDto.environment
            val dto = mappingDto.dto
            return CCv2EndpointDto(
                code = dto.code,
                name = dto.name,
                webProxy = dto.webProxy,
                service = dto.service,
                url = dto.url,
                maintenanceMode = dto.maintenanceMode,
                link = "https://${CCv2Constants.DOMAIN}/subscription/${subscription.id}/applications/commerce-cloud/environments/${environment.code}/endpoints/${dto.code}/edit"
            )
        }
    }
}
