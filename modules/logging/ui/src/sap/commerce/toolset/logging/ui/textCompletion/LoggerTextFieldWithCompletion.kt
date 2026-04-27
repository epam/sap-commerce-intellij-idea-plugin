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
import com.intellij.openapi.keymap.KeymapUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.SystemInfo
import com.intellij.util.textCompletion.TextFieldWithCompletion
import sap.commerce.toolset.i18n
import java.awt.event.InputEvent
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.io.Serial
import javax.swing.KeyStroke

/**
 * Logger input field with completion and inline filtering support.
 *
 * Typing updates the visible logger rows through [onFilterChanged] and reopens completion when needed,
 * including after deletions. Applying a logger is intentionally separated from plain Enter: Enter only
 * interacts with completion, while `Cmd+Enter` on macOS or `Ctrl+Enter` on other platforms triggers [apply]
 * to avoid creating loggers accidentally during filtering.
 */
internal class LoggerTextFieldWithCompletion(
    project: Project,
    parentDisposable: Disposable,
    val apply: () -> Unit,
    onFilterChanged: (String) -> Unit = {}
) : TextFieldWithCompletion(
    project,
    LoggerCompletionProvider(project),
    "",
    true,
    true,
    false,
    false,
) {
    init {
        toolTipText = TOOLTIP_TEXT

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
        editor.contentComponent.toolTipText = TOOLTIP_TEXT

        editor.contentComponent.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                if (e.keyCode != KeyEvent.VK_ENTER) return

                val lookup = LookupManager.getActiveLookup(editor)
                if (lookup != null && !isApplyShortcut(e)) {
                    e.consume()
                    lookup.hideLookup(false)
                    return
                }

                if (!isApplyShortcut(e)) return

                e.consume()
                apply()
            }
        })
        return editor
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = 117007143781896069L

        private const val CLOSE_SUGGESTIONS_SHORTCUT_LABEL = "Esc"

        private val FILTER_SHORTCUT_LABEL = KeymapUtil.getKeystrokeText(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0))
        private val APPLY_SHORTCUT_LABEL = KeymapUtil.getKeystrokeText(
            KeyStroke.getKeyStroke(
                KeyEvent.VK_ENTER,
                if (SystemInfo.isMac) InputEvent.META_DOWN_MASK else InputEvent.CTRL_DOWN_MASK,
            )
        )
        private val TOOLTIP_TEXT = i18n(
            "hybris.cx.loggers.logger.completion.component.tooltip",
            FILTER_SHORTCUT_LABEL,
            CLOSE_SUGGESTIONS_SHORTCUT_LABEL,
            APPLY_SHORTCUT_LABEL,
        )

        private fun isApplyShortcut(event: KeyEvent): Boolean {
            if (event.isShiftDown || event.isAltDown) return false

            return if (SystemInfo.isMac) event.isMetaDown && !event.isControlDown
            else event.isControlDown && !event.isMetaDown
        }
    }
}
