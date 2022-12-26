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

package com.intellij.idea.plugin.hybris.system.type.converter

import com.intellij.ide.highlighter.XmlFileType
import com.intellij.idea.plugin.hybris.project.utils.PluginCommon.SPRING_PLUGIN_ID
import com.intellij.idea.plugin.hybris.project.utils.PluginCommon.isPluginActive
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiSearchHelper
import com.intellij.psi.search.UsageSearchContext
import com.intellij.psi.xml.XmlAttribute
import com.intellij.util.xml.ConvertContext
import com.intellij.util.xml.CustomReferenceConverter
import com.intellij.util.xml.GenericDomValue

class AttributeHandlerReferenceConverter : CustomReferenceConverter<String>, ResolvingHint {

    override fun createReferences(value: GenericDomValue<String>, element: PsiElement, context: ConvertContext): Array<PsiReference>
            = if (isPluginActive(SPRING_PLUGIN_ID)) createSpringReferences(element, value) else createPlainXMLReference(element, value)

    private fun createPlainXMLReference(element: PsiElement, value: GenericDomValue<String>): Array<PsiReference> {
        val project = element.project

        val reference = object : PsiReferenceBase<PsiElement>(element, true), PsiPolyVariantReference {
            private val QUOTE_LENGTH = 2

            override fun getRangeInElement() = TextRange.from(1, element.textLength - QUOTE_LENGTH)

            override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
                val psiSearchHelper = PsiSearchHelper.getInstance(project)
                val module = ModuleUtil.findModuleForPsiElement(element)

                val foundEls = mutableListOf<PsiElement>()
                val searchText = value.stringValue!!.trim()

                psiSearchHelper.processElementsWithWord({ el, _ ->
                    if (el.containingFile.name.contains("-spring") && el is XmlAttribute && el.name == "id") foundEls.add(el)
                    true
                }, GlobalSearchScope.getScopeRestrictedByFileTypes(GlobalSearchScope.moduleScope(module!!), XmlFileType.INSTANCE), searchText, UsageSearchContext.ANY, true)

                return if (foundEls.isEmpty()) ResolveResult.EMPTY_ARRAY else PsiElementResolveResult.createResults(foundEls)
            }

            override fun resolve(): PsiElement? {
                val resolveResults = multiResolve(false)
                return if (resolveResults.size == 1) resolveResults[0].element else null
            }

            override fun getVariants() = PsiReference.EMPTY_ARRAY
        }


        return arrayOf(reference)
    }


    private fun createSpringReferences(element: PsiElement, value: GenericDomValue<String>): Array<PsiReference> {
        val project = element.project
        val name = value.stringValue!!.trim()

        val reference = object : PsiReferenceBase<PsiElement>(element, true), PsiPolyVariantReference {
            private val QUOTE_LENGTH = 2

            override fun getRangeInElement() = TextRange.from(1, element.textLength - QUOTE_LENGTH)

            override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
                val module = ModuleUtil.findModuleForPsiElement(element) ?: return ResolveResult.EMPTY_ARRAY

                val springModels = com.intellij.spring.SpringManager.getInstance(project).getAllModels(module)
                val pointer = findBean(springModels, name) ?: return ResolveResult.EMPTY_ARRAY

                pointer.beanClass ?: return ResolveResult.EMPTY_ARRAY

                return PsiElementResolveResult.createResults(pointer.beanClass)
            }

            override fun resolve(): PsiElement? {
                val resolveResults = multiResolve(false)
                return if (resolveResults.size == 1) resolveResults[0].element else null
            }

            override fun getVariants() = PsiReference.EMPTY_ARRAY
        }


        return arrayOf(reference)
    }

    private fun findBean(springModels: Set<com.intellij.spring.contexts.model.SpringModel>, name: String): com.intellij.spring.model.SpringBeanPointer<*>? {
        springModels.forEach { springModel ->
            val pointer = com.intellij.spring.model.utils.SpringModelSearchers.findBean(springModel, name)
            if (pointer != null) return pointer
        }
        return null
    }

    override fun canResolveTo(elementClass: Class<out PsiElement>) = !PsiDocCommentOwner::class.java.isAssignableFrom(elementClass)

}