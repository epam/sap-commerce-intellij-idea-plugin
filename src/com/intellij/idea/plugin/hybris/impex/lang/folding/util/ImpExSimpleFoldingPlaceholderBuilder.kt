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
package com.intellij.idea.plugin.hybris.impex.lang.folding.util

import com.intellij.idea.plugin.hybris.impex.lang.folding.ImpexFoldingPlaceholderBuilder
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.psi.PsiElement

@Service
class ImpExSimpleFoldingPlaceholderBuilder : ImpexFoldingPlaceholderBuilder {

    override fun getPlaceholder(psiElement: PsiElement): String = with(psiElement.text) {
        if (this.length < 2) {
            psiElement.text
        } else this[0].toString() + this[this.length - 1]
    }

    companion object {
        fun getInstance(): ImpExSimpleFoldingPlaceholderBuilder = ApplicationManager.getApplication().getService(ImpExSimpleFoldingPlaceholderBuilder::class.java)
    }

}
