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

package sap.commerce.toolset.impex.actionSystem

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.actionSystem.ex.CustomComponentAction
import com.intellij.openapi.actionSystem.impl.ActionButton
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.Disposer
import com.intellij.ui.GotItTooltip
import sap.commerce.toolset.GotItTooltips
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.actionSystem.triggerAction
import sap.commerce.toolset.i18n
import sap.commerce.toolset.impex.editor.impexSplitEditorEx
import java.awt.event.HierarchyEvent

class ImpExToggleRowModeAction : ToggleAction(
    i18n("hybris.actions.in_editor.row_mode"),
    i18n("hybris.actions.in_editor.row_mode.description"),
    HybrisIcons.ImpEx.Actions.ROW_MODE
), DumbAware, CustomComponentAction {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun isSelected(e: AnActionEvent): Boolean = e.impexSplitEditorEx()?.rowMode
        ?: false

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        e.impexSplitEditorEx()?.rowMode = state
    }

    override fun createCustomComponent(presentation: Presentation, place: String) = ActionButton(this, presentation, place, ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE)
        .also { component ->
            val disposable = Disposer.newDisposable()
            component.addHierarchyListener { e ->
                if (e.changeFlags and HierarchyEvent.DISPLAYABILITY_CHANGED.toLong() != 0L && !component.isDisplayable) {
                    Disposer.dispose(disposable)
                }
            }
            GotItTooltip(
                id = GotItTooltips.ImpEx.ROW_MODE,
                textSupplier = {
                    """
                    Display the ${icon(HybrisIcons.ImpEx.Actions.ROW_MODE)} ${code("value line")} under the caret as a list of fields, one for each column, next to the editor.
                    <br><br>Every field edits the corresponding value of the file in place and supports ${code("code completion")}, ${code("navigation")} and ${code("validation")} as the ImpEx editor does.
                    <br>The header type, the sub type and the column names lead to their declarations in the ${code("Type System")}.
                    <br><br>Place the caret on any value line to see its values, multiple carets are not supported.
                    <br><br>Whether the row mode is enabled for the newly opened editors can be configured in the ${
                        link("ImpEx settings") {
                            DataManager.getInstance().getDataContext(component).getData(CommonDataKeys.PROJECT)
                                ?.triggerAction("hybris.impex.openSettings")
                        }
                    }.
                """.trimIndent()
                },
                parentDisposable = disposable
            )
                .withHeader("Row mode for ImpEx!")
                .show(component, GotItTooltip.BOTTOM_MIDDLE)
        }
}
