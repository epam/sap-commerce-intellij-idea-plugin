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

package sap.commerce.toolset.beanSystem.codeInspection

import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.project.Project
import com.intellij.util.xml.GenericAttributeValue
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder
import com.intellij.util.xml.highlighting.DomHighlightingHelper
import sap.commerce.toolset.beanSystem.model.Beans
import sap.commerce.toolset.codeInspection.fix.XmlDeleteTagQuickFix
import sap.commerce.toolset.i18n

class BSKeywordIsNotAllowedAsBeanPropertyName : BSInspection() {

    private val javaKeywords = hashSetOf(
        "abstract",
        "assert",
        "boolean",
        "break",
        "byte",
        "case",
        "catch",
        "char",
        "class",
        "const",
        "continue",
        "default",
        "do",
        "double",
        "else",
        "enum",
        "extends",
        "false",
        "final",
        "finally",
        "float",
        "for",
        "goto",
        "if",
        "implements",
        "import",
        "instanceof",
        "int",
        "interface",
        "long",
        "native",
        "new",
        "null",
        "package",
        "private",
        "protected",
        "public",
        "return",
        "short",
        "static",
        "strictfp",
        "super",
        "switch",
        "synchronized",
        "this",
        "throw",
        "throws",
        "transient",
        "true",
        "try",
        "void",
        "volatile",
        "while",
        "permits",
        "_",
        "provides",
        "uses",
        "opens",
        "open",
        "requires",
        "exports",
        "module",
        "yield",
        "with",
        "var",
        "uses",
        "transitive",
        "to",
        "record",
        "non-sealed",
        "sealed",
    )

    override fun inspect(
        project: Project,
        dom: Beans,
        holder: DomElementAnnotationHolder,
        helper: DomHighlightingHelper,
        severity: HighlightSeverity
    ) {
        dom.beans
            .flatMap { it.properties }
            .map { it.name }
            .forEach { inspect(it, holder, severity) }
    }

    private fun inspect(
        dom: GenericAttributeValue<String>,
        holder: DomElementAnnotationHolder,
        severity: HighlightSeverity
    ) {
        val propertyName = dom.xmlAttributeValue?.value ?: return

        if (javaKeywords.contains(propertyName)) {
            holder.createProblem(
                dom,
                severity,
                i18n("hybris.inspections.bs.BSKeywordIsNotAllowedAsBeanPropertyName.message", propertyName),
                XmlDeleteTagQuickFix()
            )
        }
    }

}