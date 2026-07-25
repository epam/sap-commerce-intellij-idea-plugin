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
import sap.commerce.toolset.typeSystem.mcp.context.*
import sap.commerce.toolset.typeSystem.mcp.dto.*
import sap.commerce.toolset.typeSystem.meta.model.*

@Service(Service.Level.PROJECT)
class TSMcpService(private val project: Project) {

    suspend fun searchItems(request: TSItemSearchMcpRequest): TSItemsDto {
        val result = TSMcpDataProvider.getInstance(project).search<TSGlobalMetaItem>(request)
        val items = result.items.map { it.toDto(request.detailLevel) }
        return TSItemsDto(
            detail = request.detailLevel.name,
            filter = request.filter?.trim()?.takeIf { it.isNotEmpty() },
            extensions = request.extensions?.sorted(),
            total = result.total,
            items = items,
        )
    }

    suspend fun searchAtomics(request: TSSearchMcpRequest): TSAtomicsDto {
        val result = TSMcpDataProvider.getInstance(project).search<TSGlobalMetaAtomic>(request)
        val items = result.items.map { it.toDto() }
        return TSAtomicsDto(
            filter = request.filter?.trim()?.takeIf { it.isNotEmpty() },
            extensions = request.extensions?.sorted(),
            total = result.total,
            items = items,
        )
    }

    suspend fun searchCollections(request: TSSearchMcpRequest): TSCollectionsDto {
        val result = TSMcpDataProvider.getInstance(project).search<TSGlobalMetaCollection>(request)
        val items = result.items.map { it.toDto() }
        return TSCollectionsDto(
            filter = request.filter?.trim()?.takeIf { it.isNotEmpty() },
            extensions = request.extensions?.sorted(),
            total = result.total,
            items = items,
        )
    }

    suspend fun searchRelations(request: TSSearchMcpRequest): TSRelationsDto {
        val result = TSMcpDataProvider.getInstance(project).search<TSGlobalMetaRelation>(request)
        val items = result.items.map { it.toDto() }
        return TSRelationsDto(
            filter = request.filter?.trim()?.takeIf { it.isNotEmpty() },
            extensions = request.extensions?.sorted(),
            total = result.total,
            items = items,
        )
    }

    suspend fun searchMaps(request: TSSearchMcpRequest): TSMapsDto {
        val result = TSMcpDataProvider.getInstance(project).search<TSGlobalMetaMap>(request)
        val items = result.items.map { it.toDto() }
        return TSMapsDto(
            filter = request.filter?.trim()?.takeIf { it.isNotEmpty() },
            extensions = request.extensions?.sorted(),
            total = result.total,
            items = items,
        )
    }

    suspend fun searchEnums(request: TSEnumSearchMcpRequest): TSEnumsDto {
        val result = TSMcpDataProvider.getInstance(project).search<TSGlobalMetaEnum>(request)
        val items = result.items.map { it.toDto(request.detailLevel) }
        return TSEnumsDto(
            detail = request.detailLevel.name,
            filter = request.filter?.trim()?.takeIf { it.isNotEmpty() },
            extensions = request.extensions?.sorted(),
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
            abstract = isAbstract.takeIf { it },
            custom = isCustom.takeIf { it },
            deprecated = isDeprecated.takeIf { it },
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
        extends = extends.takeIf { it.isNotBlank() && name != it },
        extension = extensionName.takeIf { it.isNotBlank() },
        custom = isCustom.takeIf { it },
        autoCreate = isAutoCreate.takeIf { it },
        generate = isGenerate.takeIf { it },
    )

    private fun TSGlobalMetaCollection.toDto() = TSCollectionDto(
        name = name!!,
        kind = type.value,
        elementType = elementType.takeIf { it.isNotBlank() },
        extension = extensionName.takeIf { it.isNotBlank() },
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
        extension = extensionName.takeIf { it.isNotBlank() },
        custom = isCustom.takeIf { it },
        autoCreate = isAutoCreate.takeIf { it },
        generate = isGenerate.takeIf { it },
        redeclare = isRedeclare.takeIf { it },
    )

    private fun TSGlobalMetaRelation.toDto() = TSRelationDto(
        name = name!!,
        typeCode = deployment?.typeCode?.takeIf { it.isNotBlank() },
        source = source.toDto(),
        target = target.toDto(),
        extension = extensionName?.takeIf { it.isNotBlank() },
        localized = isLocalized.takeIf { it },
        custom = isCustom.takeIf { it },
        autoCreate = isAutoCreate.takeIf { it },
        generate = isGenerate.takeIf { it },
    )

    private fun TSMetaRelation.TSMetaRelationElement.toDto() = TSRelationEndDto(
        type = type,
        qualifier = qualifier?.takeIf { it.isNotBlank() },
        cardinality = cardinality.value?.takeIf { it.isNotBlank() },
        collectionType = collectionType.value?.takeIf { it.isNotBlank() },
        ordered = isOrdered.takeIf { it },
        navigable = isNavigable.takeIf { it },
    )

    companion object {
        suspend fun getInstance(): TSMcpService = currentCoroutineContext().project.service()
    }
}
