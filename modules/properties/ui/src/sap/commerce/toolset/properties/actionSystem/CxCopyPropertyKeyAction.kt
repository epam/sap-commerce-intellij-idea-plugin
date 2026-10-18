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

package sap.commerce.toolset.properties.actionSystem

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.DumbAwareAction
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.properties.presentation.CxPropertyPresentation
import java.awt.datatransfer.StringSelection

class CxCopyPropertyKeyAction : DumbAwareAction("Copy Property Name", "Copy the property name to the clipboard", HybrisIcons.Property.COPY) {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.getData(CxPropertyPresentation.DATA_KEY) != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        val property = e.getData(CxPropertyPresentation.DATA_KEY) ?: return

        CopyPasteManager.getInstance().setContents(StringSelection(property.key))
    }
}
