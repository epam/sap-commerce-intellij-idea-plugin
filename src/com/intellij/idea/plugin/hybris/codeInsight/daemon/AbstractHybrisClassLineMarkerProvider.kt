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

package com.intellij.idea.plugin.hybris.codeInsight.daemon

import com.intellij.idea.plugin.hybris.project.utils.ModuleUtils
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMember

abstract class AbstractHybrisClassLineMarkerProvider<T : PsiElement> : AbstractHybrisLineMarkerProvider<T>() {

    final override fun canProcess(psi: PsiFile) = ModuleUtils.isHybrisModule(psi)
    protected abstract fun canProcess(psi: PsiClass): Boolean

    override fun canProcess(elements: MutableList<out PsiElement>): Boolean {
        if (!super.canProcess(elements)) return false

        val psiClass = elements
            .firstNotNullOfOrNull { it as? PsiMember }
            ?.containingClass
            ?: return false

        return canProcess(psiClass)
    }


}