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

package com.intellij.idea.plugin.hybris.system.bean.codeInsight.completion.provider

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.ProcessingContext
import javax.swing.Icon

class BSBeanPropertyTypeCompletionProvider : CompletionProvider<CompletionParameters>() {

    private val additionalLookupElements by lazy {
        primitives.map { lookupElement(it, 5.0, 5, HybrisIcons.TYPE_PRIMITIVE, "Primitive") } +
            objects.map { lookupElement(it, 4.0, 4, HybrisIcons.TYPE_OBJECT, "Object") } +
            boxed.map { lookupElement(it, 3.0, 3, HybrisIcons.TYPE_BOXED, "Boxed") } +
            collections.map { lookupElement(it, 2.0, 2, HybrisIcons.TYPE_COLLECTION, "Collection") } +
            maps.map { lookupElement(it, 1.0, 1, HybrisIcons.TYPE_MAP, "Map") }
    }

    private fun lookupElement(lookupString: String, priority: Double, group: Int, icon: Icon, typeText: String) = PrioritizedLookupElement.withGrouping(
        PrioritizedLookupElement.withPriority(
            LookupElementBuilder.create(lookupString)
                .withIcon(icon)
                .withTypeText(typeText, true), priority
        ), group
    )

    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        result.addAllElements(additionalLookupElements)
        BSClassCompletionProvider.instance.addCompletionVariants(parameters, context, result)
    }

    companion object {
        val objects = setOf("String", "Number", "Object")
        val boxed = setOf("Boolean", "Short", "Integer", "Double", "Float", "Byte", "Character")
        val primitives = setOf("boolean", "int", "long", "float", "double", "char", "short", "byte")
        val maps = setOf("java.util.Map", "java.util.SortedMap")
        val collections = setOf("java.util.Collection", "java.util.Set", "java.util.List", "java.util.Enumeration")
        val instance: BSBeanPropertyTypeCompletionProvider = ApplicationManager.getApplication().getService(BSBeanPropertyTypeCompletionProvider::class.java)
    }
}