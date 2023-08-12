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
import java.util.stream.Collectors

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
            .filter { it.localName == "bean" }
            .filter { it.getAttributeValue("parent") == "fieldSetLevelMapping" }
            .mapNotNull {
                val key = it.childrenOfType<XmlTag>()
                    .filter { child -> child.getAttributeValue("name") == "dtoClass" }
                    .mapNotNull { child -> child.getAttribute("value") }
                    .firstNotNullOf { domManager.getDomElement(it) }
                val value = it.childrenOfType<XmlTag>()
                    .filter { child -> child.getAttributeValue("name") == "levelMapping" }
                    .mapNotNull { it.childrenOfType<XmlTag>().firstOrNull() }
                    .flatMap { it.childrenOfType<XmlTag>() }
                    .mapNotNull { it.getAttribute("value") }
                    .mapNotNull { domManager.getDomElement(it) }
                key to value
            }
            .forEach { inspect(it, holder, severity, bsMetaModelAccess) }
    }

    private fun inspect(
        pair: Pair<GenericAttributeValue<*>, List<GenericAttributeValue<*>>>,
        holder: DomElementAnnotationHolder,
        severity: HighlightSeverity,
        bsMetaModelAccess: BSMetaModelAccess
    ) {
        val wsDtoClazz = pair.first.value.toString()
        val clazzProperties = bsMetaModelAccess.findClassProperties(wsDtoClazz)
            .map { it.value }
        val listedClazzProperties: List<GenericAttributeValue<*>> = pair.second
        listedClazzProperties
            .map {
                val nonLegalAttributes = it.value.toString().split(",")
                    .filter { beanProp -> clazzProperties.none { clazzProp -> clazzProp != null && beanProp.contains(clazzProp) } }
                it to nonLegalAttributes
            }
            .filter { it.second.isNotEmpty() }
            .forEach {
                val dom = it.first
                val listedProperties = dom.value.toString()
                it.second
                    .forEach { illegalProperty ->
                        val startPosition = listedProperties.indexOf(illegalProperty) + 1
                        val endPosition = startPosition + illegalProperty.length
                        holder.createProblem(
                            dom,
                            severity,
                            HybrisI18NBundleUtils.message(ERROR_MSG_ONE_PROP_ILLEGAL, illegalProperty),
                            TextRange(startPosition, endPosition)
                        )
                    }
            }
    }
}

fun getTextRange(dom: DomElement) = dom.xmlElement
    ?.let { TextRange.from(0, it.textLength) }

fun BSMetaModelAccess.findClassProperties(clazz: String): List<GenericAttributeValue<String>> {
    return findMetaBeansByName(clazz)
        .flatMap { it.retrieveAllDoms() }
        .flatMap { it.properties.stream().map { p -> p.name }.collect(Collectors.toList()) }
}