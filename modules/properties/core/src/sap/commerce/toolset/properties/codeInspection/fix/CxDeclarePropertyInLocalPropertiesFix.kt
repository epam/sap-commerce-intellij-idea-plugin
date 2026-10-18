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
import com.intellij.ide.util.PsiNavigationSupport
import com.intellij.lang.properties.IProperty
import com.intellij.lang.properties.psi.PropertiesFile
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiManager
import com.intellij.util.asSafely
import sap.commerce.toolset.properties.meta.CxPropertyCollector
import sap.commerce.toolset.properties.meta.CxPropertyScope

/**
 * Declares a key nothing in the chain declares yet, in `local.properties`, and opens it so a value can be typed.
 *
 * The value is deliberately left empty: what a missing property should be set to is the one thing this cannot know,
 * and guessing would be worse than an obvious blank.
 */
class CxDeclarePropertyInLocalPropertiesFix(private val key: String) : LocalQuickFix {

    override fun getFamilyName() = "Declare in local.properties"

    override fun getName() = "Declare '$key' in local.properties"

    // The change lands in another file than the one the problem is in, so the fix opens its own write command over it.
    override fun startInWriteAction() = false

    override fun generatePreview(project: Project, previewDescriptor: ProblemDescriptor) =
        IntentionPreviewInfo.Html("Adds <code>${StringUtil.escapeXmlEntities(key)}=</code> to local.properties")

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val localProperties = localProperties(project) ?: return
        val property = WriteCommandAction.writeCommandAction(project, localProperties.containingFile)
            .withName(familyName)
            .compute<IProperty, RuntimeException> {
                localProperties.findPropertyByKey(key) ?: localProperties.addProperty(key, "")
            }

        CxPropertyCollector.getInstance(project).resetCache()

        val file = localProperties.virtualFile ?: return
        val offset = property.psiElement.textRange.endOffset
        // Navigating opens an editor, which has no place inside the command that just wrote the file.
        ApplicationManager.getApplication().invokeLater({
            PsiNavigationSupport.getInstance().createNavigatable(project, file, offset).navigate(true)
        }, project.disposed)
    }

    companion object {

        /** Whether the chain has a `local.properties` to declare in - without one the fix is not worth offering. */
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
