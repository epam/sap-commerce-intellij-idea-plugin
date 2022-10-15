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

import com.intellij.util.xml.DomElement

class TSGlobalMetaModel : AbstractTSMetaModel() {

    override fun dispose() {
        myMetaCache.clear()
        myReferencesBySourceTypeName.clear()
    }

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
    }
}
