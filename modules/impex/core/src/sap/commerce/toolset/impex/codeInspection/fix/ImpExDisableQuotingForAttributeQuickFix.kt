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

package sap.commerce.toolset.impex.codeInspection.fix

import com.intellij.codeInsight.intention.HighPriorityAction
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import sap.commerce.toolset.settings.state.ImpExQuoteStringExclusion
import sap.commerce.toolset.settings.yDeveloperSettings

class ImpExDisableQuotingForAttributeQuickFix(
    private val typeName: String,
    private val attributeName: String,
) : LocalQuickFix, HighPriorityAction {

    override fun getFamilyName() = "[y] Exclusion: quote value strings"
    override fun getName() = "Do not quote value strings for: '$typeName.$attributeName'"
    override fun generatePreview(project: Project, previewDescriptor: ProblemDescriptor): IntentionPreviewInfo = IntentionPreviewInfo.EMPTY

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        project.yDeveloperSettings.impexSettings = project.yDeveloperSettings.impexSettings.mutable()
            .apply { quoteStringExclusions.add(ImpExQuoteStringExclusion(typeName, attributeName)) }
            .immutable()
    }
}