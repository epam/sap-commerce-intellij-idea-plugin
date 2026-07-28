/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.beanSystem.mcp

import com.intellij.mcpserver.project
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.currentCoroutineContext
import sap.commerce.toolset.beanSystem.mcp.context.BSDetail
import sap.commerce.toolset.beanSystem.mcp.context.BSSearchMcpRequest
import sap.commerce.toolset.beanSystem.mcp.dto.BSBeanDto
import sap.commerce.toolset.beanSystem.mcp.dto.BSBeanPropertyDto
import sap.commerce.toolset.beanSystem.mcp.dto.BSBeanSystemDto
import sap.commerce.toolset.beanSystem.mcp.dto.BSBeansDto
import sap.commerce.toolset.beanSystem.mcp.dto.BSEnumDto
import sap.commerce.toolset.beanSystem.mcp.providers.BSMcpDataProvider
import sap.commerce.toolset.beanSystem.meta.model.BSGlobalMetaBean
import sap.commerce.toolset.beanSystem.meta.model.BSGlobalMetaClassifier
import sap.commerce.toolset.beanSystem.meta.model.BSGlobalMetaEnum
import sap.commerce.toolset.beanSystem.meta.model.BSMetaProperty
import sap.commerce.toolset.beanSystem.meta.model.BSMetaType

@Service(Service.Level.PROJECT)
class BSMcpService(private val project: Project) {

    suspend fun searchBeans(request: BSSearchMcpRequest): BSBeansDto<BSBeanDto> =
        search(request) { bean: BSGlobalMetaBean -> bean.toDto(request.detail) }

    suspend fun searchEnums(request: BSSearchMcpRequest): BSBeansDto<BSEnumDto> =
        search(request) { enum: BSGlobalMetaEnum -> enum.toDto(request.detail) }

    suspend fun getBeanSystem(extensions: String?, beanDetail: BSDetail, enumDetail: BSDetail): BSBeanSystemDto {
        val beanRequest = BSSearchMcpRequest(BSMetaType.META_BEAN, null, beanDetail, extensions)
        val beans = search(beanRequest) { bean: BSGlobalMetaBean -> bean.toDto(beanDetail) }
        val wsBeans = search(BSSearchMcpRequest(BSMetaType.META_WS_BEAN, null, beanDetail, extensions)) { bean: BSGlobalMetaBean -> bean.toDto(beanDetail) }
        val events = search(BSSearchMcpRequest(BSMetaType.META_EVENT, null, beanDetail, extensions)) { bean: BSGlobalMetaBean -> bean.toDto(beanDetail) }
        val enums = search(BSSearchMcpRequest(BSMetaType.META_ENUM, null, enumDetail, extensions)) { enum: BSGlobalMetaEnum -> enum.toDto(enumDetail) }
        return BSBeanSystemDto(
            extensions = beanRequest.extensions?.sorted(),
            beans = beans.items,
            wsBeans = wsBeans.items,
            events = events.items,
            enums = enums.items,
        )
    }

    private suspend fun <T : BSGlobalMetaClassifier<*>, D> search(
        request: BSSearchMcpRequest,
        toDto: (T) -> D,
    ): BSBeansDto<D> {
        val result = BSMcpDataProvider.getInstance(project).search<T>(request)
        val items = result.items.map(toDto)

        return BSBeansDto(
            detail = request.detail.name,
            filter = request.filter?.trim()?.takeIf { it.isNotEmpty() },
            extensions = request.extensions?.sorted(),
            matched = items.size,
            total = result.total,
            items = items,
        )
    }

    private fun BSGlobalMetaBean.toDto(detail: BSDetail) = BSBeanDto(
        name = name!!,
        shortName = shortName?.takeIf { it.isNotBlank() },
        extends = extends?.takeIf { it.isNotBlank() },
        template = template?.takeIf { it.isNotBlank() },
        extension = extensionName.takeIf { it.isNotBlank() },
        custom = isCustom.takeIf { it },
        abstract = isAbstract.takeIf { it },
        deprecated = isDeprecated.takeIf { it },
        deprecatedSince = if (detail.full) deprecatedSince?.takeIf { it.isNotBlank() } else null,
        superEquals = if (detail.full) isSuperEquals.takeIf { it } else null,
        description = if (detail.full) description?.takeIf { it.isNotBlank() } else null,
        imports = if (detail.full) imports.mapNotNull { it.type?.takeIf { type -> type.isNotBlank() } }.takeIf { it.isNotEmpty() } else null,
        annotations = if (detail.full) annotations.mapNotNull { it.value?.takeIf { value -> value.isNotBlank() } }.takeIf { it.isNotEmpty() } else null,
        properties = if (detail.withMembers) properties.values
            .filter { it.name != null }
            .sortedBy { it.name }
            .map { it.toDto(detail.full) }
            .takeIf { it.isNotEmpty() } else null,
    )

    private fun BSGlobalMetaEnum.toDto(detail: BSDetail) = BSEnumDto(
        name = name!!,
        shortName = shortName?.takeIf { it.isNotBlank() },
        extension = extensionName.takeIf { it.isNotBlank() },
        custom = isCustom.takeIf { it },
        deprecated = isDeprecated.takeIf { it },
        deprecatedSince = if (detail.full) deprecatedSince?.takeIf { it.isNotBlank() } else null,
        description = if (detail.full) description?.takeIf { it.isNotBlank() } else null,
        values = if (detail.withMembers) values.values.mapNotNull { it.name }.takeIf { it.isNotEmpty() } else null,
    )

    private fun BSMetaProperty.toDto(full: Boolean) = BSBeanPropertyDto(
        name = name!!,
        type = type?.takeIf { it.isNotBlank() },
        referencedType = referencedType?.takeIf { it.isNotBlank() },
        description = if (full) description?.takeIf { it.isNotBlank() } else null,
        deprecated = if (full) isDeprecated.takeIf { it } else null,
    )

    companion object {
        suspend fun getInstance(): BSMcpService = currentCoroutineContext().project.service()
    }
}