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

package sap.commerce.toolset.typeSystem.ui.tree.nodes

import com.intellij.ide.projectView.PresentationData
import com.intellij.openapi.project.Project
import com.intellij.ui.SimpleTextAttributes
import com.intellij.util.xml.DomElement
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.i18n
import sap.commerce.toolset.typeSystem.meta.model.*
import sap.commerce.toolset.typeSystem.settings.TSViewSettings

class TSMetaTypeNode(parent: TSNode, private val metaType: TSMetaType) : TSNode(parent) {

    override fun getName() = i18n("hybris.toolwindow.ts.group.${metaType.name.lowercase()}.name")

    @Suppress("UNCHECKED_CAST")
    override fun update(existingNode: TSNode, newNode: TSNode) {
        val current = existingNode as? TSMetaNode<TSMetaClassifier<out DomElement>> ?: return
        val new = newNode as? TSMetaNode<*> ?: return
        current.meta = new.meta
    }

    override fun update(project: Project, presentation: PresentationData) {
        when (metaType) {
            TSMetaType.META_ATOMIC -> presentation.setIcon(HybrisIcons.TypeSystem.GROUP_ATOMIC)
            TSMetaType.META_ITEM -> presentation.setIcon(HybrisIcons.TypeSystem.GROUP_ITEM)
            TSMetaType.META_ENUM -> presentation.setIcon(HybrisIcons.TypeSystem.GROUP_ENUM)
            TSMetaType.META_COLLECTION -> presentation.setIcon(HybrisIcons.TypeSystem.GROUP_COLLECTION)
            TSMetaType.META_MAP -> presentation.setIcon(HybrisIcons.TypeSystem.GROUP_MAP)
            TSMetaType.META_RELATION -> presentation.setIcon(HybrisIcons.TypeSystem.GROUP_RELATION)
        }
        presentation.addText(name, SimpleTextAttributes.REGULAR_ATTRIBUTES)

        val showOnlyCustom = TSViewSettings.getInstance(myProject).showOnlyCustom
        val entries = globalMetaModel
            ?.getMetaType<TSGlobalMetaClassifier<DomElement>>(metaType)
            ?.values
            ?.filter { if (showOnlyCustom) it.isCustom else true }
            ?.size
            ?: 0
        if (entries > 0) {
            presentation.locationString = "$entries"
        }
    }

    override fun getNewChildren(): Map<String, TSNode> {
        val settings = TSViewSettings.getInstance(myProject)

        if (metaType == TSMetaType.META_ITEM) {
            TSMetaItemNode.groupedByExtends = emptyMap()

            if (settings.groupItemByParent) {
                return getGroupedByParentMetaItemChildren(settings)
            }
        }
        return getChildren(metaType, settings)
    }

    private fun getGroupedByParentMetaItemChildren(settings: TSViewSettings): Map<String, TSNode> {
        if (!settings.showMetaItems) return emptyMap()

        val items = globalMetaModel
            ?.getMetaType<TSGlobalMetaItem>(TSMetaType.META_ITEM)
            ?.values
            ?.filter { if (settings.showOnlyCustom) it.isCustom else true }
            ?: emptyList()

        val groupedByName = items.associateBy { it.name }
        val groupedByExtends = items
            .filterNot { it.name == HybrisConstants.TS_TYPE_GENERIC_ITEM }
            .groupBy {
                if (groupedByName.containsKey(it.extendedMetaItemName)) it.extendedMetaItemName else HybrisConstants.TS_TYPE_GENERIC_ITEM
            }

        // we always have to have "Item" element
        TSMetaItemNode.groupedByExtends = groupedByExtends
        return (groupedByExtends[HybrisConstants.TS_TYPE_GENERIC_ITEM] ?: emptyList())
            .map { TSMetaItemNode(this, it) }
            .associateBy { it.name }
    }

    private fun getChildren(metaType: TSMetaType, settings: TSViewSettings): Map<String, TSNode> = globalMetaModel
        ?.getMetaType<TSGlobalMetaClassifier<DomElement>>(metaType)
        ?.values
        ?.filter { if (settings.showOnlyCustom) it.isCustom else true }
        ?.mapNotNull {
            when (it) {
                is TSGlobalMetaItem -> if (settings.showMetaItems) TSMetaItemNode(this, it) else null
                is TSGlobalMetaEnum -> if (settings.showMetaEnums) TSMetaEnumNode(this, it) else null
                is TSGlobalMetaRelation -> if (settings.showMetaRelations) TSMetaRelationNode(this, it) else null
                is TSGlobalMetaCollection -> if (settings.showMetaCollections) TSMetaCollectionNode(this, it) else null
                is TSGlobalMetaAtomic -> if (settings.showMetaAtomics) TSMetaAtomicNode(this, it) else null
                is TSGlobalMetaMap -> if (settings.showMetaMaps) TSMetaMapNode(this, it) else null
                else -> null
            }
        }
        ?.associateBy { it.name }
        ?: emptyMap()

}