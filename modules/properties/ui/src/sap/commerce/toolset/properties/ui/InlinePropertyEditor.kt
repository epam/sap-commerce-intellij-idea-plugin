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

package sap.commerce.toolset.properties.ui

import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.impl.ActionButton
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.properties.presentation.CxPropertyPresentation
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import java.io.Serial
import javax.swing.AbstractAction
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.KeyStroke

/**
 * Inline row editor for [CxPropertyList].
 *
 * Renders over a property row using the same column layout as [CxPropertyRenderer]: the key
 * occupies the left column as a read-only label, the value column hosts an editable
 * [JBTextField], and a pair of confirm/cancel action buttons replaces the row's edit/delete
 * icons. Enter on the value field — or click on the confirm button — fires [onApply];
 * Esc or the cancel button fires [onCancel].
 */
internal class InlinePropertyEditor(
    property: CxPropertyPresentation,
    private val onApply: (String) -> Unit,
    private val onCancel: () -> Unit,
) : JPanel() {

    private val keyLabel = object : JBLabel(property.key) {
        @Serial
        private val serialVersionUID: Long = -7967062521967237403L

        override fun getMinimumSize(): Dimension = Dimension(0, super.getMinimumSize().height)
        override fun getPreferredSize(): Dimension = Dimension(0, super.getPreferredSize().height)
    }.apply { toolTipText = property.key }

    private val valueField = JBTextField(property.value).apply {
        // Mirror the value column's flex behaviour: the GridBag weight decides the width.
        minimumSize = Dimension(0, preferredSize.height)
        preferredSize = Dimension(0, preferredSize.height)
        addActionListener { fireApply() }
    }

    init {
        layout = GridBagLayout()
        isOpaque = true
        // border + padding match CxPropertyRenderer's row metrics so the editor sits flush
        // with the surrounding rows
        border = JBUI.Borders.empty(VERTICAL_PADDING, HORIZONTAL_PADDING)

        val gap = JBUI.scale(COLUMN_GAP)

        add(keyLabel, GridBagConstraints().apply {
            gridx = 0; gridy = 0
            weightx = 0.5; weighty = 1.0
            fill = GridBagConstraints.BOTH
            insets = JBUI.insetsRight(gap / 2)
        })
        add(valueField, GridBagConstraints().apply {
            gridx = 1; gridy = 0
            weightx = 0.5; weighty = 1.0
            fill = GridBagConstraints.HORIZONTAL
            insets = JBUI.insets(0, gap / 2, 0, JBUI.scale(ACTION_LEFT_INSET))
        })

        val applyAction = object : AnAction("Apply", "Apply changes", HybrisIcons.Actions.FORWARD) {
            override fun actionPerformed(e: AnActionEvent) = fireApply()
        }
        val cancelAction = object : AnAction("Cancel", "Cancel edit", HybrisIcons.Actions.REMOVE) {
            override fun actionPerformed(e: AnActionEvent) = onCancel()
        }
        val applyButton = ActionButton(applyAction, applyAction.templatePresentation.clone(),
            ActionPlaces.UNKNOWN, ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE)
        val cancelButton = ActionButton(cancelAction, cancelAction.templatePresentation.clone(),
            ActionPlaces.UNKNOWN, ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE)

        add(applyButton, GridBagConstraints().apply {
            gridx = 2; gridy = 0
            weightx = 0.0; weighty = 1.0
            fill = GridBagConstraints.VERTICAL
            insets = JBUI.insetsLeft(JBUI.scale(ACTION_GAP))
        })
        add(cancelButton, GridBagConstraints().apply {
            gridx = 3; gridy = 0
            weightx = 0.0; weighty = 1.0
            fill = GridBagConstraints.VERTICAL
            insets = JBUI.insetsLeft(JBUI.scale(ACTION_GAP))
        })

        val inputMap = valueField.getInputMap(JComponent.WHEN_FOCUSED)
        val actionMap = valueField.actionMap
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancelInlineEdit")
        actionMap.put("cancelInlineEdit", object : AbstractAction() {
            @Serial
            private val serialVersionUID: Long = -3195086007452948897L

            override fun actionPerformed(e: ActionEvent) = onCancel()
        })
    }

    fun focusValueField() {
        valueField.requestFocusInWindow()
        valueField.selectAll()
    }

    private fun fireApply() {
        onApply(valueField.text)
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = 6853792617726419231L
        private const val VERTICAL_PADDING = 6
        private const val HORIZONTAL_PADDING = 12
        private const val COLUMN_GAP = 8
        private const val ACTION_GAP = 6
        private const val ACTION_LEFT_INSET = 12
    }
}
