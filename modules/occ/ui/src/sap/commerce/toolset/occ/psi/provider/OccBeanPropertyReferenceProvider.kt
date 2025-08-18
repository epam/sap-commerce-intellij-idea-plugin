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

package sap.commerce.toolset.occ.psi.provider

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.util.*
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlTag
import com.intellij.util.ProcessingContext
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.beanSystem.BSConstants
import sap.commerce.toolset.beanSystem.meta.BSMetaModelAccess
import sap.commerce.toolset.beanSystem.meta.model.BSGlobalMetaBean
import sap.commerce.toolset.occ.psi.OccPropertyMapping
import sap.commerce.toolset.occ.psi.reference.OccBSBeanPropertyReference
import sap.commerce.toolset.occ.psi.reference.OccLevelMappingReference
import java.util.*

class OccBeanPropertyReferenceProvider : PsiReferenceProvider() {

    // TODO*: Create new Global OCC Meta Model, which will contain list of levelMappings and properties per dtoClass

    override fun getReferencesByElement(
        element: PsiElement, context: ProcessingContext
    ): Array<out PsiReference> = CachedValuesManager.getManager(element.project).getCachedValue(element) {
        val attributeValue = element as? XmlAttributeValue
            ?: return@getCachedValue CachedValueProvider.Result.createSingleDependency(emptyArray(), PsiModificationTracker.MODIFICATION_COUNT)

        val propertyXmlTags = element.parents(false)
            .mapNotNull { it as? XmlTag }
            .filter { it.localName == "bean" }
            .firstOrNull()
            ?.childrenOfType<XmlTag>()
            ?.filter { it.localName == "property" }
            ?: return@getCachedValue CachedValueProvider.Result.createSingleDependency(emptyArray(), PsiModificationTracker.MODIFICATION_COUNT)
        val currentLevelMappings = propertyXmlTags
            .firstOrNull { it.getAttributeValue("name") == BSConstants.ATTRIBUTE_VALUE_LEVEL_MAPPING }
            ?.let { PsiTreeUtil.collectElements(it) { element -> element is XmlAttribute && element.localName == "key" } }
            ?.map { it as XmlAttribute }
            ?.mapNotNull { it.value }
            ?: return@getCachedValue CachedValueProvider.Result.createSingleDependency(emptyArray(), PsiModificationTracker.MODIFICATION_COUNT)

        val meta = propertyXmlTags
            .firstOrNull { it.getAttributeValue("name") == BSConstants.ATTRIBUTE_VALUE_DTO_CLASS }
            ?.let { BSMetaModelAccess.getInstance(element.project).findMetaBeanByName(it.getAttributeValue("value")) }
            ?: return@getCachedValue CachedValueProvider.Result.createSingleDependency(emptyArray(), PsiModificationTracker.MODIFICATION_COUNT)

        val levelMappings = currentLevelMappings + HybrisConstants.OCC_DEFAULT_LEVEL_MAPPINGS

        val properties = processProperties(attributeValue.value)
        val ignoredProperties = listOf<String>()
        val references = collectReferences(meta, attributeValue, properties, levelMappings, ignoredProperties, 0)
            .toTypedArray()

        CachedValueProvider.Result.createSingleDependency(
            references,
            PsiModificationTracker.MODIFICATION_COUNT,
        )
    }

    private fun collectReferences(
        meta: BSGlobalMetaBean,
        attributeValue: XmlAttributeValue,
        properties: List<OccPropertyMapping>,
        levelMappings: List<String>,
        ignoredProperties: List<String>,
        recursiveLevel: Int
    ): List<PsiReference> {
        val ownReferences = properties
            .mapNotNull {
                if (ignoredProperties.contains(it.value)) return@mapNotNull null

                return@mapNotNull if (levelMappings.contains(it.value)) OccLevelMappingReference(meta, attributeValue, it)
                else OccBSBeanPropertyReference(meta, attributeValue, it)
            }

        if (recursiveLevel > 10) return ownReferences

        // we cannot detect valid Level mapping for nested properties without global OCC Meta Model
        val nestedIgnoredProperties = listOf("BASIC", "DEFAULT", "FULL")

        val nestedReferences = properties
            .filter { it.children.isNotEmpty() }
            .mapNotNull {
                val metaProperty = meta.allProperties[it.value]
                    ?.referencedType
                    ?: return@mapNotNull null
                val nestedMeta = BSMetaModelAccess.getInstance(attributeValue.project).findMetaBeanByName(metaProperty)
                    ?: return@mapNotNull null

                collectReferences(nestedMeta, attributeValue, it.children, emptyList(), nestedIgnoredProperties, recursiveLevel + 1)
            }
            .flatten()

        return ownReferences + nestedReferences
    }

    private fun processProperties(text: String): List<OccPropertyMapping> {
        val parentProperties = LinkedList<OccPropertyMapping>()
        val properties = mutableListOf<OccPropertyMapping>()
        val textLength = text.length - 1
        val tempPropertyName = StringBuilder("")
        var newPropertyIndex = 0

        text.withIndex().forEach { iv ->
            val c = iv.value
            val index = iv.index

            if (tempPropertyName.isEmpty()) newPropertyIndex = index + 1

            if (c != '\n' && c != '\t' && c != ',' && c != '(' && c != ')' && (tempPropertyName.isNotEmpty() || c != ' ')) {
                tempPropertyName.append(c)
            }

            if ((c == ',' || index == textLength) && tempPropertyName.isNotEmpty()) {
                val newProperty = OccPropertyMapping(newPropertyIndex, tempPropertyName.toString().trim())

                if (parentProperties.lastOrNull() == null) properties.add(newProperty)
                else {
                    val lastParent = parentProperties.last()
                    lastParent.children.add(newProperty)
                    newProperty.parent = lastParent
                }
                tempPropertyName.clear()
            } else if (c == '(') {
                val newProperty = OccPropertyMapping(newPropertyIndex, tempPropertyName.toString().trim())

                if (parentProperties.lastOrNull() == null) {
                    properties.add(newProperty)
                    parentProperties.add(newProperty)
                } else {
                    val lastParent = parentProperties.last()
                    lastParent.children.add(newProperty)
                    newProperty.parent = lastParent
                    parentProperties.add(newProperty)
                }
                tempPropertyName.clear()
            } else if (c == ')') {
                if (tempPropertyName.isNotEmpty()) {
                    val newProperty = OccPropertyMapping(newPropertyIndex, tempPropertyName.toString().trim())

                    parentProperties.lastOrNull()
                        ?.let {
                            it.children.add(newProperty)
                            newProperty.parent = it
                        }
                }

                parentProperties.removeLastOrNull()

                tempPropertyName.clear()
            }
        }

        return properties
    }

}