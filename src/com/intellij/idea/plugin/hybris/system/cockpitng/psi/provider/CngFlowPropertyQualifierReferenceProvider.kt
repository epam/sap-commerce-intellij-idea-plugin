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
package com.intellij.idea.plugin.hybris.system.cockpitng.psi.provider

import com.intellij.idea.plugin.hybris.system.cockpitng.psi.reference.CngFlowTSItemAttributeReference
import com.intellij.idea.plugin.hybris.system.cockpitng.psi.reference.CngInitializePropertyReference
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.util.ProcessingContext

@Service
class CngFlowPropertyQualifierReferenceProvider : PsiReferenceProvider() {

    override fun getReferencesByElement(
        element: PsiElement, context: ProcessingContext
    ): Array<PsiReference> {
        return (element as? XmlAttributeValue)
            ?.value
            ?.split(".")
            ?.takeIf { it.size == 2 }
            ?.let {
                val initializeProperty = it[0]
                val qualifier = it[1]
                arrayOf(
                    CngInitializePropertyReference(element, TextRange.from(1, initializeProperty.length)),
                    CngFlowTSItemAttributeReference(element, TextRange.from(initializeProperty.length + 2, qualifier.length))
                )
            }
            ?: arrayOf(
                CngInitializePropertyReference(element)
            )
    }

    companion object {
        val instance: PsiReferenceProvider = ApplicationManager.getApplication().getService(CngFlowPropertyQualifierReferenceProvider::class.java)
    }
}