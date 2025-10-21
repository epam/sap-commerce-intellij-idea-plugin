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
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.impl.ActionButton
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.util.PopupUtil
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.Row
import org.jetbrains.annotations.NonNls
import java.awt.Dimension
import java.io.Serial
import javax.swing.Icon

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
