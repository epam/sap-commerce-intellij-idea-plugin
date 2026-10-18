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

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.project.Project

/** Opens the SAP Help Portal page the rule was transcribed from. */
class CxOpenPropertyDocumentationFix(private val documentationUrl: String) : LocalQuickFix {

    override fun getFamilyName() = "Open SAP Commerce Cloud documentation"

    override fun startInWriteAction() = false

    override fun availableInBatchMode() = false

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) = BrowserUtil.browse(documentationUrl)
}
