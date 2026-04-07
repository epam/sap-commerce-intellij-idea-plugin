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

package sap.commerce.toolset.impex.actionSystem

import com.intellij.codeInsight.folding.CodeFoldingManager
import com.intellij.codeInsight.folding.impl.actions.BaseFoldingHandler
import com.intellij.codeInsight.folding.impl.actions.ExpandAllRegionsAction
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.FoldRegion
import com.intellij.openapi.editor.actionSystem.EditorAction
import sap.commerce.toolset.impex.ImpExConstants

class ImpExCollapseAllRegionsAction : EditorAction(object : BaseFoldingHandler() {
    public override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        val project = editor.project
        checkNotNull(project)
        val activeCaret = caret
            ?: editor.caretModel.primaryCaret

        val codeFoldingManager = CodeFoldingManager.getInstance(project)
        val regions = if (activeCaret.hasSelection()) getFoldRegionsForSelection(editor, caret)
        else getFoldRegionsForSelection(editor, caret)
            .filterNot { region -> region.group?.toString()?.startsWith(ImpExConstants.Folding.GROUP_PREFIX) ?: false }

        ExpandAllRegionsAction.twoStepFoldToggling(
            editor,
            regions,
            { region -> collapseInFirstStep(codeFoldingManager, region) },
            false
        )
    }

    private fun collapseInFirstStep(codeFoldingManager: CodeFoldingManager, region: FoldRegion): Boolean {
        val expandedByDefault = codeFoldingManager.keepExpandedOnFirstCollapseAll(region) ?: false
        return region.isExpanded && !expandedByDefault
    }
})
