/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.transform

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiFile

interface Transformer<T : PsiFile, R: TransformationResult> {

    val name: String
    val description: String

    fun isApplicable(psiFile: PsiFile): Boolean
    fun transform(psiFile: T, onComplete: (R) -> Unit)
    suspend fun transform(psiFile: T): R

    companion object {
        val EP = ExtensionPointName.create<Transformer<in PsiFile, out TransformationResult>>("sap.commerce.toolset.transformer")
    }
}
