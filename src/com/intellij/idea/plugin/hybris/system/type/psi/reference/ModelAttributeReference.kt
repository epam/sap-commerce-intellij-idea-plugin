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

package com.intellij.idea.plugin.hybris.system.type.psi.reference

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess
import com.intellij.openapi.util.Comparing
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.*
import com.intellij.psi.impl.PsiClassImplUtil
import com.intellij.psi.impl.source.xml.XmlAttributeValueImpl
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiShortNamesCache
import com.intellij.psi.util.MethodSignatureUtil
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag

class ModelAttributeReference(
    element: PsiElement
) : PsiReferenceBase<PsiElement>(element, true), PsiPolyVariantReference {

    private val booleanType = "boolean"
    private val itemClassPostfix = "Model"
    private val checkSuperClasses = true

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
                    val psiGetterMethod = findGetter(psiClass, searchFieldName)
                    if (psiGetterMethod != null) psiElements.add(psiGetterMethod)
                    val psiSetterMethod = findSetter(psiClass, searchFieldName)
                    if (psiSetterMethod != null) psiElements.add(psiSetterMethod)
                }
            }
        }

        return PsiElementResolveResult.createResults(psiElements)
    }

    override fun getVariants(): Array<PsiReference> = PsiReference.EMPTY_ARRAY

    private fun findItemTag(element: PsiElement) =
        PsiTreeUtil.findFirstParent(element, true) { e -> return@findFirstParent e is XmlTag && e.name == "itemtype" } as XmlTag

    private fun findGetter(psiClass: PsiClass, propertyName: String): PsiMethod? {
        val booleanProperty = isBooleanProperty(psiClass, propertyName)
        return if (booleanProperty)
            findGetter(propertyName, psiClass, "is${StringUtil.capitalize(propertyName)}")
        else
            findGetter(propertyName, psiClass, "get${StringUtil.capitalize(propertyName)}")
    }

    private fun isBooleanProperty(psiClass: PsiClass, name: String): Boolean {
        val jaloClassName = jaloClassName(psiClass)
        return TSMetaModelAccess.getInstance(element.project)
            .findMetaItemByName(jaloClassName)
            ?.attributes
            ?.map { it.value }
            ?.any { it.name == name && it.type == booleanType } ?: false
    }

    private fun jaloClassName(psiClass: PsiClass) = if (psiClass.name?.contains(itemClassPostfix)!!) psiClass.name?.replace(itemClassPostfix, "") else psiClass.name

    private fun findGetter(name: String, psiClass: PsiClass, getterName: String): PsiMethod? {
        return if (!Comparing.strEqual(name, null as String?)) {
            val methodSignature = MethodSignatureUtil.createMethodSignature(
                getterName,
                PsiType.EMPTY_ARRAY,
                PsiTypeParameter.EMPTY_ARRAY,
                PsiSubstitutor.EMPTY
            )
            return MethodSignatureUtil.findMethodBySignature(psiClass, methodSignature, checkSuperClasses)
        } else {
            null
        }
    }

    private fun findSetter(psiClass: PsiClass, name: String): PsiMethod? {
        val methods = psiClass.findMethodsByName(suggestSetterName(name), checkSuperClasses)
        return if (methods.isEmpty()) null else methods.first()
    }

    private fun suggestSetterName(name: String) = "set${StringUtil.capitalize(name)}"
}