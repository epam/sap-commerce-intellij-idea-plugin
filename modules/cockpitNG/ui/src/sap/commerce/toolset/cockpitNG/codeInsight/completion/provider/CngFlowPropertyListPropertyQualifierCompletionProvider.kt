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
package sap.commerce.toolset.cockpitNG.codeInsight.completion.provider

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.JavaLookupElementBuilder
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.ProcessingContext
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.cockpitNG.psi.CngPsiHelper
import sap.commerce.toolset.cockpitNG.psi.JavaPsiHelper
import sap.commerce.toolset.typeSystem.codeInsight.completion.provider.AttributeDeclarationCompletionProvider

class CngFlowPropertyListPropertyQualifierCompletionProvider : AttributeDeclarationCompletionProvider() {

    override fun resolveType(element: PsiElement) = CngPsiHelper.resolveContextTypeForNewItemInWizardFlow(element)

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val project = parameters.editor.project ?: return
        val type = resolveType(parameters.position) ?: return

        if (type.contains(".")
            && type != HybrisConstants.COCKPIT_NG_INITIALIZE_CONTEXT_TYPE
        ) addJavaPojoCompletions(project, type, result)
        else super.addCompletions(parameters, context, result)
    }

    private fun addJavaPojoCompletions(project: Project, className: String, result: CompletionResultSet) {
        JavaPsiFacade.getInstance(project)
            .findClass(className, GlobalSearchScope.allScope(project))
            ?.let { psiClass ->
                val fields = psiClass.allFields

                return@let if (psiClass.isRecord) fields.toList()
                else fields
                    .filter {
                        val targetClass = it.containingClass ?: return@filter false
                        JavaPsiHelper.hasGetter(targetClass, it) && JavaPsiHelper.hasSetter(targetClass, it)
                    }
            }
            ?.map {
                JavaLookupElementBuilder.forField(it, it.name, it.containingClass)
                    .withTypeText(it.type.presentableText, true)
            }
            ?.forEach { result.addElement(it) }
    }

}