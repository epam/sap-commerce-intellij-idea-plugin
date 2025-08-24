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
package sap.commerce.toolset.polyglotQuery.lang.annotation

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.util.elementType
import sap.commerce.toolset.i18n
import sap.commerce.toolset.lang.annotation.AbstractAnnotator
import sap.commerce.toolset.polyglotQuery.highlighting.PolyglotQuerySyntaxHighlighter
import sap.commerce.toolset.polyglotQuery.psi.PolyglotQueryAttributeKeyName
import sap.commerce.toolset.polyglotQuery.psi.PolyglotQueryTypes.*
import sap.commerce.toolset.project.PropertyService
import sap.commerce.toolset.typeSystem.psi.reference.result.TSResolveResultUtil

class PolyglotQueryAnnotator : AbstractAnnotator() {

    override val highlighter: SyntaxHighlighter
        get() = PolyglotQuerySyntaxHighlighter.getInstance()

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        when (element.elementType) {
            IDENTIFIER -> when (element.parent.elementType) {
                TYPE_KEY_NAME -> highlightReference(TYPE_KEY_NAME, holder, element, "hybris.inspections.pgq.unresolved.type.key")
                ATTRIBUTE_KEY_NAME -> highlightReference(ATTRIBUTE_KEY_NAME, holder, element, "hybris.inspections.pgq.unresolved.attribute.key")
                BIND_PARAMETER -> highlight(BIND_PARAMETER, holder, element)
                LOCALIZED_NAME -> {
                    val language = element.text.trim()

                    val propertyService = PropertyService.getInstance(element.project)
                    val supportedLanguages = propertyService.getLanguages()

                    if (propertyService.containsLanguage(language, supportedLanguages)) {
                        highlight(LOCALIZED_NAME, holder, element)
                    } else {
                        highlightError(
                            holder, element,
                            i18n(
                                "hybris.inspections.language.unsupported",
                                language,
                                supportedLanguages.joinToString()
                            )
                        )
                    }
                }
            }

            QUESTION_MARK -> when (element.parent.elementType) {
                BIND_PARAMETER -> highlight(BIND_PARAMETER, holder, element)
            }

            LOCALIZED -> {
                element.parent.childrenOfType<PolyglotQueryAttributeKeyName>()
                    .firstOrNull()
                    ?.let { attribute ->
                        val featureName = attribute.text.trim()
                        (attribute.reference as? PsiReferenceBase.Poly<*>)
                            ?.multiResolve(false)
                            ?.firstOrNull()
                            ?.takeIf { !TSResolveResultUtil.isLocalized(it, featureName) }
                            ?.let {
                                highlightError(
                                    holder, element,
                                    i18n("hybris.inspections.language.unexpected", featureName)
                                )
                            }
                    }
            }
        }
    }

}
