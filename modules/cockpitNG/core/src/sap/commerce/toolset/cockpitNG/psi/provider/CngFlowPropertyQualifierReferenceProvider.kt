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
package sap.commerce.toolset.cockpitNG.psi.provider

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.util.ProcessingContext
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.cockpitNG.psi.CngPsiHelper
import sap.commerce.toolset.cockpitNG.psi.reference.CngFlowTSItemAttributeReference
import sap.commerce.toolset.cockpitNG.psi.reference.CngInitializePropertyReference
import sap.commerce.toolset.cockpitNG.psi.reference.CngJavaClassReference
import sap.commerce.toolset.cockpitNG.psi.reference.CngSpringBeanJavaClassReference

class CngFlowPropertyQualifierReferenceProvider : PsiReferenceProvider() {

    override fun getReferencesByElement(
        element: PsiElement, context: ProcessingContext
    ): Array<out PsiReference> = CachedValuesManager.getManager(element.project).getCachedValue(element) {
        val references = (element as? XmlAttributeValue)
            ?.value
            ?.split(".")
            ?.takeIf { it.size == 2 }
            ?.let {
                val initializeProperty = it[0]
                val qualifier = it[1]
                val attrReference: PsiReference? = CngPsiHelper.resolveContextTypeForNewItemInWizardFlow(element)
                    ?.let { type ->
                        val textRange = TextRange.from(initializeProperty.length + 2, qualifier.length)

                        when {
                            type.startsWith(HybrisConstants.COCKPIT_NG_TEMPLATE_BEAN_REFERENCE_PREFIX) ->
                                CngSpringBeanJavaClassReference(element, textRange, type.replace(HybrisConstants.COCKPIT_NG_TEMPLATE_BEAN_REFERENCE_PREFIX, ""))

                            type.contains(".") && type != HybrisConstants.COCKPIT_NG_INITIALIZE_CONTEXT_TYPE ->
                                CngJavaClassReference(element, textRange, type)

                            else -> CngFlowTSItemAttributeReference(element, textRange)
                        }
                    }

                listOfNotNull(
                    CngInitializePropertyReference(element, TextRange.from(1, initializeProperty.length)),
                    attrReference
                )
                    .toTypedArray()
            }
            ?: arrayOf(
                CngInitializePropertyReference(element)
            )

        CachedValueProvider.Result.createSingleDependency(
            references,
            PsiModificationTracker.MODIFICATION_COUNT,
        )
    }

}