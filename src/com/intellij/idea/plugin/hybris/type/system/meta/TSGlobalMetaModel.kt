/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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
package com.intellij.idea.plugin.hybris.type.system.meta

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.type.system.meta.impl.CaseInsensitive
import com.intellij.idea.plugin.hybris.type.system.meta.model.*
import com.intellij.openapi.Disposable
import com.intellij.util.xml.DomElement
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class TSGlobalMetaModel : Disposable {

    private val myMetaCache: MutableMap<MetaType, Map<String, TSGlobalMetaClassifier<out DomElement>>> = ConcurrentHashMap()
    private val myReferencesBySourceTypeName = CaseInsensitive.CaseInsensitiveConcurrentHashMap<String, TSMetaRelation.TSMetaRelationElement>()
    private val myDeploymentTables = CaseInsensitive.CaseInsensitiveConcurrentHashMap<String, TSMetaDeployment>();
    private val myDeploymentTypeCodes = ConcurrentHashMap<Int, TSMetaDeployment>();

    override fun dispose() {
        myMetaCache.clear()
        myReferencesBySourceTypeName.clear()
        myDeploymentTables.clear()
    }

    fun getDeploymentForTable(table: String?) : TSMetaDeployment? = if (table != null) myDeploymentTables[table] else null
    fun getDeploymentForTypeCode(typeCode: Int?) : TSMetaDeployment? = if (typeCode != null) myDeploymentTypeCodes[typeCode] else null
    fun getDeploymentForTypeCode(typeCode: String?) : TSMetaDeployment? = getDeploymentForTypeCode(typeCode?.toIntOrNull())
    fun getNextAvailableTypeCode(): Int = myDeploymentTypeCodes.keys
        .asSequence()
        .filter { it < HybrisConstants.TS_TYPECODE_RANGE_PROCESSING.first }
        .filter { it !in HybrisConstants.TS_TYPECODE_RANGE_B2BCOMMERCE }
        .filter { it !in HybrisConstants.TS_TYPECODE_RANGE_COMMONS }
        .filter { it !in HybrisConstants.TS_TYPECODE_RANGE_XPRINT }
        .filter { it !in HybrisConstants.TS_TYPECODE_RANGE_PRINT }
        .filter { it !in HybrisConstants.TS_TYPECODE_RANGE_PROCESSING }
        .maxOf { it } + 1

    @Suppress("UNCHECKED_CAST")
    fun <T : TSGlobalMetaClassifier<*>> getMetaType(metaType: MetaType): ConcurrentMap<String, T> =
        myMetaCache.computeIfAbsent(metaType) { CaseInsensitive.CaseInsensitiveConcurrentHashMap() } as ConcurrentMap<String, T>

    fun getMetaAtomic(name: String?) = getMetaType<TSGlobalMetaAtomic>(MetaType.META_ATOMIC)[name]
    fun getMetaEnum(name: String?) = getMetaType<TSGlobalMetaEnum>(MetaType.META_ENUM)[name]
    fun getMetaMap(name: String?) = getMetaType<TSGlobalMetaMap>(MetaType.META_MAP)[name]
    fun getMetaRelation(name: String?) = getMetaType<TSGlobalMetaRelation>(MetaType.META_RELATION)[name]
    fun getMetaItem(name: String?) = getMetaType<TSGlobalMetaItem>(MetaType.META_ITEM)[name]
    fun getMetaCollection(name: String?) = getMetaType<TSGlobalMetaCollection>(MetaType.META_COLLECTION)[name]

    fun getMetaTypes() = myMetaCache;

    fun getReference(name: String?): TSMetaRelation.TSMetaRelationElement? = name?.let { getReferences()[it] }

    fun getReferences() = myReferencesBySourceTypeName;

    fun addDeployment(deployment: TSMetaDeployment) {
        myDeploymentTables[deployment.table] = deployment
        val typeCode = deployment.typeCode?.toIntOrNull()
        if (typeCode != null) {
            myDeploymentTypeCodes[typeCode] = deployment
        }
    }

}
