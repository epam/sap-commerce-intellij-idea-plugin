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

package com.intellij.idea.plugin.hybris.system.businessProcess.util.xml

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.system.businessProcess.model.NavigableElement
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
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
            ?.let { it as NavigableElement }

    }

    override fun getVariants(context: ConvertContext): Collection<NavigableElement> {
        val domManager = DomManager.getDomManager(context.project)
        return PsiTreeUtil.collectElements(context.file.rootTag, filter)
            .filterIsInstance<XmlTag>()
            .mapNotNull { domManager.getDomElement(it) }
            .filterIsInstance<NavigableElement>()
    }

    override fun getPsiElement(dom: NavigableElement?) = dom?.getId()?.xmlAttributeValue

    companion object {
        private val filter: (PsiElement) -> Boolean = { el ->
            el is XmlTag && HybrisConstants.BP_NAVIGABLE_ELEMENTS.contains(el.name)
        }
    }
}