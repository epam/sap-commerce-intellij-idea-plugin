/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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

import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.system.type.model.CollectionType
import com.intellij.idea.plugin.hybris.system.type.model.Type
import javax.swing.Icon

interface TSMetaCollection : TSMetaClassifier<CollectionType> {

    val type: Type
    val elementType: String
    val isAutoCreate: Boolean
    val isGenerate: Boolean
}

interface TSGlobalMetaCollection : TSMetaCollection, TSGlobalMetaClassifier<CollectionType>, TSTypedClassifier {
    override val declarations: MutableSet<TSMetaCollection>
    override val icon: Icon
        get() = HybrisIcons.TypeSystem.Types.COLLECTION
}
