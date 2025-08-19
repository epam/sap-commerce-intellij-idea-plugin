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

package sap.commerce.toolset.project.codeInsight.daemon

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMember
import sap.commerce.toolset.codeInsight.daemon.HybrisLineMarkerProvider
import sap.commerce.toolset.project.isHybrisModule

abstract class HybrisClassLineMarkerProvider<T : PsiElement> : HybrisLineMarkerProvider<T>() {

    override fun canProcess(psi: PsiFile) = isHybrisModule(psi)
    protected abstract fun canProcess(psi: PsiClass): Boolean

    override fun canProcess(elements: MutableList<out PsiElement>): Boolean {
        if (!super.canProcess(elements)) return false

        val psiClass = elements
            .firstNotNullOfOrNull { it as? PsiClass }
            ?: elements
                .firstNotNullOfOrNull { it as? PsiMember }
                ?.containingClass
            ?: return false

        return canProcess(psiClass)
    }


}