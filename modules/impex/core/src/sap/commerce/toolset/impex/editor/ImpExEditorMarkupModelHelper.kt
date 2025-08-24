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

package sap.commerce.toolset.impex.editor

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.markup.HighlighterLayer
import sap.commerce.toolset.impex.highlighting.ImpExHighlighterColors

object ImpExEditorMarkupModelHelper {

    fun removeHighlighters(editor: Editor) = with(editor.markupModel) {
        allHighlighters
            .filter {
                it.textAttributesKey == ImpExHighlighterColors.VALUE_LINE_EVEN
                    || it.textAttributesKey == ImpExHighlighterColors.VALUE_LINE_ODD
                    || it.textAttributesKey == ImpExHighlighterColors.HEADER_LINE
            }
            .forEach { removeHighlighter(it) }
    }

    fun highlightValueLine(editor: Editor, valueLineIndex: Int, textOffset: Int) {
        val document = editor.document

        if (textOffset > document.textLength) return

        val lineNumber = document.getLineNumber(textOffset)

        if ((valueLineIndex + 1) % 2 == 0) {
            editor.markupModel.addLineHighlighter(ImpExHighlighterColors.VALUE_LINE_EVEN, lineNumber, HighlighterLayer.SYNTAX)
        } else {
            editor.markupModel.addLineHighlighter(ImpExHighlighterColors.VALUE_LINE_ODD, lineNumber, HighlighterLayer.SYNTAX)
        }
    }

    fun highlightHeaderLine(editor: Editor, textOffset: Int) {
        val document = editor.document
        if (textOffset > document.textLength) return

        val lineNumber = document.getLineNumber(textOffset)

        editor.markupModel.addLineHighlighter(ImpExHighlighterColors.HEADER_LINE, lineNumber, HighlighterLayer.SYNTAX)
    }
}