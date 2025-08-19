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

package sap.commerce.toolset.beanSystem.codeInsight.hints

import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.codeInsight.hints.presentation.InlayPresentation
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.parentOfType
import com.intellij.psi.util.startOffset
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlToken
import com.intellij.psi.xml.XmlTokenType
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.beanSystem.meta.BSMetaModelAccess
import sap.commerce.toolset.beanSystem.model.Bean
import sap.commerce.toolset.beanSystem.model.Beans
import sap.commerce.toolset.beanSystem.model.Enum
import sap.commerce.toolset.beanSystem.model.Property
import sap.commerce.toolset.codeInsight.hints.SystemAwareInlayHintsCollector

/**
 * use com.intellij.codeInsight.hints.presentation.PresentationFactory#referenceOnHover and show popup from clickListener
 */
class BeansXmlInlayHintsCollector(editor: Editor) : SystemAwareInlayHintsCollector(editor) {

    override fun collect(element: PsiElement, editor: Editor, sink: InlayHintsSink): Boolean {
        val project = editor.project ?: return false
        if (DumbService.isDumb(project)) return false
        if (element !is XmlToken) return true
        if (element.tokenType != XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN && element.tokenType != XmlTokenType.XML_DATA_CHARACTERS) return true
        val parent = element.parentOfType<XmlTag>() ?: return true

        val previousSibling = PsiTreeUtil.skipSiblingsBackward(element, PsiComment::class.java, PsiWhiteSpace::class.java)
            ?.text
            ?: ""
        if (previousSibling == HybrisConstants.BS_SIGN_LESS_THAN || previousSibling == HybrisConstants.BS_SIGN_LESS_THAN_ESCAPED) {
            return true
        }

        retrievePresentation(parent, project, element)
            ?.let {
                sink.addInlineElement(element.startOffset, true, it, false)
            }

        return true
    }

    private fun retrievePresentation(parent: XmlTag, project: Project, element: XmlToken): InlayPresentation? {
        val attribute = element.parentOfType<XmlAttribute>()

        return when {
            attribute == null && parent.localName == Enum.VALUE -> parent.parentOfType<XmlTag>()
                ?.takeIf { it.name == Beans.ENUM }
                ?.getAttributeValue(Enum.CLASS)
                ?.let { finEnumClass(project, it) }
                ?.allFields
                ?.find { it.name.equals(element.text, true) }
                ?.let { arrayOf(it) }
                ?.let { inlayPresentation(HybrisIcons.BeanSystem.ENUM_VALUE, it, "Navigate to the Generated Enum Value") }
                ?: unknown

            parent.localName == Beans.ENUM && attribute?.name == Bean.CLASS -> finEnumClass(project, element.text)
                ?.let { arrayOf(it) }
                ?.let { inlayPresentation(HybrisIcons.BeanSystem.ENUM, it) }
                ?: unknown

            parent.localName == Beans.BEAN && attribute?.name == Bean.CLASS -> findItemClass(project, element.text)
                ?.let { arrayOf(it) }
                ?.let {
                    val icon = BSMetaModelAccess.getInstance(project).findMetaBeanByName(element.text)
                        ?.metaType
                        ?.icon
                        ?: HybrisIcons.BeanSystem.BEAN
                    inlayPresentation(icon, it)
                }
                ?: unknown

            parent.localName == Bean.PROPERTY && attribute?.name == Property.NAME -> element.parentOfType<XmlTag>()
                ?.parentOfType<XmlTag>(false)
                ?.getAttributeValue(Bean.CLASS)
                ?.let { findItemClass(project, cleanupFqn(it)) }
                ?.allFields
                ?.find { it.name == parent.getAttributeValue(Property.NAME) }
                ?.let { arrayOf(it) }
                ?.let { inlayPresentation(HybrisIcons.BeanSystem.PROPERTY, it, "Navigate to the Generated Bean Property") }
                ?: unknown

            else -> null
        }
    }

    private fun findItemClass(project: Project, classFqn: String) = JavaPsiFacade.getInstance(project)
        .findClass(classFqn, GlobalSearchScope.allScope(project))
        ?.takeIf { inBootstrap(it) }

    private fun finEnumClass(project: Project, classFqn: String) = JavaPsiFacade.getInstance(project)
        .findClass(classFqn, GlobalSearchScope.allScope(project))
        ?.takeIf { it.isEnum && inBootstrap(it) }

    private fun cleanupFqn(classFqn: String) = classFqn
        .substringBefore(HybrisConstants.BS_SIGN_LESS_THAN)
        .substringBefore(HybrisConstants.BS_SIGN_LESS_THAN_ESCAPED)
        .trim()

    private fun inBootstrap(psiClass: PsiClass) = psiClass.containingFile.virtualFile.path.contains("/platform/bootstrap")
}