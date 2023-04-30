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

@file:JvmName("ImpexPsiUtil")

package com.intellij.idea.plugin.hybris.impex.psi

import com.intellij.idea.plugin.hybris.impex.utils.ImpexPsiUtils
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.childrenOfType

fun getHeaderLine(element: ImpexFullHeaderParameter): ImpexHeaderLine? = PsiTreeUtil
    .getParentOfType(element, ImpexHeaderLine::class.java)

fun getHeaderLine(element: ImpexValueLine): ImpexHeaderLine? = PsiTreeUtil
    .getPrevSiblingOfType(element, ImpexHeaderLine::class.java)

fun getValueGroup(element: ImpexString): ImpexValueGroup? = PsiTreeUtil
    .getParentOfType(element, ImpexValueGroup::class.java)

fun getColumnNumber(element: ImpexValueGroup): Int = ImpexPsiUtils
    .getColumnNumber(element)

fun getColumnNumber(element: ImpexFullHeaderParameter): Int = ImpexPsiUtils
    .getColumnNumber(element)

fun getValueGroup(element: ImpexValueLine, columnNumber: Int): ImpexValueGroup? = element
    .childrenOfType<ImpexValueGroup>()
    .getOrNull(columnNumber)

fun getValueLine(element: ImpexValueGroup): ImpexValueLine? = PsiTreeUtil
    .getParentOfType(element, ImpexValueLine::class.java)

fun getFullHeaderParameter(element: ImpexValueGroup): ImpexFullHeaderParameter? = ImpexPsiUtils
    .getHeaderForValueGroup(element) as? ImpexFullHeaderParameter
