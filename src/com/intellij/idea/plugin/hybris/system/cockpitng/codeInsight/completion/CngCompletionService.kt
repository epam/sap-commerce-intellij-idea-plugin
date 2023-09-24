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

package com.intellij.idea.plugin.hybris.system.cockpitng.codeInsight.completion

import com.intellij.idea.plugin.hybris.system.cockpitng.codeInsight.lookup.CngLookupElementFactory
import com.intellij.idea.plugin.hybris.system.cockpitng.psi.reference.CngInitializePropertyReference
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.util.parentsOfType
import com.intellij.psi.xml.XmlTag

@Service(Service.Level.PROJECT)
class CngCompletionService {

    fun getInitializeProperties(element: PsiElement) = element.parentsOfType<XmlTag>()
        .firstOrNull { it.localName == "flow" }
        ?.childrenOfType<XmlTag>()
        ?.filter { it.localName == "prepare" }
        ?.flatMap { it.childrenOfType<XmlTag>() }
        ?.filter { it.localName == "initialize" }
        ?.mapNotNull { CngLookupElementFactory.buildInitializeProperty(it) }
        ?.toTypedArray()
        ?.takeIf { it.isNotEmpty() }
        ?: arrayOf(
            CngLookupElementFactory.buildInitializeProperty(CngInitializePropertyReference.NEW_OBJECT)
        )

    companion object {
        fun getInstance(project: Project): CngCompletionService = project.getService(CngCompletionService::class.java)
    }
}