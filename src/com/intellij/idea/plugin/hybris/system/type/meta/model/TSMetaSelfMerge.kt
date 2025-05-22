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
package com.intellij.idea.plugin.hybris.system.type.meta.model

import com.intellij.idea.plugin.hybris.system.type.meta.TSGlobalMetaModel
import com.intellij.util.xml.DomElement

abstract class TSMetaSelfMerge<DOM : DomElement, T : TSMetaClassifier<DOM>>(localMeta: T) : TSGlobalMetaClassifier<DOM> {

    override val name = localMeta.name
    override var isCustom = localMeta.isCustom
    override val declarations: MutableSet<T> = HashSet()
    var mergeConflicts: MutableList<String> = ArrayList()

    fun merge(localMeta: T) {
        declarations.add(localMeta)

        if (localMeta.isCustom) isCustom = true

        mergeInternally(localMeta)
    }

    protected abstract fun mergeInternally(localMeta: T)
}

abstract class TSGlobalMetaItemSelfMerge<DOM : DomElement, T : TSMetaClassifier<DOM>>(localMeta: T) : TSMetaSelfMerge<DOM, T>(localMeta) {

    abstract fun postMerge(globalMetaModel: TSGlobalMetaModel)

}
