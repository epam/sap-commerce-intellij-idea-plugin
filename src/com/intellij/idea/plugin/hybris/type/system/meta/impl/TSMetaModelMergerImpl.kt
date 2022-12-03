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
package com.intellij.idea.plugin.hybris.type.system.meta.impl

import com.intellij.idea.plugin.hybris.type.system.meta.*
import com.intellij.idea.plugin.hybris.type.system.meta.model.*
import com.intellij.idea.plugin.hybris.type.system.meta.model.impl.*
import com.intellij.idea.plugin.hybris.type.system.model.*
import com.intellij.openapi.project.Project
import com.intellij.util.xml.DomElement
import java.util.*

class TSMetaModelMergerImpl(val myProject: Project) : TSMetaModelMerger {

    override fun merge(localMetaModels: Collection<TSMetaModel>) = with(TSGlobalMetaModel()) {
        localMetaModels
            // ideally we have to get the same dependency order as SAP Commerce Cloud
            .sortedBy { !it.custom }
            .forEach { merge(this, it) }

        // after merging all different declarations of the same time we may need to process properties which can be overridden via extends
        getMetaType<TSGlobalMetaItem>(TSMetaType.META_ITEM).values
            .filter { it is TSGlobalMetaItemSelfMerge<*, *>}
            .forEach { (it as TSGlobalMetaItemSelfMerge<*, *>).postMerge(this) }

        this
    }

    @Suppress("UNCHECKED_CAST")
    private fun merge(globalMetaModel: TSGlobalMetaModel, localMetaModel: TSMetaModel) {
        localMetaModel.getMetaTypes().forEach { (metaType, localMetas) ->
            run {
                val globalCache = globalMetaModel.getMetaType<TSMetaSelfMerge<out DomElement, out TSMetaClassifier<out DomElement>>>(metaType)

                localMetas.entrySet().forEach { (key, localMetaClassifiers) ->
                    localMetaClassifiers.forEach { localMetaClassifier ->
                        val globalMetaClassifier = globalCache.computeIfAbsent(key) {
                            when (localMetaClassifier) {
                                is TSMetaAtomic -> TSGlobalMetaAtomicImpl(localMetaClassifier)
                                is TSMetaEnum -> TSGlobalMetaEnumImpl(localMetaClassifier)
                                is TSMetaCollection -> TSGlobalMetaCollectionImpl(localMetaClassifier)
                                is TSMetaMap -> TSGlobalMetaMapImpl(localMetaClassifier)
                                is TSMetaRelation -> TSGlobalMetaRelationImpl(localMetaClassifier)
                                is TSMetaItem -> TSGlobalMetaItemImpl(localMetaClassifier)
                                else -> null
                            }
                        }

                        (globalMetaClassifier as TSMetaSelfMerge<DomElement, TSMetaClassifier<DomElement>>).merge(localMetaClassifier)
                    }
                }
            }
        }

        globalMetaModel.getAllRelations().putAllValues(localMetaModel.getRelations());

        val itemTypeDeployments = localMetaModel.getMetaType<TSMetaItem>(TSMetaType.META_ITEM).values()
            .map { it.deployment }
        val relationDeployments = localMetaModel.getMetaType<TSMetaRelation>(TSMetaType.META_RELATION).values()
            .map { it.deployment }
        (itemTypeDeployments + relationDeployments)
            .filter { it.table != null && it.typeCode != null }
            .forEach { globalMetaModel.addDeployment(it) }
    }

}