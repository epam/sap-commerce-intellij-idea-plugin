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
package com.intellij.idea.plugin.hybris.psi.reference

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase

abstract class TSReferenceBase<PSI : PsiElement> : PsiReferenceBase.Poly<PSI> {

    constructor(owner: PSI) : super(owner, false)
    constructor(owner: PSI, soft: Boolean) : super(owner, soft)
    constructor(element: PSI?, rangeInElement: TextRange?) : super(element, rangeInElement, false)

    override fun calculateDefaultRangeInElement() = TextRange.from(0, element.textLength)

    protected val project: Project
        get() = element.project
}
