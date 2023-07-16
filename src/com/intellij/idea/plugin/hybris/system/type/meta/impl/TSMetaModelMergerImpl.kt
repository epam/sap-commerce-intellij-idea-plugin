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
package com.intellij.idea.plugin.hybris.system.type.meta.impl

import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.system.type.meta.TSGlobalMetaModel
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaHelper
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModel
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelMerger
import com.intellij.idea.plugin.hybris.system.type.meta.model.*
import com.intellij.idea.plugin.hybris.system.type.meta.model.impl.*
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.util.xml.DomElement

class TSMetaModelMergerImpl(val myProject: Project) : TSMetaModelMerger {

    override fun merge(globalMetaModel: TSGlobalMetaModel, localMetaModels: Collection<TSMetaModel>) = with(globalMetaModel) {
        globalMetaModel.clear()

        ProgressManager.getInstance().progressIndicator.text2 = message("hybris.ts.access.progress.subTitle.merging")

        localMetaModels
            // ideally we have to get the same dependency order as SAP Commerce
            .sortedBy { !it.custom }
            .forEach { merge(this, it) }

        val allTypes = getMetaTypes().values
            .flatMap { it.values }
            .filter { it.name != null }
            .filter { it is TSTypedClassifier }
            .associate { it.name!! to (it as TSTypedClassifier) }

        // after merging all different declarations of the same time we may need to process properties which can be overridden via extends
        getMetaType<TSGlobalMetaItem>(TSMetaType.META_ITEM).values
            .forEach { (it as? TSGlobalMetaItemSelfMerge<*, *>)?.postMerge(this) }

        getMetaType<TSGlobalMetaItem>(TSMetaType.META_ITEM).values
            .flatMap { it.allAttributes.values }
            .filter { it.type != null }
            .forEach { it.flattenType = TSMetaHelper.flattenType(it.type!!, allTypes) }

        // to properly propagate `isCustom` flag we need to check every relation end defined for non directly modified Item Types
        // if at least one relation end is custom Item Type will be marked as custom too
        getMetaType<TSGlobalMetaItem>(TSMetaType.META_ITEM).values
            .filterNot { it.isCustom }
            .filter { it.allRelationEnds.any { relationEnd -> relationEnd.isCustom } }
            .forEach { it.isCustom = true }

        getMetaType<TSGlobalMetaRelation>(TSMetaType.META_RELATION).values
            .forEach {
                it.source.flattenType = TSMetaHelper.flattenType(TSMetaHelper.flattenType(it.source), allTypes)
                it.target.flattenType = TSMetaHelper.flattenType(TSMetaHelper.flattenType(it.target), allTypes)
            }
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
            .mapNotNull { it.deployment }
        val relationDeployments = localMetaModel.getMetaType<TSMetaRelation>(TSMetaType.META_RELATION).values()
            .mapNotNull { it.deployment }
        (itemTypeDeployments + relationDeployments)
            .filter { it.table != null && it.typeCode != null }
            .forEach { globalMetaModel.addDeployment(it) }
    }

}