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
package sap.commerce.toolset.cockpitNG.psi.provider

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.ProcessingContext
import sap.commerce.toolset.cockpitNG.psi.reference.CngEditorDefinitionReference

/*
Sample XML:

Spring Bean ref:
 - {workflowStartedTimeEditorRenderer}

Editor Definition ref:
 - com.hybris.cockpitng.editor.decoratededitor
 - com.WRONG.hybris.cockpitng.editor.instanteditor
 - com.hybris.cockpitng.editor.defaultdate

<editorArea:attribute qualifier="startTime"
              editor="com.hybris.cockpitng.editor.decoratededitor(com.WRONG.hybris.cockpitng.editor.instanteditor(com.hybris.cockpitng.editor.defaultdate), {workflowStartedTimeEditorRenderer})"
              readonly="true"
              label="collaboration.workflow.details.workflow.started.label">
</editorArea:attribute>
 */
class CngEditorDefinitionReferenceProvider : PsiReferenceProvider() {

    override fun getReferencesByElement(
        element: PsiElement, context: ProcessingContext
    ): Array<out PsiReference> = CachedValuesManager.getManager(element.project).getCachedValue(element) {
        val references = element.text
            .replace(regex, "")
            .split("(")
            .map { it.trim() }
            .mapNotNull {
                val offset = element.text.indexOf(it)
                if (offset == -1) return@mapNotNull null
                CngEditorDefinitionReference(element, TextRange.from(offset, it.length))
            }
            .toTypedArray()

        CachedValueProvider.Result.createSingleDependency(
            references,
            PsiModificationTracker.MODIFICATION_COUNT,
        )
    }

    companion object {
        private val regex = "(\\{)(.*?)(})|([,)\"])".toRegex()
    }
}