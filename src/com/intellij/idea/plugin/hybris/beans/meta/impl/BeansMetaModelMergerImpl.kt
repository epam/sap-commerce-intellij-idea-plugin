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
package com.intellij.idea.plugin.hybris.beans.meta.impl

import com.intellij.idea.plugin.hybris.beans.meta.BeansGlobalMetaModel
import com.intellij.idea.plugin.hybris.beans.meta.BeansMetaModel
import com.intellij.idea.plugin.hybris.beans.meta.BeansMetaModelMerger
import com.intellij.idea.plugin.hybris.beans.meta.model.BeansMetaClassifier
import com.intellij.idea.plugin.hybris.beans.meta.model.BeansMetaEnum
import com.intellij.idea.plugin.hybris.beans.meta.model.BeansMetaSelfMerge
import com.intellij.idea.plugin.hybris.beans.meta.model.impl.BeansGlobalMetaEnumImpl
import com.intellij.openapi.project.Project
import com.intellij.util.xml.DomElement
import java.util.*

class BeansMetaModelMergerImpl(val myProject: Project) : BeansMetaModelMerger {

    override fun merge(localMetaModels: Collection<BeansMetaModel>) = with(BeansGlobalMetaModel()) {
        localMetaModels
            // ideally we have to get the same dependency order as SAP Commerce Cloud
            .sortedBy { !it.custom }
            .forEach { merge(this, it) }

        this
    }

    @Suppress("UNCHECKED_CAST")
    private fun merge(globalMetaModel: BeansGlobalMetaModel, localMetaModel: BeansMetaModel) {
        localMetaModel.getMetaTypes().forEach { (metaType, localMetas) ->
            run {
                val globalCache = globalMetaModel.getMetaType<BeansMetaSelfMerge<out DomElement, out BeansMetaClassifier<out DomElement>>>(metaType)

                localMetas.entrySet().forEach { (key, localMetaClassifiers) ->
                    localMetaClassifiers.forEach { localMetaClassifier ->
                        val globalMetaClassifier = globalCache.computeIfAbsent(key) {
                            when (localMetaClassifier) {
                                is BeansMetaEnum -> BeansGlobalMetaEnumImpl(localMetaClassifier)
                                else -> null
                            }
                        }

                        (globalMetaClassifier as BeansMetaSelfMerge<DomElement, BeansMetaClassifier<DomElement>>).merge(localMetaClassifier)
                    }
                }
            }
        }
    }

}