/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.codeInsight.hints.presentation.InlayPresentation
import com.intellij.idea.plugin.hybris.codeInsight.hints.AbstractSystemAwareInlayHintsCollector
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess
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
import com.intellij.psi.util.startOffset
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlToken
import com.intellij.psi.xml.XmlTokenType
import com.intellij.util.asSafely

/**
 * use com.intellij.codeInsight.hints.presentation.PresentationFactory#referenceOnHover and show popup from clickListener
 */
class ItemsXmlInlayHintsCollector(editor: Editor) : AbstractSystemAwareInlayHintsCollector(editor) {

    private val excludedItemTypes = setOf(
        HybrisConstants.TS_TYPE_GENERIC_ITEM,
        HybrisConstants.TS_TYPE_LOCALIZABLE_ITEM,
        HybrisConstants.TS_TYPE_EXTENSIBLE_ITEM
    )

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
        val name = element.text

        return when {
            parent.name == EnumTypes.ENUMTYPE && attribute == EnumType.CODE -> finEnumClass(project, name)
                .takeIf { it.isNotEmpty() }
                ?.let { inlayPresentation(HybrisIcons.TS_ENUM, it) }
                ?: unknown

            // It is possible to declare many-to-many Relation as Item to add new index
            parent.name == ItemTypes.ITEMTYPE && attribute == ItemType.CODE && !excludedItemTypes.contains(name) -> TSMetaModelAccess.getInstance(project)
                .findMetaRelationByName(name)
                ?.takeIf { it.deployment != null }
                ?.retrieveDom()
                ?.code
                ?.xmlAttributeValue
                ?.asSafely<Navigatable>()
                ?.let { inlayPresentation(HybrisIcons.TS_RELATION, arrayOf(it), "Navigate to Relation declaration") }
                ?: findItemClass(project, name)
                    .takeIf { it.isNotEmpty() }
                    ?.let { inlayPresentation(HybrisIcons.TS_ITEM, it) }
                ?: unknown

            parent.name == "value" -> {
                val enumName = parent.parentOfType<XmlTag>()
                    ?.takeIf { it.name == EnumTypes.ENUMTYPE }
                    ?.getAttributeValue(EnumType.CODE)
                    ?: return null

                finEnumClass(project, enumName)
                    .firstOrNull()
                    ?.allFields
                    ?.find { it.name.equals(name, true) }
                    ?.let { arrayOf(it) }
                    ?.let { inlayPresentation(HybrisIcons.TS_ENUM_VALUE, it) }
                    ?: unknown
            }

            else -> null
        }
    }

    private fun findItemClass(project: Project, element: String): Array<out PsiClass> =
        PsiShortNamesCache.getInstance(project).getClassesByName(
            element + HybrisConstants.MODEL_SUFFIX, GlobalSearchScope.allScope(project)
        )
            .filter { it.containingFile.virtualFile.path.contains("/platform/bootstrap") }
            .toTypedArray()

    private fun finEnumClass(project: Project, element: String): Array<out PsiClass> =
        PsiShortNamesCache.getInstance(project).getClassesByName(
            element, GlobalSearchScope.allScope(project)
        )
            .filter { it.containingFile.virtualFile.path.contains("/platform/bootstrap") }
            .filter { psiClass ->
                psiClass.implementsListTypes
                    .mapNotNull { it.resolve() }
                    .any {
                        it.qualifiedName == HybrisConstants.CLASS_FQN_ENUM_ROOT
                    }
            }
            .toTypedArray()
}