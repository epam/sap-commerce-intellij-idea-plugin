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
package com.intellij.idea.plugin.hybris.beans.meta

import com.intellij.idea.plugin.hybris.beans.meta.model.BeansMetaClassifier
import com.intellij.idea.plugin.hybris.beans.meta.model.BeansMetaType
import com.intellij.openapi.Disposable
import com.intellij.openapi.module.Module
import com.intellij.psi.PsiFile
import com.intellij.util.containers.MultiMap
import com.intellij.util.xml.DomElement
import java.util.concurrent.ConcurrentHashMap

class BeansMetaModel(
    val module: Module,
    val psiFile: PsiFile,
    val custom: Boolean
) : Disposable {

    private val myMetaCache: MutableMap<BeansMetaType, MultiMap<String, BeansMetaClassifier<DomElement>>> = ConcurrentHashMap()

    fun addMetaModel(meta: BeansMetaClassifier<out DomElement>, metaType: BeansMetaType) {
        // add log why no name
        if (meta.name == null) return

        getMetaType<BeansMetaClassifier<out DomElement>>(metaType).putValue(meta.name!!.lowercase(), meta)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : BeansMetaClassifier<out DomElement>> getMetaType(metaType: BeansMetaType): MultiMap<String, T> =
        myMetaCache.computeIfAbsent(metaType) { MultiMap.createLinked() } as MultiMap<String, T>

    fun getMetaTypes() = myMetaCache;

    override fun dispose() {
        myMetaCache.clear()
    }

    override fun toString() = "Module: ${module.name} | psi file: ${psiFile.name}"
}
