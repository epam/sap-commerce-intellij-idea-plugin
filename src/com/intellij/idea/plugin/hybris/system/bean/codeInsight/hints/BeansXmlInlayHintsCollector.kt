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

package com.intellij.idea.plugin.hybris.system.bean.codeInsight.hints

import com.intellij.codeInsight.hints.FactoryInlayHintsCollector
import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.codeInsight.hints.presentation.InlayPresentation
import com.intellij.icons.AllIcons
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.system.type.model.EnumType
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.pom.Navigatable
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlToken
import com.intellij.psi.xml.XmlTokenType
import com.intellij.refactoring.suggested.startOffset
import com.intellij.util.OpenSourceUtil
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType
import org.jetbrains.kotlin.psi.psiUtil.getPrevSiblingIgnoringWhitespaceAndComments
import java.awt.Cursor
import javax.swing.Icon

/**
 * use com.intellij.codeInsight.hints.presentation.PresentationFactory#referenceOnHover and show popup from clickListener
 */
class BeansXmlInlayHintsCollector(editor: Editor) : FactoryInlayHintsCollector(editor) {

    val fileLabel = "Navigate to the Generated File"
    val propertyLabel = "Navigate to the Generated Property"

    val unknown: InlayPresentation by lazy {
        val icon = factory.icon(AllIcons.General.ExclMark)
        val inset = factory.inset(icon, right = 5, top = 3)
        val tooltip = factory.withTooltip("Not Yet Generated", inset)
        factory.withCursorOnHover(tooltip, Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))
    }

    override fun collect(element: PsiElement, editor: Editor, sink: InlayHintsSink): Boolean {
        val project = editor.project ?: return false
        if (DumbService.isDumb(project)) return false
        if (element !is XmlToken) return true
        if (element.tokenType != XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN) return true
        val parent = element.parentOfType<XmlTag>() ?: return true
        val attribute = element.parentOfType<XmlAttribute>()?.name ?: return true

        val previousSibling = element.getPrevSiblingIgnoringWhitespaceAndComments()
            ?.text
            ?: ""
        if (previousSibling == HybrisConstants.BS_SIGN_LESS_THAN || previousSibling == HybrisConstants.BS_SIGN_LESS_THAN_ESCAPED) {
            return true;
        }

        retrievePresentation(parent, attribute, project, element)
            ?.let {
                sink.addInlineElement(element.startOffset, true, it, false)
            }

        return true
    }

    private fun retrievePresentation(parent: XmlTag, attribute: String, project: Project, element: XmlToken) = when {
        parent.name == "enum" && attribute == "class" -> finEnumClass(project, element.text)
            ?.let { arrayOf(it) }
            ?.let { inlayPresentation(HybrisIcons.BS_ENUM, it, fileLabel) }
            ?: unknown

        parent.name == "bean" && attribute == "class" -> findItemClass(project, element.text)
            ?.let { arrayOf(it) }
            ?.let { inlayPresentation(HybrisIcons.BS_BEAN, it, fileLabel) }
            ?: unknown

        parent.name == "value" -> parent.parentOfType<XmlTag>()
            ?.takeIf { it.name == "enum" }
            ?.getAttributeValue(EnumType.CODE)
            ?.let { finEnumClass(project, it) }
            ?.let { it.allFields.find { field -> field.name.equals(element.text, true) } }
            ?.let { arrayOf(it) }
            ?.let { inlayPresentation(HybrisIcons.TS_ENUM_VALUE, it, fileLabel) }
            ?: unknown

        parent.name == "property" && attribute == "name" -> element.parentOfType<XmlTag>()
            ?.getParentOfType<XmlTag>(true)
            ?.getAttributeValue("class")
            ?.let { findItemClass(project, it) }
            ?.allFields
            ?.find { it.name == parent.getAttributeValue("name") }
            ?.let { arrayOf(it) }
            ?.let { inlayPresentation(HybrisIcons.BS_PROPERTY, it, propertyLabel) }
            ?: unknown

        else -> null
    }

    private fun inlayPresentation(i: Icon, navigatables: Array<out Navigatable>, label: String): InlayPresentation {
        val icon = factory.icon(i)
        val inset = factory.inset(icon, right = 5, top = 3)
        val tooltip = factory.withTooltip(label, inset)

        return factory.referenceOnHover(tooltip) { _, _ -> OpenSourceUtil.navigate(*navigatables) }
    }

    private fun findItemClass(project: Project, classFqn: String) = JavaPsiFacade.getInstance(project)
        .findClass(cleanupFqn(classFqn), GlobalSearchScope.allScope(project))
        ?.takeIf { inBootstrap(it) }

    private fun finEnumClass(project: Project, classFqn: String) = JavaPsiFacade.getInstance(project)
        .findClass(cleanupFqn(classFqn), GlobalSearchScope.allScope(project))
        ?.takeIf { it.isEnum && inBootstrap(it) }

    private fun cleanupFqn(classFqn: String) = classFqn
        .substringBefore(HybrisConstants.BS_SIGN_LESS_THAN)
        .substringBefore(HybrisConstants.BS_SIGN_LESS_THAN_ESCAPED)
        .trim()

    private fun inBootstrap(psiClass: PsiClass) = psiClass.containingFile.virtualFile.path.contains(HybrisConstants.EXCLUDE_BOOTSTRAP_DIRECTORY)
}