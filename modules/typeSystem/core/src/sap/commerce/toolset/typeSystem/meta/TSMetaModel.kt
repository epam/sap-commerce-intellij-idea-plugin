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
package sap.commerce.toolset.typeSystem.meta

import com.intellij.openapi.Disposable
import com.intellij.util.containers.MultiMap
import com.intellij.util.xml.DomElement
import sap.commerce.toolset.CaseInsensitiveMap
import sap.commerce.toolset.typeSystem.meta.model.TSMetaClassifier
import sap.commerce.toolset.typeSystem.meta.model.TSMetaRelation
import sap.commerce.toolset.typeSystem.meta.model.TSMetaType
import java.util.concurrent.ConcurrentHashMap

class TSMetaModel(
    val extensionName: String,
    val fileName: String,
    val custom: Boolean
) : Disposable {

    private val myMetaCache: MutableMap<TSMetaType, MultiMap<String, TSMetaClassifier<DomElement>>> = ConcurrentHashMap()
    private val myRelationsBySourceTypeName = CaseInsensitiveMap.NoCaseMultiMap<TSMetaRelation.TSMetaRelationElement>()

    fun addMetaModel(meta: TSMetaClassifier<out DomElement>, metaType: TSMetaType) {
        // add log why no name
        if (meta.name == null) return

        getMetaType<TSMetaClassifier<out DomElement>>(metaType).putValue(meta.name!!.lowercase(), meta)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : TSMetaClassifier<out DomElement>> getMetaType(metaType: TSMetaType): MultiMap<String, T> =
        myMetaCache.computeIfAbsent(metaType) { MultiMap.createLinked() } as MultiMap<String, T>

    fun getMetaTypes() = myMetaCache
    fun getRelations() = myRelationsBySourceTypeName

    override fun dispose() {
        myMetaCache.clear()
        myRelationsBySourceTypeName.clear()
    }

    override fun toString() = "Module: $extensionName | file: $fileName"
}
