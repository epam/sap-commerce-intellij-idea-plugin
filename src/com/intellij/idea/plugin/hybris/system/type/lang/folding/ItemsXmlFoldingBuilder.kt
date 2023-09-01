/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
import com.intellij.idea.plugin.hybris.settings.HybrisDeveloperSpecificProjectSettingsComponent
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent
import com.intellij.idea.plugin.hybris.system.type.model.*
import com.intellij.idea.plugin.hybris.system.type.settings.TypeSystemFoldingSettings
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.removeUserData
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.SyntaxTraverser
import com.intellij.psi.util.PsiElementFilter
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.util.xml.DomManager
import org.jetbrains.kotlin.psi.psiUtil.getChildOfType
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType

class ItemsXmlFoldingBuilder : FoldingBuilderEx(), DumbAware {

    // it can be: EnumValue, ColumnType, but not CustomProperty
    private val valueName = "value"

    private val filter = PsiElementFilter {
        when (it) {
            is XmlTag -> when (it.localName) {
                CustomProperties.PROPERTY,
                MapTypes.MAPTYPE,
                AtomicTypes.ATOMICTYPE,
                EnumTypes.ENUMTYPE,
                Persistence.COLUMNTYPE,
                CollectionTypes.COLLECTIONTYPE,
                Relations.RELATION,
                Relation.SOURCE_ELEMENT,
                Relation.TARGET_ELEMENT,
                ItemTypes.ITEMTYPE,
                ItemType.DEPLOYMENT,
                ItemType.DESCRIPTION,
                Attributes.ATTRIBUTE,
                Indexes.INDEX -> true

                valueName -> it.parentOfType<XmlTag>()
                    ?.takeIf { parent -> parent.localName == EnumTypes.ENUMTYPE || parent.localName == Persistence.COLUMNTYPE } != null

                else -> false
            }

            else -> false
        }
    }
    private val cachedFoldingSettings: Key<TypeSystemFoldingSettings> = Key.create("hybris_folding_settings")

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        if (!HybrisProjectSettingsComponent.getInstance(root.project).isHybrisProject()) return emptyArray()
        if (root !is XmlFile) return emptyArray()
        DomManager.getDomManager(root.project).getFileElement(root, Items::class.java)
            ?: return emptyArray()
        val foldingSettings = HybrisDeveloperSpecificProjectSettingsComponent.getInstance(root.project).state.typeSystemSettings.folding

        if (!foldingSettings.enabled) {
            root.removeUserData(cachedFoldingSettings)
            return emptyArray()
        }

        root.putUserData(cachedFoldingSettings, foldingSettings)

        return SyntaxTraverser.psiTraverser(root)
            .filter { filter.isAccepted(it) }
            .mapNotNull {
                if (it is PsiErrorElement || it.textRange.isEmpty) return@mapNotNull null
                FoldingDescriptor(it.node, it.textRange, FoldingGroup.newGroup(GROUP_NAME))
            }
            .toDistinctTypedArray()
    }

    override fun getPlaceholderText(node: ASTNode) = when (val psi = node.psi) {
        is XmlTag -> when (psi.localName) {
            valueName -> {
                when (psi.parentOfType<XmlTag>()?.localName) {
                    EnumTypes.ENUMTYPE -> psi.getAttributeValue(EnumValue.CODE) +
                        (psi.getSubTagText(EnumValue.DESCRIPTION)?.let { " - $it" } ?: "")

                    Persistence.COLUMNTYPE -> psi.value.trimmedText
                    else -> FALLBACK_PLACEHOLDER
                }
            }

            EnumTypes.ENUMTYPE -> psi.getAttributeValue(EnumType.CODE)

            Persistence.COLUMNTYPE -> "[type] " + (psi.getAttributeValue(ColumnType.DATABASE)?.let { "$it : " } ?: "") + (psi.getChildOfType<XmlTag>()
                ?.value
                ?.trimmedText
                ?: "")

            Relations.RELATION -> {
                val code = psi.getAttributeValue(Relation.CODE) ?: "?"
                val source = psi.findFirstSubTag(Relation.SOURCE_ELEMENT)
                val target = psi.findFirstSubTag(Relation.TARGET_ELEMENT)
                val sourceType = source?.getAttributeValue(RelationElement.TYPE) ?: "?"
                val targetType = target?.getAttributeValue(RelationElement.TYPE) ?: "?"

                val sourceRelation = source?.getAttributeValue(RelationElement.CARDINALITY) ?: Cardinality.MANY.value
                val targetRelation = source?.getAttributeValue(RelationElement.CARDINALITY) ?: Cardinality.MANY.value

                "$code ($sourceType [$sourceRelation :: $targetRelation] $targetType)"
            }

            Indexes.INDEX -> (psi.getAttributeValue(Index.NAME)
                ?.let { tablify(psi, it, getCurrentFoldingSettings(psi)?.tablifyItemIndexes, Indexes.INDEX, Index.NAME) }
                ?: FALLBACK_PLACEHOLDER) + psi.childrenOfType<XmlTag>()
                .mapNotNull { it.getAttributeValue(IndexKey.ATTRIBUTE) }
                .joinToString(", ", " : ")

            CustomProperties.PROPERTY -> (psi.getAttributeValue(CustomProperty.NAME)
                ?.let { tablify(psi, it, getCurrentFoldingSettings(psi)?.tablifyItemCustomProperties, CustomProperties.PROPERTY, CustomProperty.NAME) }
                ?: FALLBACK_PLACEHOLDER) +
                (psi.getChildOfType<XmlTag>()
                    ?.value
                    ?.trimmedText
                    ?.let { " : " + if (it.length > 50) it.substring(0, 50) + "..." else it }
                    ?: "")

            AtomicTypes.ATOMICTYPE -> psi.getAttributeValue(AtomicType.CLASS)
                ?.let { tablify(psi, it, getCurrentFoldingSettings(psi)?.tablifyAtomics, AtomicTypes.ATOMICTYPE, AtomicType.CLASS) } +
                (psi.getAttributeValue(AtomicType.EXTENDS)?.let { " : $it" } ?: "")

            MapTypes.MAPTYPE -> psi.getAttributeValue(MapType.CODE)
                ?.let { tablify(psi, it, getCurrentFoldingSettings(psi)?.tablifyMaps, MapTypes.MAPTYPE, MapType.CODE) } +
                " : " + psi.getAttributeValue(MapType.ARGUMENTTYPE)
                ?.let { tablify(psi, it, getCurrentFoldingSettings(psi)?.tablifyMaps, MapTypes.MAPTYPE, MapType.ARGUMENTTYPE, prepend = true) } +
                " <-> " + psi.getAttributeValue(MapType.RETURNTYPE)

            CollectionTypes.COLLECTIONTYPE -> psi.getAttributeValue(CollectionType.CODE)
                ?.let { tablify(psi, it, getCurrentFoldingSettings(psi)?.tablifyCollections, CollectionTypes.COLLECTIONTYPE, CollectionType.CODE) } +
                " : " +
                psi.getAttributeValue(CollectionType.ELEMENTTYPE)
                    ?.let { tablify(psi, it, getCurrentFoldingSettings(psi)?.tablifyCollections, CollectionTypes.COLLECTIONTYPE, CollectionType.ELEMENTTYPE) } +
                " : " + (psi.getAttributeValue(CollectionType.TYPE)
                ?: Type.COLLECTION.value)

            Relation.SOURCE_ELEMENT,
            Relation.TARGET_ELEMENT -> (psi.getAttributeValue(RelationElement.CARDINALITY) ?: Cardinality.MANY.value)
                .let { if (it.length == Cardinality.ONE.value.length) "$it " else it } + " : " +
                tablifyRelationElement(psi) +
                (psi.getAttributeValue(RelationElement.QUALIFIER)?.let { " : $it" } ?: "")

            ItemTypes.ITEMTYPE -> psi.getAttributeValue(ItemType.CODE) +
                " : " + (psi.getAttributeValue(ItemType.EXTENDS)
                ?: HybrisConstants.TS_TYPE_GENERIC_ITEM)

            ItemType.DEPLOYMENT -> "DB [" + psi.getAttributeValue(Deployment.TYPE_CODE) + " : " + psi.getAttributeValue(Deployment.TABLE) + "]"
            ItemType.DESCRIPTION -> "-- ${psi.value.trimmedText} --"

            Attributes.ATTRIBUTE -> psi.getAttributeValue(Attribute.QUALIFIER)
                ?.let { tablify(psi, it, getCurrentFoldingSettings(psi)?.tablifyItemAttributes, Attributes.ATTRIBUTE, Attribute.QUALIFIER) } +
                " : " + psi.getAttributeValue(Attribute.TYPE)

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
            ItemType.DESCRIPTION,
            Attributes.ATTRIBUTE,
            CustomProperties.PROPERTY,
            Relation.SOURCE_ELEMENT,
            Relation.TARGET_ELEMENT,
            Indexes.INDEX,
            Persistence.COLUMNTYPE -> true

            valueName -> true

            else -> false
        }

        else -> false
    }

    private fun tablifyRelationElement(psi: XmlTag): String {
        val value = psi.getAttributeValue(RelationElement.TYPE) ?: ""
        if (getCurrentFoldingSettings(psi)?.tablifyRelations != true) return value

        val longestLength = psi.parent.childrenOfType<XmlTag>()
            .filter { it.localName == Relation.TARGET_ELEMENT || it.localName == Relation.SOURCE_ELEMENT }
            .mapNotNull { it.getAttributeValue(RelationElement.TYPE) }
            .maxOfOrNull { it.length }
            ?: value.length
        return value + " ".repeat(longestLength - value.length)
    }

    private fun tablify(psi: PsiElement, value: String, tablify: Boolean?, tagName: String, attributeName: String, prepend: Boolean = false) = if (tablify == true) {
        val propertyNamePostfix = " ".repeat(getLongestLength(psi, tagName, attributeName, value.length) - value.length)
        if (prepend) propertyNamePostfix + value else value + propertyNamePostfix
    } else {
        value
    }

    private fun getLongestLength(psi: PsiElement, tagName: String, attributeName: String, fallbackLength: Int) = psi.parent.childrenOfType<XmlTag>()
        .filter { it.localName == tagName }
        .mapNotNull { it.getAttributeValue(attributeName) }
        .maxOfOrNull { it.length }
        ?: fallbackLength

    private fun getCurrentFoldingSettings(psi: PsiElement) = psi.getParentOfType<XmlFile>(false)
        ?.getUserData(cachedFoldingSettings)

    companion object {
        private const val GROUP_NAME = "ItemsXml"
        private const val FALLBACK_PLACEHOLDER = "..."
    }

}
