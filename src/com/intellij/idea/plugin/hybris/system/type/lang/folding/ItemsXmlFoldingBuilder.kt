/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2023 EPAM Systems <hybrisideaplugin@epam.com>
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
package com.intellij.idea.plugin.hybris.system.type.lang.folding

import ai.grazie.utils.toDistinctTypedArray
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent
import com.intellij.idea.plugin.hybris.system.type.model.*
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.SyntaxTraverser
import com.intellij.psi.util.PsiElementFilter
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.util.xml.DomManager

class ItemsXmlFoldingBuilder : FoldingBuilderEx(), DumbAware {

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        if (!HybrisProjectSettingsComponent.getInstance(root.project).isHybrisProject()) return emptyArray()
        if (root !is XmlFile) return emptyArray()
        DomManager.getDomManager(root.project).getFileElement(root, Items::class.java)
            ?: return emptyArray()

        return SyntaxTraverser.psiTraverser(root)
            .filter { ItemsXmlFilter().isAccepted(it) }
            .mapNotNull {
                if (it is PsiErrorElement || it.textRange.isEmpty) return@mapNotNull null
                FoldingDescriptor(it.node, it.textRange, FoldingGroup.newGroup(GROUP_NAME))
            }
            .toDistinctTypedArray()
    }

    override fun getPlaceholderText(node: ASTNode) = when (val psi = node.psi) {
        is XmlTag -> when (psi.localName) {
            EnumTypes.ENUMTYPE -> psi.getAttributeValue(EnumType.CODE)
            Relations.RELATION -> psi.getAttributeValue(Relation.CODE)
            Indexes.INDEX -> psi.getAttributeValue(Index.NAME)
            CustomProperties.PROPERTY -> psi.getAttributeValue(CustomProperty.NAME)

            AtomicTypes.ATOMICTYPE -> psi.getAttributeValue(AtomicType.CLASS) +
                (psi.getAttributeValue(AtomicType.EXTENDS)?.let { " : $it" } ?: "")

            MapTypes.MAPTYPE -> psi.getAttributeValue(MapType.CODE) + " : " +
                psi.getAttributeValue(MapType.ARGUMENTTYPE) + " <-> " +
                psi.getAttributeValue(MapType.RETURNTYPE)

            CollectionTypes.COLLECTIONTYPE -> psi.getAttributeValue(CollectionType.CODE) + " : " +
                psi.getAttributeValue(CollectionType.ELEMENTTYPE) + " : " +
                (psi.getAttributeValue(CollectionType.TYPE) ?: Type.COLLECTION.name)

            Relation.SOURCE_ELEMENT,
            Relation.TARGET_ELEMENT -> (psi.getAttributeValue(RelationElement.CARDINALITY) ?: Cardinality.MANY.name) + " : " +
                (psi.getAttributeValue(RelationElement.TYPE) ?: "") +
                (psi.getAttributeValue(RelationElement.QUALIFIER)?.let { " : $it" } ?: "")

            ItemTypes.ITEMTYPE -> psi.getAttributeValue(ItemType.CODE) + " : " + (psi.getAttributeValue(ItemType.EXTENDS)
                ?: HybrisConstants.TS_TYPE_GENERIC_ITEM)

            ItemType.DEPLOYMENT -> "DB [" + psi.getAttributeValue(Deployment.TYPE_CODE) + " : " + psi.getAttributeValue(Deployment.TABLE) + "]"

            Attributes.ATTRIBUTE -> psi.getAttributeValue(Attribute.QUALIFIER) + " : " + psi.getAttributeValue(Attribute.TYPE)

            else -> FALLBACK_PLACEHOLDER
        }

        else -> FALLBACK_PLACEHOLDER
    }

    override fun isCollapsedByDefault(node: ASTNode) = when (val psi = node.psi) {
        is XmlTag -> when (psi.localName) {
            MapTypes.MAPTYPE,
            AtomicTypes.ATOMICTYPE,
            CollectionTypes.COLLECTIONTYPE,
            ItemType.DEPLOYMENT,
            Attributes.ATTRIBUTE,
            CustomProperties.PROPERTY,
            Relation.SOURCE_ELEMENT,
            Relation.TARGET_ELEMENT,
            Indexes.INDEX -> true

            else -> false
        }

        else -> false
    }

    class ItemsXmlFilter : PsiElementFilter {

        override fun isAccepted(element: PsiElement) = when (element) {
            is XmlTag -> when (element.localName) {
                CustomProperties.PROPERTY,
                MapTypes.MAPTYPE,
                AtomicTypes.ATOMICTYPE,
                EnumTypes.ENUMTYPE,
                CollectionTypes.COLLECTIONTYPE,
                Relations.RELATION,
                Relation.SOURCE_ELEMENT,
                Relation.TARGET_ELEMENT,
                ItemTypes.ITEMTYPE,
                ItemType.DEPLOYMENT,
                Attributes.ATTRIBUTE,
                Indexes.INDEX -> true

                else -> false
            }

            else -> false
        }
    }

    companion object {
        private const val GROUP_NAME = "ItemsXml"
        private const val FALLBACK_PLACEHOLDER = "..."
    }

}
