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
package com.intellij.idea.plugin.hybris.system.bean.meta

import com.intellij.idea.plugin.hybris.system.bean.meta.model.*
import com.intellij.idea.plugin.hybris.system.bean.meta.model.impl.BSGlobalMetaBeanImpl
import com.intellij.idea.plugin.hybris.system.bean.meta.model.impl.BSGlobalMetaEnumImpl
import com.intellij.util.xml.DomElement

object BSMetaModelMerger {

    fun merge(globalMetaModel: BSGlobalMetaModel, localMetaModels: Collection<BSMetaModel>) = with(globalMetaModel) {
        globalMetaModel.clear()

        localMetaModels
            // ideally, we have to get the same dependency order as SAP Commerce
            .sortedBy { !it.custom }
            .forEach { merge(this, it) }

        val beans = getMetaType<BSGlobalMetaBean>(BSMetaType.META_BEAN)
        val wsBeans = beans.filter { it.value.hints.containsKey("wsRelated") }

        getMetaType<BSGlobalMetaBean>(BSMetaType.META_WS_BEAN).putAll(wsBeans)
        beans.keys.removeAll(wsBeans.keys)

        // after merging all different declarations of the same bean we need to process properties which were declared via extends
        val allBeans = wsBeans.values +
            getMetaType<BSGlobalMetaBean>(BSMetaType.META_EVENT).values +
            getMetaType<BSGlobalMetaBean>(BSMetaType.META_BEAN).values
        allBeans
            .forEach { (it as? BSGlobalMetaBeanSelfMerge<*, *>)?.postMerge(this) }

        wsBeans.values.forEach { it.metaType = BSMetaType.META_WS_BEAN }
        getMetaType<BSGlobalMetaBean>(BSMetaType.META_EVENT).values.forEach { it.metaType = BSMetaType.META_EVENT }
    }

    @Suppress("UNCHECKED_CAST")
    private fun merge(globalMetaModel: BSGlobalMetaModel, localMetaModel: BSMetaModel) {
        localMetaModel.getMetaTypes().forEach { (metaType, localMetas) ->
            run {
                val globalCache = globalMetaModel.getMetaType<BSMetaSelfMerge<out DomElement, out BSMetaClassifier<out DomElement>>>(metaType)

                localMetas.entrySet().forEach { (key, localMetaClassifiers) ->
                    localMetaClassifiers.forEach { localMetaClassifier ->
                        val globalMetaClassifier = globalCache.computeIfAbsent(key) {
                            when (localMetaClassifier) {
                                is BSMetaEnum -> BSGlobalMetaEnumImpl(localMetaClassifier)
                                is BSMetaBean -> BSGlobalMetaBeanImpl(localMetaClassifier)
                                else -> null
                            }
                        }

                        (globalMetaClassifier as BSMetaSelfMerge<DomElement, BSMetaClassifier<DomElement>>).merge(localMetaClassifier)
                    }
                }
            }
        }
    }
}