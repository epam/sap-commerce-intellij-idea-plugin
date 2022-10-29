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

import com.intellij.idea.plugin.hybris.type.system.meta.TSGlobalMetaModel
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaModel
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaModelMerger
import com.intellij.idea.plugin.hybris.type.system.meta.model.*
import com.intellij.idea.plugin.hybris.type.system.meta.model.impl.*
import com.intellij.idea.plugin.hybris.type.system.model.*
import com.intellij.util.xml.DomElement

class TSMetaModelMergerImpl() : TSMetaModelMerger {

    override fun merge(localMetaModels: Collection<TSMetaModel>) = with(TSGlobalMetaModel()) {
        localMetaModels
            // ideally we have to get the same dependency order as SAP Commerce Cloud
            .sortedBy { !it.custom }
            .forEach { merge(this, it) }

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

        globalMetaModel.getReferences().putAll(localMetaModel.getReferences());

        localMetaModel.getMetaType<TSMetaItem>(MetaType.META_ITEM).values()
            .filter { it.deployment.table != null && it.deployment.typeCode != null }
            .forEach { globalMetaModel.addDeployment(it.deployment) }
        localMetaModel.getMetaType<TSMetaRelation>(MetaType.META_RELATION).values()
            .filter { it.deployment.table != null && it.deployment.typeCode != null }
            .forEach { globalMetaModel.addDeployment(it.deployment) }
    }

}