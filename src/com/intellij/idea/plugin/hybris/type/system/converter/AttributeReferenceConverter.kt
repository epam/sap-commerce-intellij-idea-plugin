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

package com.intellij.idea.plugin.hybris.type.system.converter

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.openapi.util.Comparing
import com.intellij.openapi.util.text.StringUtil.capitalize
import com.intellij.psi.*
import com.intellij.psi.impl.PsiClassImplUtil
import com.intellij.psi.impl.source.xml.XmlAttributeValueImpl
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiShortNamesCache
import com.intellij.psi.util.MethodSignatureUtil
import com.intellij.psi.util.PsiTreeUtil.findFirstParent
import com.intellij.psi.xml.XmlTag
import com.intellij.util.xml.ConvertContext
import com.intellij.util.xml.CustomReferenceConverter
import com.intellij.util.xml.GenericDomValue

/**
 * @author Nosov Aleksandr <nosovae.dev@gmail.com>
 */
class AttributeReferenceConverter : CustomReferenceConverter<String> {
    override fun createReferences(value: GenericDomValue<String>?, element: PsiElement, context: ConvertContext?): Array<PsiReference> {
        val reference = object : PsiReferenceBase<PsiElement>(element, true), PsiPolyVariantReference {
            override fun resolve(): PsiElement? {
                val resolveResults = multiResolve(false)
                return if (resolveResults.size == 1) resolveResults.first().element else null
            }

            override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
                val project = element.project
                val className = findItemTag(element).getAttributeValue("code")
                val psiElements = mutableListOf<PsiElement>()
                val searchFieldName = (element as XmlAttributeValueImpl).value

                if (className != null) {
                    arrayListOf(className, "${className}${HybrisConstants.MODEL_SUFFIX}").forEach { name ->
                        val psiClasses = PsiShortNamesCache.getInstance(project).getClassesByName(
                                name, GlobalSearchScope.allScope(project)
                        )
                        psiClasses.forEach { psiClass ->
                            val psiField = PsiClassImplUtil.findFieldByName(psiClass, searchFieldName.uppercase(), true)
                            if (psiField != null) psiElements.add(psiField)
                            val psiGetterMethod = findGetter(psiClass, searchFieldName, true)
                            if (psiGetterMethod != null) psiElements.add(psiGetterMethod)
                            val psiSetterMethod = findSetter(psiClass, searchFieldName, true)
                            if (psiSetterMethod != null) psiElements.add(psiSetterMethod)
                        }
                    }
                }

                return PsiElementResolveResult.createResults(psiElements)
            }

            override fun getVariants() = PsiReference.EMPTY_ARRAY
        }

        return arrayOf(reference)
    }

    private fun findItemTag(element: PsiElement) =
            findFirstParent(element, true) { e -> return@findFirstParent e is XmlTag && e.name == "itemtype" } as XmlTag
    
    private fun findGetter(psiClass: PsiClass, name: String, checkSuperClasses: Boolean): PsiMethod? {
        return if (!Comparing.strEqual(name, null as String?)) {
            val methodSignature = MethodSignatureUtil.createMethodSignature(suggestGetterName(name), PsiType.EMPTY_ARRAY, PsiTypeParameter.EMPTY_ARRAY, PsiSubstitutor.EMPTY)
            return MethodSignatureUtil.findMethodBySignature(psiClass, methodSignature, checkSuperClasses)
        } else {
            null
        }
    }
    
    private fun findSetter(psiClass: PsiClass, name: String, checkSuperClasses: Boolean): PsiMethod? {
        val methods = psiClass.findMethodsByName(suggestSetterName(name), checkSuperClasses)
        return if (methods.isEmpty()) null else methods.first()
    }

    private fun suggestGetterName(name: String) = "get${capitalize(name)}"
    private fun suggestSetterName(name: String) = "set${capitalize(name)}"
}