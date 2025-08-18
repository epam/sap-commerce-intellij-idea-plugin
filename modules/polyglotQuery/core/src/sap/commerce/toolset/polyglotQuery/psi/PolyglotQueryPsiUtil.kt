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

@file:JvmName("PolyglotQueryPsiUtil")

package sap.commerce.toolset.polyglotQuery.psi

import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.childrenOfType

fun getTypeName(element: PolyglotQueryTypeKey): String? = element.childrenOfType<PolyglotQueryTypeKeyName>()
    .firstOrNull()
    ?.text

fun getTypeName(element: PolyglotQueryTypeKeyName): String = element.firstChild.text

fun getTypeName(element: PolyglotQueryAttributeKeyName): String? = PsiTreeUtil
    .getParentOfType(element, PolyglotQueryQuery::class.java)
    ?.typeKey
    ?.typeName
