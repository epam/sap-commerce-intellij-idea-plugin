/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
package com.intellij.idea.plugin.hybris.impex.assistance.event

import com.intellij.codeInsight.folding.impl.FoldingUtil
import com.intellij.codeInsight.highlighting.HighlightManager
import com.intellij.codeInsight.highlighting.HighlightManagerImpl
import com.intellij.codeInsight.highlighting.HighlightUsagesHandler
import com.intellij.idea.plugin.hybris.common.services.CommonIdeaService
import com.intellij.idea.plugin.hybris.impex.ImpexLanguage
import com.intellij.idea.plugin.hybris.impex.psi.ImpexFullHeaderParameter
import com.intellij.idea.plugin.hybris.impex.utils.ImpexPsiUtils
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.colors.EditorColors
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.editor.markup.RangeHighlighter
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.removeUserData
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiUtilBase
import com.intellij.util.concurrency.AppExecutorUtil

@Service
class ImpexHighlightingCaretListener : CaretListener {

    override fun caretAdded(e: CaretEvent) {}
    override fun caretRemoved(e: CaretEvent) {}

    override fun caretPositionChanged(e: CaretEvent) {
        if (CommonIdeaService.instance.isTypingActionInProgress()) return

        val editor = e.editor
        val project = editor.project ?: return
        if (project.isDisposed) return

        ReadAction
            .nonBlocking<List<PsiElement>> {
                if (PsiUtilBase.getLanguageInEditor(editor, project) !is ImpexLanguage) return@nonBlocking emptyList()

                ImpexPsiUtils.getHeaderOfValueGroupUnderCaret(editor)
                    ?.let { it as? ImpexFullHeaderParameter }
                    ?.let { listOf(it) }
                    ?: ImpexPsiUtils.getFullHeaderParameterUnderCaret(editor)
                        ?.valueGroups
                        ?.let { it.mapNotNull { ivg -> ivg.value } }
                    ?: emptyList()
            }
            .withDocumentsCommitted(project)
            .expireWhen { editor.isDisposed }
            .finishOnUiThread(ModalityState.defaultModalityState()) {
                it.takeIf { it.isNotEmpty() }
                    ?.map { psiElement ->  psiElement.textRange }
                    ?.filterNot { textRange -> FoldingUtil.isTextRangeFolded(editor, textRange) }
                    ?.let { textRanges -> highlightArea(editor, textRanges, project) }
                    ?: clearHighlightedArea(editor)
            }
            .submit(AppExecutorUtil.getAppExecutorService())
    }

    fun clearHighlightedArea(editor: Editor) {
        editor.removeUserData(CACHE_KEY)
            ?.let {
                editor.project
                    ?.let { project -> modifyHighlightedArea(editor, it, project, true) }
            }
    }

    private fun highlightArea(editor: Editor, values: List<TextRange>, project: Project) {
        if (isAlreadyHighlighted(editor, values)) return

        clearHighlightedArea(editor)

        modifyHighlightedArea(editor, values, project, false)

        editor.putUserData(CACHE_KEY, values)
    }

    private fun modifyHighlightedArea(
        editor: Editor,
        textRanges: List<TextRange>,
        project: Project,
        clear: Boolean
    ) {
        val highlightManager = HighlightManager.getInstance(project)
        removeInvalidRangeHighlighters(editor, highlightManager)

        // This list must be modifiable
        // https://hybris-integration.atlassian.net/browse/IIP-11
        textRanges
            // Do not use Collectors.toList() here because:
            // There are no guarantees on the type, mutability, serializability,
            // or thread-safety of the List returned; if more control over the
            // returned List is required, use toCollection(Supplier).
            .toMutableList()
            .takeIf { it.isNotEmpty() }
            ?.let {
                HighlightUsagesHandler.highlightRanges(
                    highlightManager,
                    editor,
                    EditorColors.SEARCH_RESULT_ATTRIBUTES,
                    clear,
                    it
                )
            }
    }

    /**
     * IIPS-174: It seems like sometimes when we highlight code inside "Code Preview Panel" in combination with OOTB
     * "unchanged lines" folding from IDEA it can end up in creating invalid highlighting ranges.
     * E.g. when you run an inspection for an impex file and in the results panel click on an inspection result
     * on the right it shows you a preview of a snippet from that file, and if you have multiple inspection warnings in
     * the same file when you click on them the preview panel jumps into different parts of the file, which leads to
     * creation of multiple highlight ranges while the editor stays the same, but OOTB folding messes everything up by
     * folding many lines as the result highlight ranges created for the first inspection have invalid start and end
     * offsets for the editor with folded lines when you click on some other inspection from the same file.
     */
    private fun removeInvalidRangeHighlighters(editor: Editor, highlightManager: HighlightManager) {
        if (highlightManager !is HighlightManagerImpl) return

        highlightManager.getHighlighters(editor)
            .filter { isNotProperRangeHighlighter(it) }
            .forEach { highlightManager.removeSegmentHighlighter(editor, it) }
    }

    /**
     * From [TextRange#isProperRange(int, int))][TextRange.isProperRange]
     */
    private fun isNotProperRangeHighlighter(rangeHighlighter: RangeHighlighter) = rangeHighlighter.startOffset > rangeHighlighter.endOffset
        || rangeHighlighter.startOffset < 0

    private fun isAlreadyHighlighted(editor: Editor, ranges: List<TextRange>) = editor.getUserData(CACHE_KEY) == ranges

    companion object {
        private val CACHE_KEY = Key.create<List<TextRange>>("IMPEX_COLUMN_HIGHLIGHT_CACHE")

        val instance: ImpexHighlightingCaretListener = ApplicationManager.getApplication().getService(ImpexHighlightingCaretListener::class.java)
    }

}