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

package com.intellij.idea.plugin.hybris.system.cockpitng.psi.reference

import com.intellij.idea.plugin.hybris.system.cockpitng.psi.CngPsiHelper
import com.intellij.psi.PsiElement

class CngFlowTSItemAttributeReference(element: PsiElement) : CngTSItemAttributeReference(element) {

    override fun resolveType(element: PsiElement) = CngPsiHelper.resolveContextTypeForNewItemInWizardFlow(element)

    override fun resolve(): PsiElement? {
        val resolveResults = multiResolve(false)
        if (resolveResults.size != 1) return null

        return with (resolveResults[0]) {
            if (this.isValidResult) return@with this.element
            return@with null
        }
    }
}
