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
package com.intellij.idea.plugin.hybris.acl.completion

import com.intellij.codeInsight.completion.*
import com.intellij.idea.plugin.hybris.acl.codeInsight.lookup.AclLookupElementFactory
import com.intellij.idea.plugin.hybris.acl.psi.AclFile
import com.intellij.idea.plugin.hybris.acl.psi.AclTypes
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.TokenType
import com.intellij.util.ProcessingContext

class AclCompletionContributor : CompletionContributor() {

    init {
        extend(
            CompletionType.BASIC,
            psiElement(TokenType.BAD_CHARACTER)
                .withParent(psiElement(PsiErrorElement::class.java).withParent(AclFile::class.java)),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet
                ) {
                    result.addElement(AclLookupElementFactory.buildUserRightsPasswordAware())
                    result.addElement(AclLookupElementFactory.buildUserRightsPasswordUnaware())
                }
            }
        )
        extend(
            CompletionType.BASIC,
            psiElement(AclTypes.FIELD_VALUE)
                .withParent(
                    psiElement(PsiErrorElement::class.java)
                        .withParent(psiElement(AclTypes.USER_RIGHTS_VALUE_GROUP_PERMISSION))
                )
                .inside(AclFile::class.java),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet
                ) {
                    result.addAllElements(AclLookupElementFactory.buildPermissions())
                }
            }
        )
    }
}
