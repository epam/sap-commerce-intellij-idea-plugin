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

package sap.commerce.toolset.logging.ui

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
import java.awt.event.InputEvent
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.KeyStroke

/**
 * Logger input field with completion and inline filtering support.
 *
 * Typing updates the visible logger rows through [onFilterChanged]
 * and reopens completion when needed, including after deletions.
 * Applying a logger is intentionally separated from plain Enter:
 * Enter only interacts with the lookup (closes it if shown), while
 * ⌘+Enter on macOS or Ctrl+Enter on other platforms triggers
 * [onApplyLogger]. The split keeps the user from creating loggers
 * accidentally while filtering.
 *
 * The lookup also gets a custom advertisement at the bottom
 * describing the field-specific shortcuts. The platform's default
 * tips are suppressed by setting [AutoPopupController.NO_ADS] as
 * user data on the editor — `LookupImpl` checks this flag during
 * refresh and clears its advertiser when present, so our
 * `addAdvertisement` ends up as the only entry. The
 * `LookupImpl`-typed cast is still kept behind `asSafely` so a
 * future release that drops or replaces the class falls through
 * silently rather than crashing.
 *
 * The same shortcut summary is also exposed as a hover tooltip on
 * both the field and its editor's content component (HTML-formatted
 * so the three lines render as separate rows the way the platform
 * Swing tooltip handler expects).
 *
 * Matches the standard IntelliJ Lookup so navigation, item insertion
 * via Tab/click, and Esc-to-close all behave the way they do in code
 * completion elsewhere in the IDE.
 */
internal class LoggerNameTextField(
    project: Project,
    parentDisposable: Disposable,
    private val onApplyLogger: () -> Unit,
    onFilterChanged: (String) -> Unit = {},
) : TextFieldWithCompletion(
    project,
    CxLoggerNameCompletionProvider(project),
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
        editor.putUserData(AutoPopupController.NO_ADS, true)
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
                onApplyLogger()
            }
        })
        return editor
    }

    companion object {
        private const val ESC_KEYSTROKE = "Esc"

        private val FILTER_KEYSTROKE = KeymapUtil.getKeystrokeText(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0))

        private val APPLY_KEYSTROKE = KeymapUtil.getKeystrokeText(
            KeyStroke.getKeyStroke(
                KeyEvent.VK_ENTER,
                if (SystemInfo.isMac) InputEvent.META_DOWN_MASK else InputEvent.CTRL_DOWN_MASK,
            )
        )

        private val LOOKUP_AD_TEXT = "Press $FILTER_KEYSTROKE to filter out loggers · " +
            "Press $ESC_KEYSTROKE to close the suggestion list · " +
            "Press $APPLY_KEYSTROKE to apply a logger"

        private val TOOLTIP_TEXT = "<html>" +
            "Press $FILTER_KEYSTROKE to filter out loggers.<br>" +
            "Press $ESC_KEYSTROKE to close the suggestion list.<br>" +
            "Press $APPLY_KEYSTROKE to apply a logger." +
            "</html>"

        private fun isApplyShortcut(event: KeyEvent): Boolean {
            if (event.isShiftDown || event.isAltDown) return false
            return if (SystemInfo.isMac) event.isMetaDown && !event.isControlDown
            else event.isControlDown && !event.isMetaDown
        }
    }
}
