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
package sap.commerce.toolset.beanSystem.psi.provider

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
import sap.commerce.toolset.beanSystem.meta.BSMetaModelAccess
import sap.commerce.toolset.beanSystem.meta.model.BSMetaEnum
import sap.commerce.toolset.beanSystem.psi.reference.BSBeanReference
import sap.commerce.toolset.beanSystem.psi.reference.BSEnumReference

class BSBeanReferenceProvider : PsiReferenceProvider() {

    override fun getReferencesByElement(
        element: PsiElement, context: ProcessingContext
    ): Array<out PsiReference> = CachedValuesManager.getManager(element.project).getCachedValue(element) {
        val attributeValue = element as? XmlAttributeValue
            ?: return@getCachedValue CachedValueProvider.Result.createSingleDependency(emptyArray(), PsiModificationTracker.MODIFICATION_COUNT)

        val text = attributeValue.value
            .replace(HybrisConstants.BS_SIGN_LESS_THAN_ESCAPED, "    ")
            .replace(HybrisConstants.BS_SIGN_GREATER_THAN_ESCAPED, "    ")
            .replace(HybrisConstants.BS_SIGN_GREATER_THAN, " ")

        val metaModelAccess = BSMetaModelAccess.getInstance(element.project)

        val references = process(text)
            .mapNotNull {
                val meta = metaModelAccess.findMetasByName(it.value).firstOrNull()
                    ?: return@mapNotNull null

                val textRange = TextRange.from(it.key, it.value.length)

                return@mapNotNull if (meta is BSMetaEnum) BSEnumReference(element, textRange)
                else BSBeanReference(element, textRange)
            }
            .toTypedArray()

        CachedValueProvider.Result.createSingleDependency(
            references,
            PsiModificationTracker.MODIFICATION_COUNT,
        )
    }

    private fun process(text: String): Map<Int, String> {
        val properties = mutableMapOf<Int, String>()
        var tempPropertyName = ""
        for (i in text.indices) {
            val c = text[i]
            if (c == ',' || c == ' ') {
                consume(tempPropertyName, properties, i)
                tempPropertyName = ""
            } else if (i == text.length - 1) {
                tempPropertyName += c
                consume(tempPropertyName, properties, i + 1)
            } else {
                tempPropertyName += c
            }
        }
        return properties
    }

    private fun consume(fqn: String, properties: MutableMap<Int, String>, lastIndexOfFqn: Int) {
        if (fqn.isEmpty()) return

        properties[lastIndexOfFqn - fqn.length + 1] = fqn
    }

}
