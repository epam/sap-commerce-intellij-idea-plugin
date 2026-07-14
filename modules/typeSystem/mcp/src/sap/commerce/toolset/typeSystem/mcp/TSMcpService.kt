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

package sap.commerce.toolset.typeSystem.mcp

import com.intellij.mcpserver.project
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.currentCoroutineContext
import sap.commerce.toolset.typeSystem.mcp.dto.*
import sap.commerce.toolset.typeSystem.mcp.providers.TSMcpDataProvider
import sap.commerce.toolset.typeSystem.meta.model.*

@Service(Service.Level.PROJECT)
class TSMcpService(private val project: Project) {

    suspend fun searchItemTypes(context: TSMcpSearchContext): TSItemListResponse {
        val result = TSMcpDataProvider.getInstance(project).search<TSGlobalMetaItem>(context)
        val items = result.items.map { it.toDto(context.detailLevel) }
        return TSItemListResponse(
            detail = context.detailLevel.name,
            filter = context.filter?.trim()?.takeIf { it.isNotEmpty() },
            extensions = context.extensions?.sorted(),
            matched = items.size,
            total = result.total,
            items = items,
        )
    }

    suspend fun searchAtomicTypes(context: TSMcpSearchContext): TSAtomicListResponse {
        val result = TSMcpDataProvider.getInstance(project).search<TSGlobalMetaAtomic>(context)
        val items = result.items.map { it.toDto() }
        return TSAtomicListResponse(
            filter = context.filter?.trim()?.takeIf { it.isNotEmpty() },
            extensions = context.extensions?.sorted(),
            matched = items.size,
            total = result.total,
            items = items,
        )
    }

    suspend fun searchCollectionTypes(context: TSMcpSearchContext): TSCollectionListResponse {
        val result = TSMcpDataProvider.getInstance(project).search<TSGlobalMetaCollection>(context)
        val items = result.items.map { it.toDto() }
        return TSCollectionListResponse(
            filter = context.filter?.trim()?.takeIf { it.isNotEmpty() },
            extensions = context.extensions?.sorted(),
            matched = items.size,
            total = result.total,
            items = items,
        )
    }

    suspend fun searchEnumTypes(context: TSMcpSearchContext, detail: EnumTypeDetail): TSEnumListResponse {
        val result = TSMcpDataProvider.getInstance(project).search<TSGlobalMetaEnum>(context)
        val items = result.items.map { it.toDto(detail) }
        return TSEnumListResponse(
            detail = detail.name,
            filter = context.filter?.trim()?.takeIf { it.isNotEmpty() },
            extensions = context.extensions?.sorted(),
            matched = items.size,
            total = result.total,
            items = items,
        )
    }

    suspend fun searchMapTypes(context: TSMcpSearchContext): TSMapListResponse {
        val result = TSMcpDataProvider.getInstance(project).search<TSGlobalMetaMap>(context)
        val items = result.items.map { it.toDto() }
        return TSMapListResponse(
            filter = context.filter?.trim()?.takeIf { it.isNotEmpty() },
            extensions = context.extensions?.sorted(),
            matched = items.size,
            total = result.total,
            items = items,
        )
    }

    private fun TSGlobalMetaItem.toDto(detail: ItemTypeDetail): TSItemDto {
        val attrs = if (detail != ItemTypeDetail.TYPES) {
            attributes.values.sortedBy { it.name }.map { it.toAttributeDto(detail) }
        } else null

        return TSItemDto(
            name = name!!,
            extends = extendedMetaItemName?.takeIf { it.isNotBlank() },
            typeCode = deployment?.typeCode?.takeIf { it.isNotBlank() },
            extension = extensionName.takeIf { it.isNotBlank() },
            isAbstract = isAbstract.takeIf { it },
            isCustom = isCustom.takeIf { it },
            isDeprecated = isDeprecated.takeIf { it },
            attributes = attrs,
        )
    }

    private fun TSGlobalMetaItem.TSGlobalMetaItemAttribute.toAttributeDto(detail: ItemTypeDetail): TSItemAttributeDto {
        val full = detail == ItemTypeDetail.FULL
        val (redeclared, declared) = if (full) {
            declarations.filter { it.extensionName.isNotBlank() }.partition { it.isRedeclare }
        } else Pair(emptyList(), emptyList())

        val persistence = if (full) {
            TSAttributePersistenceDto(
                type = persistence.type?.name?.takeIf { it.isNotBlank() },
                qualifier = persistence.qualifier?.takeIf { it.isNotBlank() },
                attributeHandler = persistence.attributeHandler?.takeIf { it.isNotBlank() },
            ).takeIf { it.type != null || it.qualifier != null || it.attributeHandler != null }
        } else null

        return TSItemAttributeDto(
            name = name,
            type = type?.takeIf { it.isNotBlank() },
            declaredIn = if (full) (declared.firstOrNull()?.extensionName ?: extensionName)?.takeIf { it.isNotBlank() } else null,
            redeclaredIn = if (full) redeclared.map { it.extensionName }.distinct().sorted().takeIf { it.isNotEmpty() } else null,
            localized = if (full) isLocalized.takeIf { it } else null,
            dynamic = if (full) isDynamic.takeIf { it } else null,
            deprecated = if (full) isDeprecated.takeIf { it } else null,
            autoCreate = if (full) isAutoCreate.takeIf { it } else null,
            generate = if (full) isGenerate.takeIf { it } else null,
            defaultValue = if (full) defaultValue?.takeIf { it.isNotBlank() } else null,
            selectionOf = if (full) isSelectionOf?.takeIf { it.isNotBlank() } else null,
            flattenType = if (full) flattenType?.takeIf { it.isNotBlank() } else null,
            description = if (full) description?.takeIf { it.isNotBlank() } else null,
            modifiers = if (full) modifiers.activeModifiers().takeIf { it.isNotEmpty() } else null,
            persistence = persistence,
        )
    }

    private fun TSGlobalMetaAtomic.toDto() = TSAtomicDto(
        name = name,
        extends = extends?.takeIf { it.isNotBlank() && name != it },
        extension = extensionName?.takeIf { it.isNotBlank() },
        custom = isCustom.takeIf { it },
        autoCreate = isAutoCreate.takeIf { it },
        generate = isGenerate.takeIf { it },
    )

    private fun TSGlobalMetaCollection.toDto() = TSCollectionDto(
        name = name!!,
        kind = type.value,
        elementType = elementType?.takeIf { it.isNotBlank() },
        extension = extensionName?.takeIf { it.isNotBlank() },
        custom = isCustom.takeIf { it },
        autoCreate = isAutoCreate.takeIf { it },
        generate = isGenerate.takeIf { it },
    )

    private fun TSGlobalMetaEnum.toDto(detail: EnumTypeDetail): TSEnumDto {
        val full = detail == EnumTypeDetail.VALUES
        return TSEnumDto(
            name = name!!,
            extension = extensionName.takeIf { it.isNotBlank() },
            dynamic = isDynamic.takeIf { it },
            custom = isCustom.takeIf { it },
            autoCreate = isAutoCreate.takeIf { it },
            generate = isGenerate.takeIf { it },
            deprecated = isDeprecated.takeIf { it },
            description = if (full) description?.takeIf { it.isNotBlank() } else null,
            values = if (full) values.values.map { it.toDto() } else null,
        )
    }

    private fun TSMetaEnum.TSMetaEnumValue.toDto() = TSEnumValueDto(
        name = name,
        description = description?.takeIf { it.isNotBlank() },
    )

    private fun TSGlobalMetaMap.toDto() = TSMapDto(
        name = name!!,
        argumentType = argumentType?.takeIf { it.isNotBlank() },
        returnType = returnType?.takeIf { it.isNotBlank() },
        extension = extensionName?.takeIf { it.isNotBlank() },
        custom = isCustom.takeIf { it },
        autoCreate = isAutoCreate.takeIf { it },
        generate = isGenerate.takeIf { it },
        redeclare = isRedeclare.takeIf { it },
    )

    companion object {
        fun getInstance(project: Project): TSMcpService = project.service()
        suspend fun getInstance(): TSMcpService = currentCoroutineContext().project.service()
    }
}
