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
package com.intellij.idea.plugin.hybris.system.bean.psi.provider

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.system.bean.meta.BSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.bean.meta.model.BSMetaEnum
import com.intellij.idea.plugin.hybris.system.bean.psi.reference.BSBeanReference
import com.intellij.idea.plugin.hybris.system.bean.psi.reference.BSEnumReference
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.util.ProcessingContext

@Service
class BSBeanReferenceProvider : PsiReferenceProvider() {

    override fun getReferencesByElement(
        element: PsiElement, context: ProcessingContext
    ): Array<out PsiReference> {
        val attributeValue = element as? XmlAttributeValue ?: return emptyArray()

        val text = attributeValue.value
            .replace(HybrisConstants.BS_SIGN_LESS_THAN_ESCAPED, "    ")
            .replace(HybrisConstants.BS_SIGN_GREATER_THAN_ESCAPED, "    ")
            .replace(HybrisConstants.BS_SIGN_GREATER_THAN, " ")

        val metaModelAccess = BSMetaModelAccess.getInstance(element.project)

        return process(text)
            .mapNotNull {
                val meta = metaModelAccess.findMetasByName(it.value).firstOrNull()
                    ?: return@mapNotNull null

                val textRange = TextRange.from(it.key, it.value.length)

                return@mapNotNull if (meta is BSMetaEnum) BSEnumReference(element, textRange)
                else BSBeanReference(element, textRange)
            }
            .toTypedArray()
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

    companion object {
        val instance: PsiReferenceProvider = ApplicationManager.getApplication().getService(BSBeanReferenceProvider::class.java)
    }
}
