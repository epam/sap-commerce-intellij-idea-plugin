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

package com.intellij.idea.plugin.hybris.system.businessProcess.util.xml

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.system.businessProcess.model.*
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import com.intellij.util.asSafely
import com.intellij.util.xml.ConvertContext
import com.intellij.util.xml.DomManager
import com.intellij.util.xml.ResolvingConverter

class BpNavigableElementConverter : ResolvingConverter<NavigableElement>() {

    override fun toString(dom: NavigableElement?, context: ConvertContext) = dom?.getId()?.stringValue

    override fun fromString(name: String?, context: ConvertContext): NavigableElement? {
        if (name == null) return null
        val domManager = DomManager.getDomManager(context.project)
        return PsiTreeUtil.collectElements(context.file.rootTag, filter)
            .filterIsInstance<XmlTag>()
            .firstOrNull { el -> el.getAttribute("id")?.value == name }
            ?.let { domManager.getDomElement(it) }
            ?.asSafely<NavigableElement>()

    }

    override fun getVariants(context: ConvertContext): Collection<NavigableElement> {
        val domManager = DomManager.getDomManager(context.project)
        return PsiTreeUtil.collectElements(context.file.rootTag, filter)
            .filterIsInstance<XmlTag>()
            .mapNotNull { domManager.getDomElement(it) }
            .filterIsInstance<NavigableElement>()
    }

    override fun getPsiElement(dom: NavigableElement?) = dom?.getId()?.xmlAttributeValue

    override fun createLookupElement(dom: NavigableElement?): LookupElement? {
        val id = dom?.getId()?.stringValue ?: return null

        return when (dom) {
            is Process -> LookupElementBuilder.create(id)
                .withTypeText("Process", true)
                .withIcon(HybrisIcons.BUSINESS_PROCESS)

            is ScriptAction -> LookupElementBuilder.create(id)
                .withTypeText("Script Action", true)
                .withIcon(HybrisIcons.BP_DIAGRAM_SCRIPT)

            is Action -> LookupElementBuilder.create(id)
                .withTypeText("Action", true)
                .withIcon(HybrisIcons.BP_DIAGRAM_ACTION)

            is Split -> LookupElementBuilder.create(id)
                .withTypeText("Split", true)
                .withIcon(HybrisIcons.BP_DIAGRAM_SPLIT)

            is Wait -> LookupElementBuilder.create(id)
                .withTypeText("Wait", true)
                .withIcon(HybrisIcons.BP_DIAGRAM_WAIT)

            is Join -> LookupElementBuilder.create(id)
                .withTypeText("Join", true)
                .withIcon(HybrisIcons.BP_DIAGRAM_JOIN)

            is End -> LookupElementBuilder.create(id)
                .withTypeText("End", true)
                .withIcon(HybrisIcons.BP_DIAGRAM_END)

            is Notify -> LookupElementBuilder.create(id)
                .withTypeText("Notify", true)
                .withIcon(HybrisIcons.BP_DIAGRAM_NOTIFY)

            else -> null
        }
    }

    companion object {
        private val filter: (PsiElement) -> Boolean = { el ->
            el is XmlTag && HybrisConstants.BP_NAVIGABLE_ELEMENTS.contains(el.name)
        }
    }
}