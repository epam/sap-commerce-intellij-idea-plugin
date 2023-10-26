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
package com.intellij.idea.plugin.hybris.system.bean.psi.provider

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.system.bean.meta.BSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.bean.psi.reference.OccBSBeanPropertyReference
import com.intellij.idea.plugin.hybris.system.bean.psi.reference.OccLevelMappingReference
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlTag
import com.intellij.util.ProcessingContext
import org.jetbrains.kotlin.psi.psiUtil.parents

@Service
class OccBeanPropertyReferenceProvider : PsiReferenceProvider() {

    // TODO*: Create new Global OCC Meta Model, which will contain list of levelMappings and properties per dtoClass

    override fun getReferencesByElement(
        element: PsiElement, context: ProcessingContext
    ): Array<out PsiReference> {
        val attributeValue = element as? XmlAttributeValue ?: return emptyArray()

        val propertyXmlTags = element.parents
            .mapNotNull { it as? XmlTag }
            .filter { it.localName == "bean" }
            .firstOrNull()
            ?.childrenOfType<XmlTag>()
            ?.filter { it.localName == "property" }
            ?: return emptyArray()
        val currentLevelMappings = propertyXmlTags
            .firstOrNull { it.getAttributeValue("name") == "levelMapping" }
            ?.let { PsiTreeUtil.collectElements(it) { element -> element is XmlAttribute && element.localName == "key" } }
            ?.map { it as XmlAttribute }
            ?.mapNotNull { it.value }
            ?: return emptyArray()

        val meta = propertyXmlTags
            .firstOrNull { it.getAttributeValue("name") == "dtoClass" }
            ?.let { BSMetaModelAccess.getInstance(element.project).findMetaBeanByName(it.getAttributeValue("value")) }
            ?: return emptyArray()

        val levelMappings = currentLevelMappings + HybrisConstants.OCC_DEFAULT_LEVEL_MAPPINGS

        return processProperties(attributeValue.value)
            .map {
                val textRange = TextRange.from(it.key, it.value.length)

                return@map if (levelMappings.contains(it.value)) OccLevelMappingReference(meta, attributeValue, textRange)
                else OccBSBeanPropertyReference(meta, attributeValue, textRange)
            }
            .toTypedArray()
    }

    private fun processProperties(text: String): Map<Int, String> {
        val properties = mutableMapOf<Int, String>()
        var tempPropertyName = ""
        for (i in text.indices) {
            val c = text[i]
            if (c == ',' || c == ' ' || c == '\n') {
                consumeProperty(tempPropertyName, properties, i)
                tempPropertyName = ""
            } else if (i == text.length - 1) {
                tempPropertyName += c
                consumeProperty(tempPropertyName, properties, i + 1)
            } else {
                tempPropertyName += c
            }
        }
        return properties
    }

    private fun consumeProperty(currentProperty: String, properties: MutableMap<Int, String>, lastIndexOfProperty: Int) {
        if (currentProperty.isEmpty()) return

        // TODO: also process nested properties, rely on recursion
        val propertyName = if (currentProperty.contains('('))
            currentProperty.substringBefore('(')
        else currentProperty

        properties[lastIndexOfProperty - currentProperty.length + 1] = propertyName
    }

    companion object {
        val instance: PsiReferenceProvider = ApplicationManager.getApplication().getService(OccBeanPropertyReferenceProvider::class.java)
    }
}
