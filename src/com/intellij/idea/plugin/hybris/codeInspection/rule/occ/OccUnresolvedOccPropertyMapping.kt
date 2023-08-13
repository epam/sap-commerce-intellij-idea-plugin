/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.codeInspection.rule.occ

import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils
import com.intellij.idea.plugin.hybris.system.bean.meta.BSMetaModelAccess
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.xml.XmlTag
import com.intellij.util.xml.DomElement
import com.intellij.util.xml.DomManager
import com.intellij.util.xml.GenericAttributeValue
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder
import com.intellij.util.xml.highlighting.DomHighlightingHelper

private const val ERROR_MSG_ONE_PROP_ILLEGAL = "hybris.inspections.occ.OccUnresolvedOccPropertyMapping.one.property.illegal.key"

class OccUnresolvedOccPropertyMapping : AbstractOccInspection() {
    override fun inspect(
        project: Project,
        dom: DomElement,
        holder: DomElementAnnotationHolder,
        helper: DomHighlightingHelper,
        severity: HighlightSeverity
    ) {
        val domManager = DomManager.getDomManager(project)
        val bsMetaModelAccess = BSMetaModelAccess.getInstance(project)
        PsiTreeUtil.findChildrenOfType(dom.xmlElement, XmlTag::class.java)
            .filter { it.filterByTagName("bean") }
            .filter { it.filterByAttributeValue("parent", "fieldSetLevelMapping") }
            .flatMap {
                val dtoClass = domManager.findDtoClass(it)
                val classAttributesXmlTags = domManager.findClassAttributesXmlTag(it)
                val classAttributes = bsMetaModelAccess.findClassAttributesAsString(dtoClass.value.toString())
                classAttributesXmlTags
                    .mapNotNull { attributesXmlTag ->
                        val illegalAttributes = findIllegalProperties(attributesXmlTag, classAttributes)
                        if (illegalAttributes.isNotEmpty()) IllegalAttributeData(attributesXmlTag, illegalAttributes) else null
                    }
            }
            .forEach { inspect(it, holder, severity) }
    }

    private fun inspect(
        data: IllegalAttributeData,
        holder: DomElementAnnotationHolder,
        severity: HighlightSeverity
    ) {
        data.illegalAttributes
            .forEach { illegalAttribute ->
                holder.createProblem(
                    data.xmlTag,
                    severity,
                    HybrisI18NBundleUtils.message(ERROR_MSG_ONE_PROP_ILLEGAL, illegalAttribute),
                    textRange(data.xmlTag.value.toString(), illegalAttribute)
                )
            }
    }

    private fun textRange(listedPropertiesAsString: String, illegalProperty: String): TextRange {
        val startPosition = if (listedPropertiesAsString.endsWith(",$illegalProperty"))
            listedPropertiesAsString.lastIndexOf(",$illegalProperty") + 2
        else
            listedPropertiesAsString.indexOf(",$illegalProperty,") + 2
        val endPosition = startPosition + illegalProperty.length
        return TextRange(startPosition, endPosition)
    }

    private fun findIllegalProperties(
        classAttributesXmlTag: GenericAttributeValue<*>,
        clazzProperties: List<String>
    ) = classAttributesXmlTag.value.toString().split(",")
        .filter {
            val prop = if (it.contains("(")) it.substring(0, it.indexOf("(")) else it
            !clazzProperties.contains(prop)
        }

    private fun XmlTag.filterByTagName(tagName: String): Boolean = localName == tagName
    private fun XmlTag.filterByAttributeValue(attribute: String, attributeValue: String): Boolean = getAttributeValue(attribute) == attributeValue

    private fun DomManager.findClassAttributesXmlTag(
        xmlTag: XmlTag
    ) = xmlTag.childrenOfType<XmlTag>()
        .filter { child -> child.getAttributeValue("name") == "levelMapping" }
        .mapNotNull { it.childrenOfType<XmlTag>().firstOrNull() }
        .flatMap { it.childrenOfType<XmlTag>() }
        .mapNotNull { it.getAttribute("value") }
        .mapNotNull { getDomElement(it) }

    private fun DomManager.findDtoClass(xmlTag: XmlTag) = xmlTag.childrenOfType<XmlTag>()
        .filter { child -> child.getAttributeValue("name") == "dtoClass" }
        .mapNotNull { child -> child.getAttribute("value") }
        .firstNotNullOf { getDomElement(it) }

    private fun BSMetaModelAccess.findClassAttributesAsString(clazz: String): List<String> = findMetaBeansByName(clazz)
        .flatMap { it.retrieveAllDoms() }
        .flatMap { it.properties.mapNotNull { p -> p.name } }
        .mapNotNull { it.value }
}

private data class IllegalAttributeData(
    val xmlTag: GenericAttributeValue<*>,
    val illegalAttributes: List<String>
)