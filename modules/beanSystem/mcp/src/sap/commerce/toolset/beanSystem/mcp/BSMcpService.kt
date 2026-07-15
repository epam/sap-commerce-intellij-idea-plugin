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
import sap.commerce.toolset.beanSystem.mcp.dto.*
import sap.commerce.toolset.beanSystem.mcp.providers.BSMcpDataProvider
import sap.commerce.toolset.beanSystem.meta.model.BSGlobalMetaBean
import sap.commerce.toolset.beanSystem.meta.model.BSMetaProperty

@Service(Service.Level.PROJECT)
class BSMcpService(private val project: Project) {

    suspend fun searchBeans(context: BSMcpSearchContext, detail: BSBeanDetail): BSBeanListResponse {
        val result = BSMcpDataProvider.getInstance(project).search<BSGlobalMetaBean>(context)
        val items = result.items.map { it.toDto(detail) }
        return BSBeanListResponse(
            detail = detail.name,
            filter = context.filter?.trim()?.takeIf { it.isNotEmpty() },
            extensions = context.extensions?.sorted(),
            matched = items.size,
            total = result.total,
            items = items,
        )
    }

    private fun BSGlobalMetaBean.toDto(detail: BSBeanDetail): BSBeanDto {
        val full = detail == BSBeanDetail.FULL
        val withProps = detail != BSBeanDetail.BASIC

        return BSBeanDto(
            name = name!!,
            shortName = shortName?.takeIf { it.isNotBlank() },
            extends = extends?.takeIf { it.isNotBlank() },
            template = template?.takeIf { it.isNotBlank() },
            extension = extensionName.takeIf { it.isNotBlank() },
            custom = isCustom.takeIf { it },
            abstract = isAbstract.takeIf { it },
            deprecated = isDeprecated.takeIf { it },
            deprecatedSince = if (full) deprecatedSince?.takeIf { it.isNotBlank() } else null,
            superEquals = if (full) isSuperEquals.takeIf { it } else null,
            description = if (full) description?.takeIf { it.isNotBlank() } else null,
            imports = if (full) imports.mapNotNull { it.type?.takeIf { type -> type.isNotBlank() } }.takeIf { it.isNotEmpty() } else null,
            annotations = if (full) annotations.mapNotNull { it.value?.takeIf { value -> value.isNotBlank() } }.takeIf { it.isNotEmpty() } else null,
            properties = if (withProps) properties.values
                .filter { it.name != null }
                .sortedBy { it.name }
                .map { it.toDto(full) }
                .takeIf { it.isNotEmpty() } else null,
        )
    }

    private fun BSMetaProperty.toDto(full: Boolean) = BSBeanPropertyDto(
        name = name!!,
        type = type?.takeIf { it.isNotBlank() },
        referencedType = referencedType?.takeIf { it.isNotBlank() },
        description = if (full) description?.takeIf { it.isNotBlank() } else null,
        deprecated = if (full) isDeprecated.takeIf { it } else null,
    )

    companion object {
        fun getInstance(project: Project): BSMcpService = project.service()
        suspend fun getInstance(): BSMcpService = currentCoroutineContext().project.service()
    }
}
