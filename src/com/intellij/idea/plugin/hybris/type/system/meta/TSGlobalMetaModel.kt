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

import com.intellij.idea.plugin.hybris.type.system.meta.impl.CaseInsensitive
import com.intellij.util.xml.DomElement
import java.util.concurrent.ConcurrentHashMap

class TSGlobalMetaModel : AbstractTSMetaModel() {

    private val myDeploymentTables = CaseInsensitive.CaseInsensitiveConcurrentHashMap<String, TSMetaDeployment<*>>();
    private val myDeploymentTypeCodes = ConcurrentHashMap<Int, TSMetaDeployment<*>>();

    override fun dispose() {
        myMetaCache.clear()
        myReferencesBySourceTypeName.clear()
        myDeploymentTables.clear()
    }

    fun getDeploymentForTable(table: String?) : TSMetaDeployment<*>? = if (table != null) myDeploymentTables[table] else null
    fun getDeploymentForTypeCode(typeCode: Int?) : TSMetaDeployment<*>? = if (typeCode != null) myDeploymentTypeCodes[typeCode] else null
    fun getDeploymentForTypeCode(typeCode: String?) : TSMetaDeployment<*>? = getDeploymentForTypeCode(typeCode?.toIntOrNull())
    fun getNextAvailableTypeCode(): Int = myDeploymentTypeCodes.keys
        .asSequence()
        .filter { it < 32700 } // OOTB Processing extension
        .filter { it !in 13200 .. 13299 } // OOTB Commons extension
        .filter { it !in 10000 .. 10099 } // OOTB Processing extension
        .filter { it !in 24400 .. 24599 } // OOTB XPrint extension
        .maxOf { it } + 1

    fun merge(metaModels : List<TSMetaModel>): TSGlobalMetaModel {
        metaModels.forEach { merge(it) }

        return this
    }

    private fun merge(another: TSMetaModel) {
        another.getMetaTypes().forEach { (metaType, cache) ->
            run {
                val globalCache = getMetaType<TSMetaClassifier<DomElement?>>(metaType)

                cache.forEach { (key, metaClassifier) ->
                    val globalMetaClassifier = globalCache[key]

                    if (globalMetaClassifier != null
                        && globalMetaClassifier is TSMetaSelfMerge<DomElement?>
                        && metaClassifier is TSMetaSelfMerge<DomElement?>) {
                        globalMetaClassifier.merge(metaClassifier)
                    } else {
                        globalCache[key] = metaClassifier
                    }

                }
            }
        }
        getReferences().putAllValues(another.getReferences());

        another.getMetaType<TSMetaItem>(MetaType.META_ITEM).values
            .filter { it.deployment.table != null && it.deployment.typeCode != null }
            .forEach { mergeDeploymentInformation(it.deployment) }
        another.getMetaType<TSMetaRelation>(MetaType.META_RELATION).values
            .filter { it.deployment.table != null && it.deployment.typeCode != null }
            .forEach { mergeDeploymentInformation(it.deployment) }
    }

    private fun mergeDeploymentInformation(deployment: TSMetaDeployment<*>) {
        myDeploymentTables[deployment.table] = deployment
        val typeCode = deployment.typeCode?.toIntOrNull()
        if (typeCode != null) {
            myDeploymentTypeCodes[typeCode] = deployment
        }
    }
}
