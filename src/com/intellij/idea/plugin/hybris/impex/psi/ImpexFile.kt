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
package com.intellij.idea.plugin.hybris.impex.psi

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.idea.plugin.hybris.impex.ImpexLanguage
import com.intellij.idea.plugin.hybris.impex.file.ImpexFileType
import com.intellij.psi.FileViewProvider
import java.io.Serial

class ImpexFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, ImpexLanguage.getInstance()) {

    override fun getFileType() = ImpexFileType.INSTANCE
    override fun toString() = "ImpEx File"
    override fun getIcon(flags: Int) = super.getIcon(flags)

    companion object {
        @Serial
        private const val serialVersionUID: Long = 5112646813557523662L
    }
}