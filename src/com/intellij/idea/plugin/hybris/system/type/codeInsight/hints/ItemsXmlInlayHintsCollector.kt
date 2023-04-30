/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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

package com.intellij.idea.plugin.hybris.system.type.codeInsight.hints

import com.intellij.codeInsight.hints.FactoryInlayHintsCollector
import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.codeInsight.hints.presentation.InlayPresentation
import com.intellij.icons.AllIcons
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.system.type.model.EnumType
import com.intellij.idea.plugin.hybris.system.type.model.EnumTypes
import com.intellij.idea.plugin.hybris.system.type.model.ItemType
import com.intellij.idea.plugin.hybris.system.type.model.ItemTypes
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiShortNamesCache
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlToken
import com.intellij.psi.xml.XmlTokenType
import com.intellij.refactoring.suggested.startOffset
import com.intellij.util.OpenSourceUtil
import java.awt.Cursor
import javax.swing.Icon

/**
 * use com.intellij.codeInsight.hints.presentation.PresentationFactory#referenceOnHover and show popup from clickListener
 */
class ItemsXmlInlayHintsCollector(editor: Editor) : FactoryInlayHintsCollector(editor) {

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

        retrievePresentation(parent, attribute, project, element)
            ?.let {
                sink.addInlineElement(element.startOffset, true, it, false)
            }

        return true
    }

    private fun retrievePresentation(parent: XmlTag, attribute: String, project: Project, element: XmlToken): InlayPresentation? {
        if (parent.name == EnumTypes.ENUMTYPE && attribute == EnumType.CODE) {
            return finEnumClass(project, element.text)
                .takeIf { it.isNotEmpty() }
                ?.let { inlayPresentation(HybrisIcons.TS_ENUM, it) }
                ?: unknown
        } else if (parent.name == ItemTypes.ITEMTYPE && attribute == ItemType.CODE) {
            return findItemClass(project, element.text)
                .takeIf { it.isNotEmpty() }
                ?.let { inlayPresentation(HybrisIcons.TS_ITEM, it) }
                ?: unknown
        } else if (parent.name == "value") {
            val enumName = parent.parentOfType<XmlTag>()
                ?.takeIf { it.name == EnumTypes.ENUMTYPE }
                ?.getAttributeValue(EnumType.CODE)
                ?: return null

            return finEnumClass(project, enumName)
                .takeIf { it.isNotEmpty() }
                ?.mapNotNull { it.allFields.find { field -> field.name.equals(element.text, true) } }
                ?.toTypedArray()
                ?.let { inlayPresentation(HybrisIcons.TS_ENUM_VALUE, it) }
                ?: unknown
        }
        return null
    }

    private fun inlayPresentation(icon: Icon, navigatables: Array<out Navigatable>): InlayPresentation {
        val icon = factory.icon(icon)
        val inset = factory.inset(icon, right = 5, top = 3)
        val tooltip = factory.withTooltip("Navigate to the Generated File", inset)

        return factory.referenceOnHover(tooltip) { _, _ -> OpenSourceUtil.navigate(*navigatables) }
    }

    private fun findItemClass(project: Project, element: String): Array<out PsiClass> =
        PsiShortNamesCache.getInstance(project).getClassesByName(
            element + HybrisConstants.MODEL_SUFFIX, GlobalSearchScope.allScope(project)
        )
            .filter { it.containingFile.virtualFile.path.contains(HybrisConstants.EXCLUDE_BOOTSTRAP_DIRECTORY) }
            .toTypedArray()

    private fun finEnumClass(project: Project, element: String): Array<out PsiClass> =
        PsiShortNamesCache.getInstance(project).getClassesByName(
            element, GlobalSearchScope.allScope(project)
        )
            .filter { it.containingFile.virtualFile.path.contains(HybrisConstants.EXCLUDE_BOOTSTRAP_DIRECTORY) }
            .filter { psiClass ->
                psiClass.implementsListTypes
                    .mapNotNull { it.resolve() }
                    .any {
                        it.qualifiedName == HybrisConstants.CLASS_ENUM_ROOT
                    }
            }
            .toTypedArray()
}