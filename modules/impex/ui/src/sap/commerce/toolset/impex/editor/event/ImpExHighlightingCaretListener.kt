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

package sap.commerce.toolset.impex.editor.event

import com.intellij.codeInsight.folding.impl.FoldingUtil
import com.intellij.codeInsight.highlighting.HighlightManager
import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.colors.EditorColors
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.editor.markup.RangeHighlighter
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.getOrCreateUserDataUnsafe
import com.intellij.util.application
import com.intellij.util.asSafely
import kotlinx.coroutines.*
import sap.commerce.toolset.actionSystem.isTypingActionInProgress
import sap.commerce.toolset.impex.psi.ImpExFullHeaderParameter
import sap.commerce.toolset.impex.utils.ImpExPsiUtils
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.cancellation.CancellationException

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
        val singleFlight = SingleFlight<TextRange, RangeHighlighter>(CoroutineScope(Dispatchers.IO))

        highlightJob?.cancel()
        highlightJob =
            CoroutineScope(Dispatchers.Default).launch {
                println("starting highlight")
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

                elements.takeIf { it.isNotEmpty() }
                    ?.map { it.textRange }
                    ?.filterNot { textRange -> FoldingUtil.isTextRangeFolded(editor, textRange) }
                    ?.toMutableList()
                    ?.let { textRanges -> highlightArea(editor, textRanges, project, singleFlight) }
                    ?: clearHighlightedArea(editor)
                println("completed highlight")
            }
        highlightJob.invokeOnCompletion { throwable ->
            if (throwable is CancellationException) {
                println("cancelled: ${throwable.message}")
            }
        }
        editor.putUserData(KEY_JOB_CACHE, highlightJob)
    }

    fun clearHighlightedArea(editor: Editor) {
        val project = editor.project ?: return
        val cache = editor.getOrCreateUserDataUnsafe(KEY_H_CACHE) { ConcurrentHashMap() }

        val highlightManager = HighlightManager.getInstance(project)
        val ranges = cache.toMap()
        ranges.forEach { (range, highlighter) ->
            cache.remove(range)
            highlightManager.removeSegmentHighlighter(editor, highlighter)
        }

        println("cleared ${ranges.size}")
    }

    private suspend fun highlightArea(editor: Editor, textRangesToHighlight: MutableList<TextRange>, project: Project, singleFlight: SingleFlight<TextRange, RangeHighlighter>) {
        val cache = editor.getOrCreateUserDataUnsafe(KEY_H_CACHE) { ConcurrentHashMap() }

        val highlightManager = HighlightManager.getInstance(project)

        val clear = cache
            .filterNot { textRangesToHighlight.contains(it.key) }
        clear
            .forEach { (range, highlighter) ->
                cache.remove(range)
                highlightManager.removeSegmentHighlighter(editor, highlighter)
            }

        println("cleared ${clear.size}")

        val add = textRangesToHighlight
            .filterNot { cache.contains(it) }

        println("added ${add.size}")

        add
            .forEach { range ->
                val x = mutableSetOf<RangeHighlighter>()
                cache[range] = singleFlight.execute(range) {
                    highlightManager.addRangeHighlight(editor, range.startOffset, range.endOffset, EditorColors.SEARCH_RESULT_ATTRIBUTES, false, x)
                    x.last()
                }
            }
    }

    companion object {
        private val KEY_JOB_CACHE = Key.create<Job>("impex.highlighting.highlighting.caret")
        private val KEY_H_CACHE = Key.create<MutableMap<TextRange, RangeHighlighter>>("IMPEX_COLUMN_HIGHLIGHT_CACHE_")

        fun getInstance(): ImpExHighlightingCaretListener = application.service()
    }


    class SingleFlight<K, V>(
        private val scope: CoroutineScope
    ) {
        private val inFlight = ConcurrentHashMap<K, Deferred<V>>()

        suspend fun execute(key: K, block: suspend () -> V): V {
            val deferred = inFlight.computeIfAbsent(key) {
                scope.async(start = CoroutineStart.LAZY) {
                    try {
                        block()
                    } finally {
                        inFlight.remove(key)
                    }
                }
            }

            return deferred.await()
        }
    }
}