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
package sap.commerce.toolset.typeSystem.folding

import com.intellij.lang.ASTNode
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiElementFilter
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlTag
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.folding.XmlFoldingBuilderEx
import sap.commerce.toolset.typeSystem.model.*
import sap.commerce.toolset.typeSystem.settings.TSFoldingSettings
import sap.commerce.toolset.typeSystem.settings.state.TSFoldingSettingsState

class ItemsXmlFoldingBuilder : XmlFoldingBuilderEx<TSFoldingSettingsState, Items>(Items::class.java), DumbAware {

    // it can be: EnumValue, ColumnType, but not CustomProperty
    private val valueName = "value"

    override val filter = PsiElementFilter {
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

    override fun initSettings(project: Project) = TSFoldingSettings.getInstance().state

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

            Persistence.COLUMNTYPE -> buildString {
                append("[type] ")
                psi.getAttributeValue(ColumnType.DATABASE)?.let { "$it : " }
                    ?.let { append(it) }
                psi.childrenOfType<XmlTag>()
                    .firstOrNull()
                    ?.value
                    ?.trimmedText
                    ?.let { append(it) }
            }

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

            Indexes.INDEX -> buildString {
                val name = psi.getAttributeValue(Index.NAME)
                    ?.let { tablify(psi, it, getCachedFoldingSettings(psi)?.tablifyItemIndexes, Indexes.INDEX, Index.NAME) }
                    ?: FALLBACK_PLACEHOLDER
                val indexes = psi.childrenOfType<XmlTag>()
                    .mapNotNull { it.getAttributeValue(IndexKey.ATTRIBUTE) }
                    .joinToString(", ", TYPE_SEPARATOR)
                append(name)
                append(indexes)
            }

            CustomProperties.PROPERTY -> buildString {
                val name = psi.getAttributeValue(CustomProperty.NAME)
                    ?.let { tablify(psi, it, getCachedFoldingSettings(psi)?.tablifyItemCustomProperties, CustomProperties.PROPERTY, CustomProperty.NAME) }
                    ?: FALLBACK_PLACEHOLDER
                append(name)

                psi.childrenOfType<XmlTag>()
                    .firstOrNull()
                    ?.value
                    ?.trimmedText
                    ?.let { TYPE_SEPARATOR + if (it.length > 50) it.substring(0, 50) + "..." else it }
                    ?.let { append(it) }
            }

            AtomicTypes.ATOMICTYPE -> buildString {
                val clazz = psi.getAttributeValue(AtomicType.CLASS)
                    ?.let { tablify(psi, it, getCachedFoldingSettings(psi)?.tablifyAtomics, AtomicTypes.ATOMICTYPE, AtomicType.CLASS) }
                    ?.takeIf { it.isNotBlank() }
                    ?: HybrisConstants.Folding.NO_VALUE
                append(clazz)

                psi.getAttributeValue(AtomicType.EXTENDS)?.let { TYPE_SEPARATOR + it }
                    ?.let { append(it) }
            }

            MapTypes.MAPTYPE -> buildString {
                val code = psi.getAttributeValue(MapType.CODE)
                    ?.let { tablify(psi, it, getCachedFoldingSettings(psi)?.tablifyMaps, MapTypes.MAPTYPE, MapType.CODE) }
                    ?.takeIf { it.isNotBlank() }
                    ?: HybrisConstants.Folding.NO_VALUE
                val argumentType = psi.getAttributeValue(MapType.ARGUMENTTYPE)
                    ?.let { tablify(psi, it, getCachedFoldingSettings(psi)?.tablifyMaps, MapTypes.MAPTYPE, MapType.ARGUMENTTYPE, prepend = true) }
                val returnType = psi.getAttributeValue(MapType.RETURNTYPE)

                append(code)
                append(TYPE_SEPARATOR)
                append(argumentType)
                append(" <-> ")
                append(returnType)
            }

            CollectionTypes.COLLECTIONTYPE -> buildString {
                val code = psi.getAttributeValue(CollectionType.CODE)
                    ?.let { tablify(psi, it, getCachedFoldingSettings(psi)?.tablifyCollections, CollectionTypes.COLLECTIONTYPE, CollectionType.CODE) }
                    ?.takeIf { it.isNotBlank() }
                    ?: HybrisConstants.Folding.NO_VALUE
                val elementType = psi.getAttributeValue(CollectionType.ELEMENTTYPE)
                    ?.let { tablify(psi, it, getCachedFoldingSettings(psi)?.tablifyCollections, CollectionTypes.COLLECTIONTYPE, CollectionType.ELEMENTTYPE) }
                    ?.takeIf { it.isNotBlank() }
                    ?: HybrisConstants.Folding.NO_VALUE
                val type = psi.getAttributeValue(CollectionType.TYPE)
                    ?: Type.COLLECTION.value

                append(code)
                append(TYPE_SEPARATOR)
                append(elementType)
                append(TYPE_SEPARATOR)
                append(type)
            }

            Relation.SOURCE_ELEMENT,
            Relation.TARGET_ELEMENT -> buildString {
                val cardinality = psi.getAttributeValue(RelationElement.CARDINALITY) ?: Cardinality.MANY.value
                append(cardinality.let { if (it.length == Cardinality.ONE.value.length) "$it " else it })
                append(TYPE_SEPARATOR)
                append(tablifyRelationElement(psi))

                psi.getAttributeValue(RelationElement.QUALIFIER)?.let { TYPE_SEPARATOR + it }
                    ?.let { append(it) }
            }

            ItemTypes.ITEMTYPE -> buildString {
                val code = psi.getAttributeValue(ItemType.CODE)
                    ?.takeIf { it.isNotBlank() }
                    ?: HybrisConstants.Folding.NO_VALUE
                val extends = (psi.getAttributeValue(ItemType.EXTENDS)
                    ?: HybrisConstants.TS_TYPE_GENERIC_ITEM)

                append(code)
                append(TYPE_SEPARATOR)
                append(extends)
            }

            ItemType.DEPLOYMENT -> buildString {
                val typeCode = psi.getAttributeValue(Deployment.TYPE_CODE)
                    ?.takeIf { it.isNotBlank() }
                    ?: HybrisConstants.Folding.NO_VALUE
                val table = psi.getAttributeValue(Deployment.TABLE)
                    ?.takeIf { it.isNotBlank() }
                    ?: HybrisConstants.Folding.NO_VALUE

                append("DB [")
                append(typeCode)
                append(TYPE_SEPARATOR)
                append(table)
                append("]")
            }

            ItemType.DESCRIPTION -> buildString {
                append(HybrisConstants.Folding.DESCRIPTION_PREFIX)
                append(' ')
                append(psi.value.trimmedText)
            }

            Attributes.ATTRIBUTE -> buildString {
                val qualifier = psi.getAttributeValue(Attribute.QUALIFIER)
                    ?.let { tablify(psi, it, getCachedFoldingSettings(psi)?.tablifyItemAttributes, Attributes.ATTRIBUTE, Attribute.QUALIFIER) }
                    ?.takeIf { it.isNotBlank() }
                    ?: HybrisConstants.Folding.NO_VALUE
                val type = psi.getAttributeValue(Attribute.TYPE)
                    ?.takeIf { it.isNotBlank() }
                    ?: HybrisConstants.Folding.NO_VALUE

                append(qualifier)
                append(TYPE_SEPARATOR)
                append(type)
                append(mandatory(psi))
            }

            else -> FALLBACK_PLACEHOLDER
        }

        else -> FALLBACK_PLACEHOLDER
    }

    private fun mandatory(psi: PsiElement) = psi.childrenOfType<XmlTag>().firstOrNull { it.name == Attribute.MODIFIERS }
        ?.getAttribute(Modifiers.OPTIONAL)
        ?.value
        ?.let {
            when (it) {
                "false" -> " [!]"
                else -> ""
            }
        }
        ?: ""

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
        val value = psi.getAttributeValue(RelationElement.TYPE)?.let { it + mandatory(psi) } ?: ""
        if (getCachedFoldingSettings(psi)?.tablifyRelations != true) return value

        val longestLength = psi.parent.childrenOfType<XmlTag>()
            .filter { it.localName == Relation.TARGET_ELEMENT || it.localName == Relation.SOURCE_ELEMENT }
            .mapNotNull {
                it.getAttributeValue(RelationElement.TYPE)
                    ?.let { relType -> relType + mandatory(it) }
            }
            .maxOfOrNull { it.length }
            ?: value.length
        return value + " ".repeat(longestLength - value.length)
    }

}
