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
package com.intellij.idea.plugin.hybris.system.type.meta

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.root
import com.intellij.idea.plugin.hybris.common.yExtensionName
import com.intellij.idea.plugin.hybris.system.type.meta.impl.TSMetaModelNameProvider
import com.intellij.idea.plugin.hybris.system.type.meta.model.*
import com.intellij.idea.plugin.hybris.system.type.model.EnumType
import com.intellij.idea.plugin.hybris.system.type.model.ItemType
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.util.xml.DomElement
import org.apache.commons.collections4.CollectionUtils
import java.util.*
import kotlin.io.path.exists
import kotlin.io.path.inputStream

/**
 * Global Meta Model can be retrieved at any time and will ensure that only a single Thread can perform its initialization/update
 *
 * The main idea is that we have two levels of Meta Model cache:
 * 1. Global Meta Model cached at Project level with dependencies to all items.xml files in the Project.
 * - processing of the dependant PsiFiles is ignored and done during retrieval from the PsiFile cache
 * - once all dependant PsiFiles processed, each Meta Model will be merged into single one
 * 2. PsiFile (items.xml) specific cache
 * - retrieving of that cache also performs processing of the PsiFile and pre-filling into MetaModel caches
 *
 * It is quite important to take into account the possibility of interruption of the process, especially during Inspection and other heavy operations
 */
@Service(Service.Level.PROJECT)
class TSMetaModelAccess(private val project: Project) : Disposable {

    companion object {
        @JvmStatic
        fun getInstance(project: Project): TSMetaModelAccess = project.getService(TSMetaModelAccess::class.java)
    }

    private val metaModelStateService = project.service<TSMetaModelStateService>()
    private val myReservedTypeCodes by lazy {
        ModuleManager.getInstance(project)
            .modules
            .firstOrNull { it.yExtensionName() == HybrisConstants.EXTENSION_NAME_CORE }
            ?.root()
            ?.resolve(HybrisConstants.RESERVED_TYPE_CODES_FILE)
            ?.takeIf { it.exists() }
            ?.let {
                it.inputStream().use { fis ->
                    Properties().also { p -> p.load(fis) }
                }
            }
            ?.entries
            ?.mapNotNull {
                val key = it.key.toString().toIntOrNull() ?: return@mapNotNull null
                key to it.value.toString()
            }
            ?.associate { it.first to it.second }
            ?: emptyMap()
    }

    fun <T : TSGlobalMetaClassifier<*>> getAll(metaType: TSMetaType) = metaModelStateService.get().getMetaType<T>(metaType).values
    fun getAllOf(vararg metaTypes: TSMetaType): Collection<TSGlobalMetaClassifier<*>> = (metaTypes
        .takeIf { it.isNotEmpty() }
        ?: TSMetaType.entries.toTypedArray()
        )
        .flatMap { getAll(it) }

    fun getAll(): Collection<TSGlobalMetaClassifier<*>> = TSMetaType.entries
        .flatMap { getAll(it) }

    fun findMetaForDom(dom: ItemType) = findMetaItemByName(TSMetaModelNameProvider.extract(dom))
    fun findMetaForDom(dom: EnumType) = findMetaEnumByName(TSMetaModelNameProvider.extract(dom))

    fun findMetaItemByName(name: String?) = findMetaByName<TSGlobalMetaItem>(TSMetaType.META_ITEM, name)
    fun findMetaEnumByName(name: String?) = findMetaByName<TSGlobalMetaEnum>(TSMetaType.META_ENUM, name)
    fun findMetaAtomicByName(name: String?) = findMetaByName<TSGlobalMetaAtomic>(TSMetaType.META_ATOMIC, name)
    fun findMetaCollectionByName(name: String?) = findMetaByName<TSGlobalMetaCollection>(TSMetaType.META_COLLECTION, name)
    fun findMetaMapByName(name: String?) = findMetaByName<TSGlobalMetaMap>(TSMetaType.META_MAP, name)
    fun findMetaRelationByName(name: String?) = findMetaByName<TSGlobalMetaRelation>(TSMetaType.META_RELATION, name)

    fun findRelationByName(name: String?) = CollectionUtils.emptyIfNull(metaModelStateService.get().getAllRelations().values())
        .mapNotNull { metaRelationElement -> metaRelationElement.owner }
        .filter { ref: TSMetaRelation -> name == ref.name }

    fun findMetaClassifierByName(name: String?): TSGlobalMetaClassifier<out DomElement>? = findMetaItemByName(name)
        ?: findMetaCollectionByName(name)
        ?: findMetaRelationByName(name)
        ?: findMetaEnumByName(name)
        ?: findMetaMapByName(name)
        ?: findMetaAtomicByName(name)

    fun getNextAvailableTypeCode(): Int? {
        val projectTypeCodes = metaModelStateService.get().getDeploymentTypeCodes().keys
        val reservedTypesCodes = getReservedTypeCodes().keys
        val keys = projectTypeCodes + reservedTypesCodes

        return (10100..Short.MAX_VALUE)
            .asSequence()
            .filterNot { it in HybrisConstants.TS_TYPECODE_RANGE_COMMONS }
            .filterNot { it in HybrisConstants.TS_TYPECODE_RANGE_XPRINT }
            .filterNot { it in HybrisConstants.TS_TYPECODE_RANGE_PRINT }
            .filterNot { it in HybrisConstants.TS_TYPECODE_RANGE_PROCESSING }
            .firstOrNull { !keys.contains(it) }
    }

    fun getReservedTypeCodes() = myReservedTypeCodes

    private fun <T : TSGlobalMetaClassifier<*>> findMetaByName(metaType: TSMetaType, name: String?): T? = metaModelStateService.get()
        .getMetaType<T>(metaType)[name]

    override fun dispose() {
    }
}