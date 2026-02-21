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

package sap.commerce.toolset.impex.editor.event

import com.intellij.codeInsight.folding.impl.FoldingUtil
import com.intellij.codeInsight.highlighting.HighlightManagerImpl
import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.colors.EditorColors
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.openapi.editor.markup.RangeHighlighter
import com.intellij.openapi.progress.checkCanceled
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.getOrCreateUserDataUnsafe
import com.intellij.util.application
import com.intellij.util.asSafely
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import sap.commerce.toolset.actionSystem.isTypingActionInProgress
import sap.commerce.toolset.impex.psi.ImpExFullHeaderParameter
import sap.commerce.toolset.impex.utils.ImpExPsiUtils
import java.util.concurrent.ConcurrentHashMap

@Service
class ImpExHighlightingCaretListener : CaretListener {

    override fun caretAdded(e: CaretEvent) {}
    override fun caretRemoved(e: CaretEvent) {}

    override fun caretPositionChanged(e: CaretEvent) {
        if (isTypingActionInProgress()) return

        val editor = e.editor
        val project = editor.project ?: return
        if (project.isDisposed) return

        var highlightJob = editor.getUserData(KEY_JOB_CACHE)

        highlightJob?.cancel()
        highlightJob = CoroutineScope(Dispatchers.Default).launch {
            if (project.isDisposed) return@launch

            val elements = readAction {
                ImpExPsiUtils.getHeaderOfValueGroupUnderCaret(editor)
                    ?.asSafely<ImpExFullHeaderParameter>()
                    ?.let { listOf(it) }
                    ?: ImpExPsiUtils.getFullHeaderParameterUnderCaret(editor)
                        ?.valueGroups
                        ?.let { it.mapNotNull { ivg -> ivg.value } }
                    ?: emptyList()
            }

            val ranges = elements.takeIf { it.isNotEmpty() }
                ?.map { it.textRange }
                ?.toSet()
                ?: emptySet()
            ranges
                .filterNot { textRange -> FoldingUtil.isTextRangeFolded(editor, textRange) }
                .let { textRanges -> highlightArea(editor, textRanges) }

            cleanup(editor, ranges)
        }
        editor.putUserData(KEY_JOB_CACHE, highlightJob)
    }

    private suspend fun cleanup(editor: Editor, ranges: Set<TextRange>) {
        val markupModel = editor.markupModel
        val cache = editor.getOrCreateUserDataUnsafe(KEY_CACHE) { ConcurrentHashMap() }

        val clear = cache.keys.filterNot { ranges.contains(it) }
        clear.forEach { highlighter ->
            checkCanceled()
            cache.remove(highlighter)?.let { markupModel.removeHighlighter(it) }
        }
    }

    private suspend fun highlightArea(editor: Editor, textRangesToHighlight: Collection<TextRange>) {
        val markupModel = editor.markupModel
        val cache = editor.getOrCreateUserDataUnsafe(KEY_CACHE) { ConcurrentHashMap() }

        textRangesToHighlight.forEach { range ->
            checkCanceled()

            cache.computeIfAbsent(range) {
                markupModel.addRangeHighlighter(
                    EditorColors.IDENTIFIER_UNDER_CARET_ATTRIBUTES, range.startOffset, range.endOffset,
                    HighlightManagerImpl.OCCURRENCE_LAYER,
                    HighlighterTargetArea.EXACT_RANGE
                )
            }
        }
    }

    companion object {
        private val KEY_JOB_CACHE = Key.create<Job>("impex.highlighting.highlighting.caret")
        private val KEY_CACHE = Key.create<MutableMap<TextRange, RangeHighlighter>>("IMPEX_COLUMN_HIGHLIGHT_CACHE_")

        fun getInstance(): ImpExHighlightingCaretListener = application.service()
    }
}
