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

package com.intellij.idea.plugin.hybris.system.type.util.xml.converter

import com.intellij.idea.plugin.hybris.system.type.psi.reference.RelationElementMetaTypeReference
import com.intellij.psi.PsiDocCommentOwner
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolvingHint
import com.intellij.util.xml.ConvertContext
import com.intellij.util.xml.CustomReferenceConverter
import com.intellij.util.xml.GenericDomValue

class RelationElementMetaTypeReferenceConverter : CustomReferenceConverter<String>, ResolvingHint {

    override fun createReferences(value: GenericDomValue<String>?, element: PsiElement, context: ConvertContext?) = arrayOf(RelationElementMetaTypeReference(element))

    override fun canResolveTo(elementClass: Class<out PsiElement>) = !PsiDocCommentOwner::class.java.isAssignableFrom(elementClass)

}