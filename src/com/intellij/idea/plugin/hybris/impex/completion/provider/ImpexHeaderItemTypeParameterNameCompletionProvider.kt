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

package com.intellij.idea.plugin.hybris.impex.completion.provider

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.impex.psi.ImpexAnyHeaderParameterName
import com.intellij.idea.plugin.hybris.impex.psi.ImpexFullHeaderParameter
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaItemService
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaModelAccess
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import com.intellij.util.ProcessingContext
import org.apache.commons.lang3.Validate
import java.util.*

/**
 * @author Nosov Aleksandr <nosovae.dev@gmail.com>
 */
class ImpexHeaderItemTypeParameterNameCompletionProvider : CompletionProvider<CompletionParameters>() {

    public override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            result: CompletionResultSet
    ) {
        Validate.notNull(parameters)
        Validate.notNull(result)

        val project = this.getProject(parameters) ?: return

        val psiElementUnderCaret = parameters.position
        val typeName = findItemTypeReference(psiElementUnderCaret)

        if (typeName.isNotBlank()) {
            fillDomAttributesCompletions(project, typeName, result)
        }
    }

    private fun fillDomAttributesCompletions(
            project: Project,
            typeName: String,
            resultSet: CompletionResultSet
    ) {
        val metaService = TSMetaModelAccess.getInstance(project)

        val metaItem = metaService.findMetaItemByName(typeName)
        if (metaItem == null) {
            val metaEnum = metaService.findMetaEnumByName(typeName)
            if (metaEnum != null) {
                resultSet.addElement(LookupElementBuilder.create("code").withIcon(HybrisIcons.TYPE_SYSTEM))
            }
        } else {
            val metaItemService = TSMetaItemService.getInstance(project)
            metaItemService.getAttributes(metaItem, true)
                    .map { prop ->
                        val name = prop.name
                        val builder = LookupElementBuilder
                                .create(name)
                                .withIcon(HybrisIcons.TYPE_SYSTEM)
                                .withStrikeoutness(prop.isDeprecated)
                        val typeText = getTypePresentableText(prop.type)
                        if (StringUtil.isEmpty(typeText)) builder else builder.withTypeText(typeText, true)
                    }
                    .filter { Objects.nonNull(it) }
                    .forEach { resultSet.addElement(it) }

            metaItemService.getRelationEnds(metaItem, true)
                    .map { ref -> LookupElementBuilder.create(ref.qualifier).withIcon(HybrisIcons.TYPE_SYSTEM) }
                    .forEach { resultSet.addElement(it) }

        }


    }

    private fun getProject(parameters: CompletionParameters): Project? {
        Validate.notNull(parameters)

        return parameters.editor.project
    }

    private fun findItemTypeReference(element: PsiElement): String {
        val parent = PsiTreeUtil.getParentOfType(element, ImpexFullHeaderParameter::class.java)
        val parameterName = PsiTreeUtil.findChildOfType(parent, ImpexAnyHeaderParameterName::class.java)
        if (parameterName != null) {
            val references = parameterName.references
            if (references.isNotEmpty()) {
                val reference = references.first().resolve()
                return obtainTypeName(reference)
            }
        }
        return ""
    }

    private fun obtainTypeName(reference: PsiElement?): String {
        val typeTag = PsiTreeUtil.findFirstParent(reference, { value -> value is XmlTag })
        return (typeTag as XmlTag).attributes.first { it.name == "type" }.value!!
    }

    companion object {

        val instance: CompletionProvider<CompletionParameters>
            get() = ApplicationManager.getApplication().getService(ImpexHeaderItemTypeParameterNameCompletionProvider::class.java)

        private fun getTypePresentableText(type: String?): String {
            if (type == null) {
                return ""
            }
            val index = type.lastIndexOf('.')
            return if (index >= 0) type.substring(index + 1) else type
        }
    }
}
