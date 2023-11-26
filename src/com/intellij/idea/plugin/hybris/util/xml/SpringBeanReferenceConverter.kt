/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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

package com.intellij.idea.plugin.hybris.util.xml

import com.intellij.idea.plugin.hybris.project.utils.PluginCommon.SPRING_PLUGIN_ID
import com.intellij.idea.plugin.hybris.project.utils.PluginCommon.isPluginActive
import com.intellij.idea.plugin.hybris.system.type.psi.reference.PlainXmlReference
import com.intellij.idea.plugin.hybris.system.type.psi.reference.SpringReference
import com.intellij.psi.PsiDocCommentOwner
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolvingHint
import com.intellij.util.xml.ConvertContext
import com.intellij.util.xml.CustomReferenceConverter
import com.intellij.util.xml.GenericDomValue

class SpringBeanReferenceConverter : CustomReferenceConverter<String>, ResolvingHint {

    override fun createReferences(
        value: GenericDomValue<String>,
        element: PsiElement,
        context: ConvertContext
    ) = value.stringValue
        ?.takeIf { it.isNotBlank() }
        ?.let {
            if (isPluginActive(SPRING_PLUGIN_ID)) {
                arrayOf(SpringReference(element, it.trim()))
            } else {
                arrayOf(PlainXmlReference(element, value))
            }
        }
        ?: emptyArray()

    override fun canResolveTo(elementClass: Class<out PsiElement>) = !PsiDocCommentOwner::class.java.isAssignableFrom(elementClass)

}