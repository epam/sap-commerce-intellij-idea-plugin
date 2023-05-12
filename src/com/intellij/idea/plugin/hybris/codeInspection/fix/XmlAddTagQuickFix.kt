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

package com.intellij.idea.plugin.hybris.codeInspection.fix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.psi.util.PsiNavigateUtil
import com.intellij.openapi.project.Project
import com.intellij.psi.impl.source.xml.XmlTagImpl
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlTokenType

class XmlAddTagQuickFix(
    private val tagName: String,
    private val tagBody: String? = null,
    private val insertAfterTag: String? = null,
    private val attributes: Map<String, String>? = null
) : LocalQuickFix {

    override fun getFamilyName() = attributes
        ?.let { attributes.entries.joinToString(", ") { "'${it.key}'='${it.value}'" } }
        ?.let { message("hybris.inspections.fix.xml.AddTagWithAttributes", tagName, it) }
        ?: message("hybris.inspections.fix.xml.AddTag", tagName)

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val currentElement = descriptor.psiElement as? XmlTag ?: return

        val tagToInsert = currentElement.createChildTag(
            tagName,
            currentElement.namespace,
            tagBody,
            false
        )

        val insertedTag = insertAfterTag
            ?.let { currentElement.findFirstSubTag(it) }
            ?.let { subTag -> currentElement.addAfter(tagToInsert, subTag) as? XmlTag }
            ?: currentElement.addSubTag(tagToInsert, true)

        attributes?.forEach { insertedTag.setAttribute(it.key, it.value) }

        val psiElement = (insertedTag as? XmlTagImpl)
            ?.getChildren(TokenSet.create(XmlTokenType.XML_END_TAG_START))
            ?.firstOrNull()
            ?.psi
            ?: insertedTag

        PsiNavigateUtil.navigate(descriptor, psiElement)
    }
}
