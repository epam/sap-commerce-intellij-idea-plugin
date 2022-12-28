/*
 * This file is part of "hybris integration" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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

package com.intellij.idea.plugin.hybris.system.type.converter

import com.intellij.idea.plugin.hybris.project.utils.PluginCommon.SPRING_PLUGIN_ID
import com.intellij.idea.plugin.hybris.project.utils.PluginCommon.isPluginActive
import com.intellij.idea.plugin.hybris.system.type.psi.reference.PlainXmlReference
import com.intellij.idea.plugin.hybris.system.type.psi.reference.SpringReference
import com.intellij.psi.*
import com.intellij.util.xml.ConvertContext
import com.intellij.util.xml.CustomReferenceConverter
import com.intellij.util.xml.GenericDomValue

class AttributeHandlerReferenceConverter : CustomReferenceConverter<String>, ResolvingHint {

    override fun createReferences(value: GenericDomValue<String>, element: PsiElement, context: ConvertContext): Array<PsiReference>
            = if (isPluginActive(SPRING_PLUGIN_ID)) createSpringReferences(element, value) else createPlainXMLReference(element, value)

    private fun createPlainXMLReference(element: PsiElement, value: GenericDomValue<String>): Array<PsiReference> = arrayOf(PlainXmlReference(element, value))

    private fun createSpringReferences(element: PsiElement, value: GenericDomValue<String>): Array<PsiReference> {
        val name = value.stringValue!!.trim()

        return arrayOf(SpringReference(element, name))
    }

    override fun canResolveTo(elementClass: Class<out PsiElement>) = !PsiDocCommentOwner::class.java.isAssignableFrom(elementClass)

}