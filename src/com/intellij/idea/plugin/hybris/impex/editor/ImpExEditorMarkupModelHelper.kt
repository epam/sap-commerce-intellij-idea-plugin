/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.impex.editor

import com.intellij.idea.plugin.hybris.impex.highlighting.ImpexHighlighterColors
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.markup.HighlighterLayer

object ImpExEditorMarkupModelHelper {

    fun removeHighlighters(editor: Editor) {
        val markupModel = editor.markupModel

        markupModel.allHighlighters
            .filter { it.textAttributesKey == ImpexHighlighterColors.VALUE_LINE_EVEN || it.textAttributesKey == ImpexHighlighterColors.VALUE_LINE_ODD }
            .forEach { markupModel.removeHighlighter(it) }
    }

    fun highlightLine(
        it: Editor,
        valueLineIndex: Int,
        textOffset: Int
    ) {
        if (textOffset > it.document.textLength) return

        val lineNumber = it.document.getLineNumber(textOffset)

        if ((valueLineIndex + 1) % 2 == 0) {
            it.markupModel.addLineHighlighter(ImpexHighlighterColors.VALUE_LINE_EVEN, lineNumber, HighlighterLayer.SYNTAX)
        } else {
            it.markupModel.addLineHighlighter(ImpexHighlighterColors.VALUE_LINE_ODD, lineNumber, HighlighterLayer.SYNTAX)
        }
    }
}