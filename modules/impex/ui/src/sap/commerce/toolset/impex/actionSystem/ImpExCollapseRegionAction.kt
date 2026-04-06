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

import com.intellij.codeInsight.folding.impl.FoldingUtil
import com.intellij.codeInsight.folding.impl.actions.BaseFoldingHandler
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.FoldRegion
import com.intellij.openapi.editor.actionSystem.EditorAction
import sap.commerce.toolset.impex.ImpExConstants

internal class ImpExCollapseRegionAction : EditorAction(object : BaseFoldingHandler() {
    public override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        val lines = editor.caretModel.allCarets
            .map { it.logicalPosition }
            .map { it.line }

        val processor = Runnable {
            for (line in lines) {
                val region = FoldingUtil.findFoldRegionStartingAtLine(editor, line)
                if (region != null && region.isExpanded && !region.isColumnValue()) {
                    region.isExpanded = false
                } else {
                    val offset = editor.caretModel.offset
                    val regions = FoldingUtil.getFoldRegionsAtOffset(editor, offset)
                    for (region1 in regions) {
                        if (region1.isExpanded && !region1.isColumnValue()) {
                            region1.isExpanded = false
                            break
                        }
                    }
                }
            }
        }
        editor.foldingModel.runBatchFoldingOperation(processor)
    }

    private fun FoldRegion.isColumnValue(): Boolean = this.group
        ?.toString()
        ?.startsWith(ImpExConstants.Folding.GROUP_PREFIX)
        ?.let { this.placeholderText.startsWith(ImpExConstants.Folding.VALUE_PREFIX) }
        ?: false
})

