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

package sap.commerce.toolset.beanSystem.codeInsight.completion.provider

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.psi.util.parentsOfType
import com.intellij.psi.xml.XmlTag
import com.intellij.util.ProcessingContext
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.beanSystem.codeInsight.lookup.BSLookupElementFactory
import sap.commerce.toolset.beanSystem.meta.BSMetaHelper
import sap.commerce.toolset.beanSystem.model.AbstractPojo
import sap.commerce.toolset.beanSystem.model.Beans

class BSBeanPropertyTypeCompletionProvider : CompletionProvider<CompletionParameters>() {

    private val staticLookupElements by lazy {
        primitives.map { BSLookupElementFactory.buildPropertyType(it, 5.0, 5, HybrisIcons.Types.PRIMITIVE, "Primitive") } +
            objects.map { BSLookupElementFactory.buildPropertyType(it, 4.0, 4, HybrisIcons.Types.OBJECT, "Object") } +
            boxed.map { BSLookupElementFactory.buildPropertyType(it, 3.0, 3, HybrisIcons.Types.BOXED, "Boxed") } +
            collections.map { BSLookupElementFactory.buildPropertyType(it, 2.0, 2, HybrisIcons.Types.COLLECTION, "Collection") } +
            maps.map { BSLookupElementFactory.buildPropertyType(it, 1.0, 1, HybrisIcons.Types.MAP, "Map") }
    }

    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        result.addAllElements(staticLookupElements)

        // Generics defined at Bean level
        parameters.position.parentsOfType<XmlTag>()
            .firstOrNull { it.localName == Beans.BEAN }
            ?.getAttributeValue(AbstractPojo.CLASS)
            ?.let { BSMetaHelper.getGenerics(it) }
            ?.map { BSLookupElementFactory.buildPropertyType(it, 6.0, 6, HybrisIcons.Types.GENERIC, "Bean Generic") }
            ?.let { result.addAllElements(it) }

        BSClassCompletionProvider.getInstance().addCompletionVariants(parameters, context, result)
    }

    companion object {
        val objects = setOf("String", "Number", "Object")
        val boxed = setOf("Boolean", "Short", "Integer", "Double", "Float", "Byte", "Character")
        val primitives = setOf("boolean", "int", "long", "float", "double", "char", "short", "byte")
        val maps = setOf("java.util.Map", "java.util.SortedMap")
        val collections = setOf("java.util.Collection", "java.util.Set", "java.util.List", "java.util.Enumeration")
    }
}