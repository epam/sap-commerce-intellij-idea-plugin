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

import com.intellij.ide.projectView.PresentationData
import com.intellij.openapi.project.Project
import com.intellij.ui.SimpleTextAttributes
import com.intellij.util.xml.DomElement
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.beanSystem.meta.BSMetaHelper
import sap.commerce.toolset.beanSystem.meta.model.*
import sap.commerce.toolset.beanSystem.settings.BSViewSettings
import sap.commerce.toolset.i18n

@Suppress("UNCHECKED_CAST")
class BSMetaTypeNode(parent: BSNode, private val metaType: BSMetaType) : BSNode(parent) {

    override fun getName() = i18n("hybris.toolwindow.beans.group.${metaType.name.lowercase()}.name")

    override fun update(existingNode: BSNode, newNode: BSNode) {
        val current = existingNode as? BSMetaNode<BSMetaClassifier<out DomElement>> ?: return
        val new = newNode as? BSMetaNode<*> ?: return
        current.meta = new.meta
    }

    override fun update(project: Project, presentation: PresentationData) {
        when (metaType) {
            BSMetaType.META_ENUM -> presentation.setIcon(HybrisIcons.BeanSystem.GROUP_BY_ENUM)
            BSMetaType.META_BEAN -> presentation.setIcon(HybrisIcons.BeanSystem.GROUP_BY_BEAN_DTO)
            BSMetaType.META_WS_BEAN -> presentation.setIcon(HybrisIcons.BeanSystem.GROUP_BY_BEAN_WS)
            BSMetaType.META_EVENT -> presentation.setIcon(HybrisIcons.BeanSystem.GROUP_BY_BEAN_EVENT)
        }
        presentation.addText(name, SimpleTextAttributes.REGULAR_ATTRIBUTES)

        val settings = BSViewSettings.getInstance(myProject)
        val entries = globalMetaModel
            ?.getMetaType<BSGlobalMetaClassifier<DomElement>>(metaType)
            ?.values
            ?.filter { if (settings.showCustomOnly) it.isCustom else true }
            ?.filter { if (settings.showDeprecatedOnly) BSMetaHelper.isDeprecated(it) else true }
            ?.size
            ?: 0
        if (entries > 0) {
            presentation.locationString = "$entries"
        }
    }

    override fun getNewChildren(): Map<String, BSNode> {
        val settings = BSViewSettings.getInstance(myProject)

        return globalMetaModel
            ?.getMetaType<BSGlobalMetaClassifier<DomElement>>(metaType)
            ?.values
            ?.filter { if (settings.showCustomOnly) it.isCustom else true }
            ?.filter { if (settings.showDeprecatedOnly) BSMetaHelper.isDeprecated(it) else true }
            ?.mapNotNull {
                when (it) {
                    is BSGlobalMetaEnum -> BSMetaEnumNode(this, it)
                    is BSGlobalMetaBean -> BSMetaBeanNode(this, it)
                    else -> null
                }
            }
            ?.associateBy { it.name }
            ?: emptyMap()
    }

}