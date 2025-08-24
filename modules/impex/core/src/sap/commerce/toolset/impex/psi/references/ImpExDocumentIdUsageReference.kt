/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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

package sap.commerce.toolset.impex.psi.references

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.*
import com.intellij.util.asSafely
import sap.commerce.toolset.impex.lang.refactoring.ImpExPsiElementManipulator
import sap.commerce.toolset.impex.psi.ImpExDocumentIdDec
import sap.commerce.toolset.impex.psi.ImpExDocumentIdUsage
import sap.commerce.toolset.impex.psi.ImpExValue
import sap.commerce.toolset.typeSystem.meta.TSMetaModelAccess

class ImpExDocumentIdUsageReference(
    private val impexValue: ImpExValue,
    textRange: TextRange,
) : PsiReferenceBase.Poly<PsiElement>(impexValue, textRange, false) {

    private val cacheKeyResolvedResults = Key.create<ParameterizedCachedValue<Array<ResolveResult>, ImpExDocumentIdUsageReference>>("RESOLVED_RESULTS_$textRange")

    override fun getVariants(): Array<LookupElementBuilder> = CachedValuesManager.getManager(element.project)
        .getParameterizedCachedValue(element, KEY_LOOKUP_ELEMENTS, PROVIDER_LOOKUP_ELEMENTS, false, this)

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> = CachedValuesManager.getManager(element.project)
        .getParameterizedCachedValue(element, cacheKeyResolvedResults, PROVIDER_RESOLVED_RESULTS, false, this)

    override fun handleElementRename(newElementName: String) = ImpExPsiElementManipulator().handleContentChange(element, rangeInElement, newElementName)

    companion object {
        private val KEY_LOOKUP_ELEMENTS = Key.create<ParameterizedCachedValue<Array<LookupElementBuilder>, ImpExDocumentIdUsageReference>>("LOOKUP_ELEMENTS")

        private val PROVIDER_LOOKUP_ELEMENTS = ParameterizedCachedValueProvider<Array<LookupElementBuilder>, ImpExDocumentIdUsageReference> { ref ->
            val fullHeaderParameter = ref.impexValue.valueGroup
                ?.fullHeaderParameter
                ?: return@ParameterizedCachedValueProvider CachedValueProvider.Result.create(emptyArray(), PsiModificationTracker.MODIFICATION_COUNT)

            val lookupElements = fullHeaderParameter
                .parametersList
                .firstOrNull()
                ?.parameterList
                ?.takeIf { it.size == 1 }
                ?.firstOrNull()
                ?.childrenOfType<ImpExDocumentIdUsage>()
                ?.firstOrNull()
                ?.reference
                ?.asSafely<ImpExDocumentIdReference>()
                ?.multiResolve(false)
                ?.mapNotNull { it.element as? ImpExDocumentIdDec }
                ?.flatMap { it.values.values }
                ?.flatten()
                ?.distinctBy { it.text }
                ?.map { idDec ->
                    val meta = idDec.valueGroup?.fullHeaderParameter?.headerLine?.fullHeaderType?.headerTypeName?.text
                        ?.let { TSMetaModelAccess.getInstance(ref.impexValue.project).findMetaClassifierByName(it) }

                    LookupElementBuilder.createWithSmartPointer(idDec.text, idDec).also { builder ->
                        if (meta != null) {
                            return@map builder
                                .withTypeIconRightAligned(true)
                                .withTypeText(meta.name, meta.icon, true)
                        }
                    }
                }
                ?.toTypedArray()
                ?: emptyArray()

            CachedValueProvider.Result.create(lookupElements, PsiModificationTracker.MODIFICATION_COUNT)
        }

        private val PROVIDER_RESOLVED_RESULTS = ParameterizedCachedValueProvider<Array<ResolveResult>, ImpExDocumentIdUsageReference> { ref ->
            val name = ref.value
            val results = ref.impexValue.valueGroup
                ?.fullHeaderParameter
                ?.parametersList
                ?.firstOrNull()
                ?.parameterList
                ?.takeIf { it.size == 1 }
                ?.firstOrNull()
                ?.childrenOfType<ImpExDocumentIdUsage>()
                ?.firstOrNull()
                ?.reference
                ?.asSafely<ImpExDocumentIdReference>()
                ?.multiResolve(false)
                ?.mapNotNull { it.element as? ImpExDocumentIdDec }
                ?.mapNotNull { it.values[name] }
                ?.flatten()
                ?.let { PsiElementResolveResult.createResults(it) }
                ?: emptyArray()

            CachedValueProvider.Result.create(
                results,
                PsiModificationTracker.MODIFICATION_COUNT,
            )
        }
    }
}
