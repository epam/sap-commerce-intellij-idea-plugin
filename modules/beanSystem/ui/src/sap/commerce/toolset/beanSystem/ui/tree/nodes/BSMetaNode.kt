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

package sap.commerce.toolset.beanSystem.ui.tree.nodes

import com.intellij.util.xml.DomElement
import sap.commerce.toolset.beanSystem.meta.model.BSMetaClassifier

@Suppress("UNCHECKED_CAST")
abstract class BSMetaNode<T : BSMetaClassifier<out DomElement>>(parent: BSNode, var meta: T) : BSNode(parent) {

    override fun update(existingNode: BSNode, newNode: BSNode) {
        val current = existingNode as? BSMetaNode<BSMetaClassifier<out DomElement>> ?: return
        val new = newNode as? BSMetaNode<*> ?: return
        current.meta = new.meta
    }
}
