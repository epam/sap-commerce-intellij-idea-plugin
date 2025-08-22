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

package sap.commerce.toolset.typeSystem.psi.reference

import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.*
import com.intellij.psi.impl.PsiClassImplUtil
import com.intellij.psi.impl.source.xml.XmlAttributeValueImpl
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiShortNamesCache
import com.intellij.psi.util.MethodSignatureUtil
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.typeSystem.meta.TSMetaModelAccess
import sap.commerce.toolset.typeSystem.psi.TSPsiHelper

class ModelAttributeReference(
    element: PsiElement
) : PsiReferenceBase<PsiElement>(element, true), PsiPolyVariantReference {

    private val checkSuperClasses = true

    override fun resolve(): PsiElement? {
        val resolveResults = multiResolve(false)
        return if (resolveResults.size == 1) resolveResults.first().element else null
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val itemName = TSPsiHelper.resolveTypeCode(element)
            ?.takeIf { it.isNotEmpty() } ?: return ResolveResult.EMPTY_ARRAY
        val searchFieldName = (element as XmlAttributeValueImpl).value
        val project = element.project

        return arrayOf(itemName, "$itemName${HybrisConstants.MODEL_SUFFIX}")
            .flatMap { className ->
                PsiShortNamesCache.getInstance(project)
                    .getClassesByName(className, GlobalSearchScope.allScope(project))
                    .flatMap { psiClass ->
                        listOfNotNull(
                            PsiClassImplUtil.findFieldByName(psiClass, searchFieldName.uppercase(), true),
                            findGetter(psiClass, itemName, searchFieldName),
                            findSetter(psiClass, searchFieldName)
                        )
                    }
            }
            .let { PsiElementResolveResult.createResults(it) }
    }

    override fun getVariants(): Array<PsiReference> = EMPTY_ARRAY

    private fun findGetter(psiClass: PsiClass, itemName: String, searchFieldName: String): PsiMethod? {
        val methodName = if (isBooleanProperty(itemName, searchFieldName))
            "is${StringUtil.capitalize(searchFieldName)}"
        else "get${StringUtil.capitalize(searchFieldName)}"

        val methodSignature = MethodSignatureUtil.createMethodSignature(
            methodName,
            PsiType.EMPTY_ARRAY,
            PsiTypeParameter.EMPTY_ARRAY,
            PsiSubstitutor.EMPTY
        )
        return MethodSignatureUtil.findMethodBySignature(psiClass, methodSignature, checkSuperClasses)
    }

    private fun findSetter(psiClass: PsiClass, name: String) = psiClass.findMethodsByName("set${StringUtil.capitalize(name)}", checkSuperClasses)
        .firstOrNull()

    private fun isBooleanProperty(itemName: String, name: String) = TSMetaModelAccess.getInstance(element.project)
        .findMetaItemByName(itemName)
        ?.allAttributes
        ?.get(name)
        ?.let { it.type == HybrisConstants.TS_PRIMITIVE_BOOLEAN }
        ?: false
}