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
package com.intellij.idea.plugin.hybris.system.bean.meta

import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSGlobalMetaClassifier
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSGlobalMetaEnum
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSMetaType
import com.intellij.idea.plugin.hybris.type.system.meta.impl.CaseInsensitive
import com.intellij.openapi.Disposable
import com.intellij.util.xml.DomElement
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class BSGlobalMetaModel : Disposable {

    private val myMetaCache: MutableMap<BSMetaType, Map<String, BSGlobalMetaClassifier<out DomElement>>> = ConcurrentHashMap()

    override fun dispose() {
        myMetaCache.clear()
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : BSGlobalMetaClassifier<*>> getMetaType(metaType: BSMetaType): ConcurrentMap<String, T> =
        myMetaCache.computeIfAbsent(metaType) { CaseInsensitive.CaseInsensitiveConcurrentHashMap() } as ConcurrentMap<String, T>

    fun getMetaEnum(name: String?) = getMetaType<BSGlobalMetaEnum>(BSMetaType.META_ENUM)[name]

    fun getMetaTypes() = myMetaCache;

}
