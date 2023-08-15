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

package com.intellij.idea.plugin.hybris.codeInspection.rule.occ

import com.intellij.idea.plugin.hybris.codeInspection.rule.AbstractInspection
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.system.bean.meta.BSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSGlobalMetaBean
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.util.xml.DomElement
import com.intellij.util.xml.DomManager
import com.intellij.util.xml.GenericAttributeValue
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder
import com.intellij.util.xml.highlighting.DomHighlightingHelper

class OccUnresolvedOccPropertyMapping : AbstractInspection<DomElement>(DomElement::class.java) {

    // TODO: Introduce new Global Meta Model for OCC, which will scan all `.xml` and create Map of <BeanClass to Levels, etc.>
    // extends may be have generics, `com.x.y.ChildBean extends com.x.y.ParentBean<com.x.y.SuperBean>`
    // where ParentBean declared as `com.x.y.ParentBean<SOME>`
    private val ignoredLevelMappings = setOf("BASIC", "DEFAULT", "FULL")

    override fun canProcess(project: Project, file: XmlFile) = file.rootTag?.localName == "beans"
        && file.rootTag?.namespace == "http://www.springframework.org/schema/beans"

    override fun canProcess(dom: DomElement) = dom.xmlElement != null
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
            .filter { it.localName == "bean" }
            .filter { it.getAttributeValue("parent") == "fieldSetLevelMapping" }
            .flatMap { processLevelMappingTag(domManager, bsMetaModelAccess, it) }
            .forEach {
                val domValue = it.first
                val notFoundProperties = it.second
                notFoundProperties.forEach { property ->
                    inspect(domValue, property.key, property.value, holder, severity)
                }
            }
    }

    private fun processLevelMappingTag(domManager: DomManager, bsMetaModelAccess: BSMetaModelAccess, xmlTag: XmlTag): List<Pair<GenericAttributeValue<*>, Map<Int, String>>> {
        val levelMapping = findLevelMapping(xmlTag) ?: return emptyList()
        val bean = findBean(bsMetaModelAccess, xmlTag) ?: return emptyList()
        return PsiTreeUtil.collectElements(levelMapping) { element -> element is XmlAttribute && element.localName == "value" }
            .filter { it.textLength > 0 }
            .map { it as XmlAttribute }
            .mapNotNull { attribute ->
                val domElement = domManager.getDomElement(attribute)
                    ?: return@mapNotNull null
                val notFoundProperties = processProperties(bean, attribute)
                    .takeIf { properties -> properties.isNotEmpty() }
                    ?: return@mapNotNull null
                domElement to notFoundProperties
            }
    }

    /**
     * @return Map<startOffsetInText,Property>
     */
    private fun processProperties(bean: BSGlobalMetaBean, xmlAttribute: XmlAttribute): Map<Int, String> {
        val text = xmlAttribute.value ?: return emptyMap()
        val notFoundProperties = mutableMapOf<Int, String>()
        var tempPropertyName = ""
        for (i in text.indices) {
            val c = text[i]
            if (c == ',' || c == ' ' || c == '\n') {
                consumeProperty(bean, tempPropertyName, notFoundProperties, i)
                tempPropertyName = ""
            } else if (i == text.length - 1) {
                tempPropertyName += c
                consumeProperty(bean, tempPropertyName, notFoundProperties, i + 1)
            } else {
                tempPropertyName += c
            }
        }
        return notFoundProperties
    }

    private fun consumeProperty(bean: BSGlobalMetaBean, currentProperty: String, notFoundProperties: MutableMap<Int, String>, lastIndexOfProperty: Int) {
        if (currentProperty.isEmpty()) return

        val propertyName = if (currentProperty.contains('('))
            currentProperty.substringBefore('(')
        else currentProperty

        // TODO: introduce `allProperties`, which will respect "extends"
        // TODO: rely on new Global Meta Model and respect all possible level mappings, even custom
        if (!ignoredLevelMappings.contains(propertyName) && bean.properties[propertyName] == null) {
            notFoundProperties[lastIndexOfProperty - currentProperty.length] = propertyName
        }
    }

    private fun inspect(
        valueElement: GenericAttributeValue<*>,
        startOffset: Int,
        propertyName: String,
        holder: DomElementAnnotationHolder,
        severity: HighlightSeverity
    ) {
        holder.createProblem(
            valueElement,
            severity,
            "Not found: $propertyName",
            TextRange.from(startOffset + HybrisConstants.QUOTE_LENGTH - 1, propertyName.length)
        )
    }

    private fun findBean(bsMetaModelAccess: BSMetaModelAccess, xmlTag: XmlTag) = xmlTag.childrenOfType<XmlTag>()
        .filter { child -> child.getAttributeValue("name") == "dtoClass" }
        .mapNotNull { child -> child.getAttribute("value") }
        .mapNotNull { bean -> bean.value }
        .flatMap { beanName -> bsMetaModelAccess.findMetaBeansByName(beanName) }
        .firstOrNull()

    private fun findLevelMapping(xmlTag: XmlTag) = xmlTag.childrenOfType<XmlTag>()
        .firstOrNull { child -> child.getAttributeValue("name") == "levelMapping" }

}