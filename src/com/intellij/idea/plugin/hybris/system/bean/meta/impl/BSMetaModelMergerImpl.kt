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
package com.intellij.idea.plugin.hybris.system.bean.meta.impl

import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils
import com.intellij.idea.plugin.hybris.system.bean.meta.BSGlobalMetaModel
import com.intellij.idea.plugin.hybris.system.bean.meta.BSMetaModel
import com.intellij.idea.plugin.hybris.system.bean.meta.BSMetaModelMerger
import com.intellij.idea.plugin.hybris.system.bean.meta.model.*
import com.intellij.idea.plugin.hybris.system.bean.meta.model.impl.BSGlobalMetaBeanImpl
import com.intellij.idea.plugin.hybris.system.bean.meta.model.impl.BSGlobalMetaEnumImpl
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.util.xml.DomElement

class BSMetaModelMergerImpl(val myProject: Project) : BSMetaModelMerger {

    override fun merge(globalMetaModel: BSGlobalMetaModel, localMetaModels: Collection<BSMetaModel>) = with(globalMetaModel) {
        globalMetaModel.clear()

        ProgressManager.getInstance().progressIndicator.text2 = HybrisI18NBundleUtils.message("hybris.bs.access.progress.subTitle.merging")

        localMetaModels
            // ideally we have to get the same dependency order as SAP Commerce
            .sortedBy { !it.custom }
            .forEach { merge(this, it) }

        val beans = getMetaType<BSGlobalMetaBean>(BSMetaType.META_BEAN)
        val wsBeans = beans.filter { it.value.hints.containsKey("wsRelated") }

        getMetaType<BSGlobalMetaBean>(BSMetaType.META_WS_BEAN).putAll(wsBeans)
        beans.keys.removeAll(wsBeans.keys)

        Unit
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