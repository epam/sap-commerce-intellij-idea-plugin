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

package sap.commerce.toolset.impex.transform

import com.intellij.lang.Language
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.project.Project
import sap.commerce.toolset.flexibleSearch.file.FlexibleSearchFileType
import sap.commerce.toolset.impex.ImpExLanguage
import sap.commerce.toolset.impex.psi.ImpExValueLine
import sap.commerce.toolset.impex.transform.flexibleSearch.FxSTransformationService
import sap.commerce.toolset.transform.TransformationResult
import sap.commerce.toolset.transform.Transformer

class ImpExValueLineToFxSTransformer : Transformer<ImpExValueLine> {

    override val id: String
        get() = "impexValueLine-to-fxs"
    override val description: String
        get() = "Converts ImpEx Value Line statement to FlexibleSearch format"
    override val outputFileType: LanguageFileType
        get() = FlexibleSearchFileType

    override fun isApplicable(language: Language) = language is ImpExLanguage

    override fun transform(project: Project, psiElement: ImpExValueLine, onComplete: (TransformationResult) -> Unit) = FxSTransformationService.getInstance(project)
        .transform(outputFileType, psiElement, onComplete)

    override suspend fun transform(project: Project, psiElement: ImpExValueLine): TransformationResult = FxSTransformationService.getInstance(project)
        .transform(outputFileType, psiElement)
}