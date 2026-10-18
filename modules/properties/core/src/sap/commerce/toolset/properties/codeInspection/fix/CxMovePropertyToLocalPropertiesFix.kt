/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.properties.codeInspection.fix

import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.lang.properties.psi.PropertiesFile
import com.intellij.lang.properties.psi.Property
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.asSafely
import sap.commerce.toolset.properties.meta.CxPropertyCollector
import sap.commerce.toolset.properties.meta.CxPropertyScope

/**
 * Moves a declaration out of an extension and into `local.properties`, where a developer's own environment belongs.
 *
 * An existing declaration of the same key in `local.properties` is updated rather than duplicated, since two
 * declarations of one key in a single file only confuse the reader — the later one wins either way.
 */
class CxMovePropertyToLocalPropertiesFix : LocalQuickFix {

    override fun getFamilyName() = "Move to local.properties"

    // Two files change, so the fix opens its own write command over both rather than the platform's over the current one.
    override fun startInWriteAction() = false

    override fun generatePreview(project: Project, previewDescriptor: ProblemDescriptor): IntentionPreviewInfo {
        val key = propertyOf(previewDescriptor)?.key ?: return IntentionPreviewInfo.EMPTY

        return IntentionPreviewInfo.Html("Moves <code>${StringUtil.escapeXmlEntities(key)}</code> to local.properties")
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val property = propertyOf(descriptor) ?: return
        val key = property.key ?: return
        // The raw text holds the escapes of the file; setValue and addProperty escape again what they are given.
        val value = property.unescapedValue.orEmpty()
        val localProperties = localProperties(project) ?: return

        WriteCommandAction.writeCommandAction(project, property.containingFile, localProperties.containingFile)
            .withName(familyName)
            .run<RuntimeException> {
                localProperties.findPropertyByKey(key)
                    ?.setValue(value)
                    ?: localProperties.addProperty(key, value)

                property.delete()
            }

        CxPropertyCollector.getInstance(project).resetCache()
    }

    private fun propertyOf(descriptor: ProblemDescriptor) = PsiTreeUtil.getParentOfType(descriptor.psiElement, Property::class.java, false)
        ?: descriptor.psiElement.asSafely<Property>()

    companion object {

        /** Whether the chain has a `local.properties` to move to - without one the fix is not worth offering. */
        fun isAvailable(project: Project) = localProperties(project) != null

        private fun localProperties(project: Project) = CxPropertyCollector.getInstance(project)
            .collect()
            .sources
            .find { it.scope == CxPropertyScope.LOCAL }
            ?.file
            ?.let { PsiManager.getInstance(project).findFile(it) }
            ?.asSafely<PropertiesFile>()
    }
}
