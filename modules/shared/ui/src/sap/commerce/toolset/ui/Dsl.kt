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
package sap.commerce.toolset.ui

import com.intellij.icons.AllIcons
import com.intellij.ide.HelpTooltip
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.impl.ActionButton
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.util.PopupUtil
import com.intellij.ui.ContextHelpLabel
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.InlineBanner
import com.intellij.ui.UIBundle
import com.intellij.ui.components.ActionLink
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.builder.impl.DslComponentPropertyInternal
import com.intellij.util.MathUtil
import org.jetbrains.annotations.NonNls
import sap.commerce.toolset.Notifications
import java.awt.Dimension
import java.awt.datatransfer.StringSelection
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.io.Serial
import javax.swing.Icon
import javax.swing.JLabel

fun DialogWrapper.repackDialog() {
    invokeLater {
        peer.window?.pack()
    }
}

fun Row.nullableIntTextField(range: IntRange? = null, keyboardStep: Int? = null): Cell<JBTextField> {
    val result = cell(JBTextField())
        .validationOnInput {
            val value = it.text.toIntOrNull()
            when {
                value == null -> null
                range != null && value !in range -> error(UIBundle.message("please.enter.a.number.from.0.to.1", range.first, range.last))
                else -> null
            }
        }
    result.columns(COLUMNS_TINY)
    result.component.putClientProperty(DslComponentPropertyInternal.INT_TEXT_RANGE, range)

    keyboardStep?.let {
        result.component.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent?) {
                val increment: Int = when (e?.keyCode) {
                    KeyEvent.VK_UP -> keyboardStep
                    KeyEvent.VK_DOWN -> -keyboardStep
                    else -> return
                }

                var value = result.component.text.toIntOrNull()
                if (value != null) {
                    value += increment
                    if (range != null) {
                        value = MathUtil.clamp(value, range.first, range.last)
                    }
                    result.component.text = value.toString()
                    e.consume()
                }
            }
        })
    }
    return result
}

fun Row.copyLink(project: Project, label: String?, value: String, confirmationMessage: String = "Copied to clipboard"): Cell<ActionLink> {
    return link(value) {
        CopyPasteManager.getInstance().setContents(StringSelection(value))
        Notifications.create(NotificationType.INFORMATION, confirmationMessage, "")
            .hideAfter(10)
            .notify(project)
    }
        .comment(label)
        .applyToComponent {
            HelpTooltip()
                .setTitle("Click to copy to clipboard")
                .installOn(this)
        }
}

fun Panel.inlineBanner(message: String, status: EditorNotificationPanel.Status = EditorNotificationPanel.Status.Info, icon: Icon? = null) = row {
    inlineBanner(message, status, icon)
}
    .resizableRow()
    .topGap(TopGap.MEDIUM)

fun Row.inlineBanner(message: String, status: EditorNotificationPanel.Status = EditorNotificationPanel.Status.Info, icon: Icon? = null) = cell(
    InlineBanner(message, status)
        .showCloseButton(false)
        .setIcon(icon ?: status.icon)
)
    .align(Align.CENTER)
    .resizableColumn()

fun Row.contextHelp(
    icon: Icon = AllIcons.General.ContextHelp,
    description: String,
    title: String? = null
): Cell<JLabel> {
    val result = if (title == null) ContextHelpLabel.create(description)
    else ContextHelpLabel.create(title, description)

    result.icon = icon

    return cell(result)
}

fun Row.actionButton(
    action: AnAction, @NonNls actionPlace: String = ActionPlaces.UNKNOWN,
    sinkExtender: (DataSink) -> Unit = {},
): Cell<ActionButton> {
    val component = ActionButtonSink(action, action.templatePresentation.clone(), actionPlace, ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE, sinkExtender)

    return cell(component)
}

fun Row.actionsButton(
    vararg actions: AnAction,
    actionPlace: String = ActionPlaces.UNKNOWN,
    icon: Icon = AllIcons.General.GearPlain,
    title: String? = null,
    showDisabledActions: Boolean = true,
    sinkExtender: (DataSink) -> Unit = {},
): Cell<ActionButton> {
    val actionGroup = PopupActionGroup(arrayOf(*actions), title, icon, showDisabledActions)
    val presentation = actionGroup.templatePresentation.clone()
    val component = ActionButtonSink(actionGroup, presentation, actionPlace, ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE, sinkExtender)

    return cell(component)
}

private class ActionButtonSink(
    action: AnAction,
    presentation: Presentation?,
    place: String,
    minimumSize: Dimension,
    private val sinkExtender: (DataSink) -> Unit,
) : ActionButton(action, presentation, place, minimumSize), UiDataProvider {
    override fun uiDataSnapshot(sink: DataSink) {
        sinkExtender(sink)
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = 4700000688059262171L
    }
}

private class PopupActionGroup(
    private val actions: Array<AnAction>,
    private val title: String?,
    private val icon: Icon,
    private val showDisabledActions: Boolean,
) : ActionGroup(), DumbAware {
    init {
        isPopup = true
        templatePresentation.isPerformGroup = actions.isNotEmpty()
        templatePresentation.icon = icon
    }

    override fun getChildren(e: AnActionEvent?): Array<AnAction> = actions

    override fun actionPerformed(e: AnActionEvent) {
        val popup = JBPopupFactory.getInstance().createActionGroupPopup(
            title, this, e.dataContext,
            JBPopupFactory.ActionSelectionAid.MNEMONICS, showDisabledActions
        )
        PopupUtil.showForActionButtonEvent(popup, e)
    }
}
