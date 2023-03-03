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
package com.intellij.idea.plugin.hybris.system.type.meta

import com.intellij.idea.plugin.hybris.system.type.meta.impl.CaseInsensitive
import com.intellij.idea.plugin.hybris.system.type.meta.model.*
import com.intellij.openapi.Disposable
import com.intellij.openapi.util.ModificationTracker
import com.intellij.util.xml.DomElement
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class TSGlobalMetaModel : ModificationTracker, Disposable {

    private var modificationTracker = 0L
    private val myMetaCache: MutableMap<TSMetaType, Map<String, TSGlobalMetaClassifier<out DomElement>>> = ConcurrentHashMap()
    private val myReferencesBySourceTypeName = CaseInsensitive.NoCaseMultiMap<TSMetaRelation.TSMetaRelationElement>()
    private val myDeploymentTables = CaseInsensitive.CaseInsensitiveConcurrentHashMap<String, TSMetaDeployment>();
    private val myDeploymentTypeCodes = ConcurrentHashMap<Int, TSMetaDeployment>();

    fun clear() {
        cleanup()

        if (modificationTracker == Long.MAX_VALUE) modificationTracker = 0L
        modificationTracker++
    }

    fun getDeploymentForTable(table: String?): TSMetaDeployment? = if (table != null) myDeploymentTables[table] else null
    fun getDeploymentForTypeCode(typeCode: Int?): TSMetaDeployment? = if (typeCode != null) myDeploymentTypeCodes[typeCode] else null
    fun getDeploymentForTypeCode(typeCode: String?): TSMetaDeployment? = getDeploymentForTypeCode(typeCode?.toIntOrNull())

    @Suppress("UNCHECKED_CAST")
    fun <T : TSGlobalMetaClassifier<*>> getMetaType(metaType: TSMetaType): ConcurrentMap<String, T> =
        myMetaCache.computeIfAbsent(metaType) { CaseInsensitive.CaseInsensitiveConcurrentHashMap() } as ConcurrentMap<String, T>

    fun getMetaAtomic(name: String?) = getMetaType<TSGlobalMetaAtomic>(TSMetaType.META_ATOMIC)[name]
    fun getMetaEnum(name: String?) = getMetaType<TSGlobalMetaEnum>(TSMetaType.META_ENUM)[name]
    fun getMetaMap(name: String?) = getMetaType<TSGlobalMetaMap>(TSMetaType.META_MAP)[name]
    fun getMetaRelation(name: String?) = getMetaType<TSGlobalMetaRelation>(TSMetaType.META_RELATION)[name]
    fun getMetaItem(name: String?) = getMetaType<TSGlobalMetaItem>(TSMetaType.META_ITEM)[name]
    fun getMetaCollection(name: String?) = getMetaType<TSGlobalMetaCollection>(TSMetaType.META_COLLECTION)[name]

    fun getMetaTypes() = myMetaCache;

    fun getRelations(name: String?): Collection<TSMetaRelation.TSMetaRelationElement>? = name?.let { getAllRelations()[it] }

    fun getAllRelations() = myReferencesBySourceTypeName;
    fun getDeploymentTypeCodes() = myDeploymentTypeCodes

    fun addDeployment(deployment: TSMetaDeployment) {
        myDeploymentTables[deployment.table] = deployment
        val typeCode = deployment.typeCode?.toIntOrNull()
        if (typeCode != null) {
            myDeploymentTypeCodes[typeCode] = deployment
        }
    }

    override fun getModificationCount() = modificationTracker
    override fun dispose() = cleanup()

    @Synchronized
    private fun cleanup() {
        myMetaCache.clear()
        myReferencesBySourceTypeName.clear()
        myDeploymentTables.clear()
        myDeploymentTypeCodes.clear()
    }
}
