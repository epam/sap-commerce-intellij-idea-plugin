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

package sap.commerce.toolset.logging.ui.textCompletion

import com.intellij.codeInsight.AutoPopupController
import com.intellij.codeInsight.lookup.LookupManager
import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.psi.search.PsiShortNamesCache
import com.intellij.util.textCompletion.TextFieldWithCompletion
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.io.Serial

/**
 * Text field that serves a dual purpose: the user can type a logger name and
 * press Enter (or click the associated Apply Logger button) to add it, and as
 * they type the rendered logger list below is filtered by a case-insensitive
 * substring match via [onFilterChanged].
 *
 * The field is a [TextFieldWithCompletion] wired to a [sap.commerce.toolset.logging.ui.textCompletion.LoggerCompletionProvider]
 * that queries [PsiShortNamesCache] on demand for each popup refresh. There is
 * no upfront enumeration — resolution happens lazily, prefix-filtered via the
 * index, and stops as soon as [LoggerCompletionProvider.Companion.MAX_VISIBLE_SUGGESTIONS] matches are collected.
 * Opening the panel is free; typing is bounded by the cap, not by project size.
 *
 * Suggestions only appear after the user has typed at least
 * [LoggerCompletionProvider.Companion.MIN_PREFIX_LENGTH] characters. When more matches exist than the cap, a
 * single non-selectable hint row (`… N more matches — refine search`) is
 * appended to prompt the user to narrow their prefix. True Search-Everywhere
 * style pagination isn't feasible via the lookup API — see the comment on
 * [sap.commerce.toolset.logging.ui.textCompletion.LoggerCompletionProvider] for the rationale.
 *
 * The blank-text validation only fires on apply (via `validateAll()` in the
 * caller's apply handler), so it never flashes a red ring during filtering.
 *
 * Enter-to-apply is wired through a key listener on the editor's content
 * component and is intentionally suppressed while the completion lookup is
 * showing, so pressing Enter in the popup picks the highlighted suggestion
 * instead of applying a partial string.
 */
internal class LoggerTextFieldWithCompletion(
    project: Project,
    parentDisposable: Disposable,
    val apply: () -> Unit,
    onFilterChanged: (String) -> Unit = {}
) : TextFieldWithCompletion(
    project,
    LoggerCompletionProvider(project),
    /* value = */ "",
    /* oneLineMode = */ true,
    /* autoPopup = */ true,
    /* forceAutoPopup = */ false,
    /* showHint = */ true,
) {
    init {
        // Document listener on the editor document drives two things on every
        // keystroke (including backspace and other deletions):
        //
        // 1. The row-filter for the logger list below — onFilterChanged flips
        //    AtomicBooleanProperty values, cheap.
        //
        // 2. Re-opening the completion popup if it's not currently showing.
        //    The platform's CharFilter path reopens the popup when the user
        //    types a character, but it has no notion of deletions — so after
        //    the user dismisses the popup (Escape, click away, etc.) and then
        //    backspaces, nothing would bring the popup back until the next
        //    typed character. For a dedicated picker field this is annoying:
        //    users expect suggestions to track the current text continuously.
        //    scheduleAutoPopup respects the platform's auto-popup delay and
        //    is idempotent when a lookup is already active, so the guard
        //    below is a courtesy, not a correctness requirement.
        val docListener = object : DocumentListener {
            override fun documentChanged(event: DocumentEvent) {
                onFilterChanged(text)

                val editor = editor ?: return
                if (LookupManager.getActiveLookup(editor) != null) return
                AutoPopupController.getInstance(project).scheduleAutoPopup(editor)
            }
        }
        document.addDocumentListener(docListener, parentDisposable)
        Disposer.register(parentDisposable) { document.removeDocumentListener(docListener) }
    }

    override fun createEditor(): EditorEx {
        val editor = super.createEditor()
        // Enter-to-apply. Suppressed while the completion lookup is
        // visible so Enter there picks the highlighted suggestion
        // instead of applying a partial string.
        editor.contentComponent.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                if (e.keyCode != KeyEvent.VK_ENTER) return
                if (e.isShiftDown || e.isControlDown || e.isAltDown || e.isMetaDown) return
                if (LookupManager.getActiveLookup(editor) != null) return

                e.consume()
                apply()
            }
        })
        return editor
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = 117007143781896069L
    }
}