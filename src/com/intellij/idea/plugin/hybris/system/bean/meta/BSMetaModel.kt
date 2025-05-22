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

import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSMetaClassifier
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSMetaType
import com.intellij.openapi.Disposable
import com.intellij.util.containers.MultiMap
import com.intellij.util.xml.DomElement
import java.util.concurrent.ConcurrentHashMap

class BSMetaModel(
    val extensionName: String,
    val fileName: String,
    val custom: Boolean
) : Disposable {

    private val myMetaCache: MutableMap<BSMetaType, MultiMap<String, BSMetaClassifier<DomElement>>> = ConcurrentHashMap()

    fun addMetaModel(meta: BSMetaClassifier<out DomElement>, metaType: BSMetaType) {
        // add log why no name
        if (meta.name == null) return

        getMetaType<BSMetaClassifier<out DomElement>>(metaType).putValue(meta.name!!.lowercase(), meta)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : BSMetaClassifier<out DomElement>> getMetaType(metaType: BSMetaType): MultiMap<String, T> =
        myMetaCache.computeIfAbsent(metaType) { MultiMap.createLinked() } as MultiMap<String, T>

    fun getMetaTypes() = myMetaCache

    override fun dispose() {
        myMetaCache.clear()
    }

    override fun toString() = "Module: $extensionName | file: $fileName"
}
