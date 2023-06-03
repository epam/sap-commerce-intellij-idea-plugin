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
package com.intellij.idea.plugin.hybris.system.type.validation.impl

import com.intellij.idea.plugin.hybris.system.type.validation.ItemsXmlDomValidator
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiField
import com.intellij.util.xml.DomElement

abstract class AbstractClassesValidation<T : DomElement, M : DomElement> : ItemsXmlDomValidator<T> {

    abstract fun buildGeneratedClassName(itemType: T): String
    abstract fun buildJavaFieldName(itemAttribute: M): String
    abstract fun getDefinedAttributes(itemType: T): List<M>
    abstract fun isJavaClassGenerationDisabledForItemType(itemType: T): Boolean
    protected abstract fun isJavaFieldGenerationDisabled(itemAttribute: M): Boolean

    override fun validate(dom: List<T>, psi: Map<String, PsiClass>): Boolean {
        for (itemType in dom) {
            if (isJavaClassGenerationDisabledForItemType(itemType)) continue
            val javaClass = getJavaClassForItemType(itemType, psi) ?: return true
            if (!isJavaClassMatchesItemTypeDefinition(javaClass, itemType)) return true
        }
        return false
    }

    private fun isJavaClassMatchesItemTypeDefinition(
        javaClass: PsiClass,
        itemType: T
    ): Boolean {
        val allFields = javaClass.allFields

        for (itemAttribute in getDefinedAttributes(itemType)) {
            if (isJavaFieldGenerationDisabled(itemAttribute)) continue
            if (!isJavaFieldGenerated(itemAttribute, allFields)) return false
        }
        return true
    }

    /**
     * Finds attribute in generated class for attribute defined for type in items.xml
     */
    private fun isJavaFieldGenerated(itemAttribute: M, allFields: Array<PsiField>) = allFields
        .any { it.name.equals(buildJavaFieldName(itemAttribute), true) }

    /**
     * Finds generated class for type defined in items.xml
     */
    private fun getJavaClassForItemType(
        itemType: T,
        generatedClasses: Map<String, PsiClass>
    ) = generatedClasses[buildGeneratedClassName(itemType)]
}
