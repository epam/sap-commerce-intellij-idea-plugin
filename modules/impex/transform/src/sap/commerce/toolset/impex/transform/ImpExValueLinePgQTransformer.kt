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
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.impex.ImpExLanguage
import sap.commerce.toolset.impex.psi.ImpExValueLine
import sap.commerce.toolset.impex.transform.context.ImpExTransformationResult
import sap.commerce.toolset.impex.transform.polyglotQuery.PgQTransformationService
import sap.commerce.toolset.polyglotQuery.PolyglotQueryLanguage
import sap.commerce.toolset.transform.Transformer

class ImpExValueLinePgQTransformer : Transformer<ImpExValueLine, ImpExTransformationResult> {

    override val id: String
        get() = "impexValueLine-to-pgq"
    override val name: String
        get() = "PolyglotQuery"
    override val description: String
        get() = "Converts ImpEx Value Line statement to PolyglotQuery format"
    override val language: Language
        get() = PolyglotQueryLanguage
    override val fileExtension: String
        get() = HybrisConstants.Languages.PolyglotQuery.EXTENSION

    override fun isApplicable(language: Language) = language is ImpExLanguage

    override fun transform(psiElement: ImpExValueLine, onComplete: (ImpExTransformationResult) -> Unit) = PgQTransformationService.getInstance(psiElement.project)
        .transform(name, psiElement, onComplete)

    override suspend fun transform(psiElement: ImpExValueLine): ImpExTransformationResult = PgQTransformationService.getInstance(psiElement.project)
        .transform(name, psiElement)
}
