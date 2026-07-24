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

package sap.commerce.toolset.flexibleSearch.actionSystem

import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.*
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.asSafely
import com.intellij.util.ui.JBUI
import sap.commerce.toolset.*
import sap.commerce.toolset.flexibleSearch.FlexibleSearchConstants
import sap.commerce.toolset.flexibleSearch.editor.flexibleSearchSplitEditor
import sap.commerce.toolset.flexibleSearch.exec.FlexibleSearchExecConstants
import sap.commerce.toolset.flexibleSearch.psi.FlexibleSearchPsiFile
import sap.commerce.toolset.scratch.createScratchFile
import sap.commerce.toolset.transform.TransformationResult
import sap.commerce.toolset.transform.Transformer
import java.awt.datatransfer.StringSelection
import java.awt.event.KeyEvent

class FlexibleSearchExportAction : AnAction() {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) = e.ifNotFromSearchPopup {
        val psiFile = e.getData(CommonDataKeys.PSI_FILE).asSafely<FlexibleSearchPsiFile>()
            ?: return@ifNotFromSearchPopup
        val canTransform = Transformer.EP.extensionList.any { it.isApplicable(psiFile) }
        if (!canTransform) {
            e.presentation.isEnabledAndVisible = false
            return@ifNotFromSearchPopup
        }

        e.presentation.text = i18n("hybris.fxs.actions.export")
        e.presentation.description = i18n("hybris.fxs.actions.export.description")
        val isDumb = e.project?.let { DumbService.isDumb(it) } ?: false
        e.presentation.icon = HybrisIcons.FlexibleSearch.Actions.EXPORT
        e.presentation.isEnabled = !isDumb
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.flexibleSearchSplitEditor() ?: return
        val execResult = editor.lastExecResult
        val hasData = execResult?.hasDataRows == true
        val rows = execResult?.rows ?: emptyList()

        val psiFile = e.getData(CommonDataKeys.PSI_FILE)?.asSafely<FlexibleSearchPsiFile>() ?: return
        val applicableTransformers = Transformer.EP.extensionList.filter { it.isApplicable(psiFile) }
        if (applicableTransformers.isEmpty()) return

        val inputEvent = e.inputEvent ?: return

        var includeTypeSystemUnique = true
        var includeData = hasData
        var selectedTransformerIndex = 0
        lateinit var myPopup: JBPopup

        val exportPanel = panel {
            row {
                checkBox(i18n("hybris.fxs.actions.export.dialog.include_type_unique"))
                    .bindSelected({ includeTypeSystemUnique }, { includeTypeSystemUnique = it })
            }
            row {
                checkBox(i18n("hybris.fxs.actions.export.dialog.include_data"))
                    .bindSelected({ includeData }, { includeData = it })
                    .enabled(hasData)
                    .comment(
                        if (hasData) i18n("hybris.fxs.actions.export.dialog.include_data.has_result", rows.size)
                        else i18n("hybris.fxs.actions.export.dialog.include_data.no_result")
                    )
            }

            row(i18n("hybris.fxs.actions.export.dialog.transformer")) {
                val cell = comboBox(applicableTransformers.map { it.name })
                cell.component.addActionListener {
                    selectedTransformerIndex = cell.component.selectedIndex.coerceAtLeast(0)
                }
            }
            separator()

            row {
                if (hasData) {
                    button(i18n("hybris.fxs.actions.export.dialog.clear_data")) {
                        editor.clearExecutionResult()
                        myPopup.cancel()
                    }
                }
                button(i18n("hybris.fxs.actions.export.dialog.export")) {
                    myPopup.closeOk(null)
                }.align(AlignX.RIGHT)
            }
        }.apply {
            border = JBUI.Borders.empty(8, 16)
        }

        JBPopupFactory.getInstance()
            .createComponentPopupBuilder(exportPanel, exportPanel.preferredFocusedComponent)
            .setMovable(false)
            .setResizable(false)
            .setRequestFocus(true)
            .setTitle(i18n("hybris.fxs.actions.export.dialog.title"))
            .setTitleIcon(ActiveIcon(HybrisIcons.FlexibleSearch.Actions.EXPORT))
            .setKeyEventHandler {
                val enterKey = it.keyCode == KeyEvent.VK_ENTER
                if (enterKey) myPopup.closeOk(it)
                enterKey
            }
            .createPopup()
            .also { popup ->
                myPopup = popup
                popup.addListener(object : JBPopupListener {
                    override fun onClosed(event: LightweightWindowEvent) {
                        if (!event.isOk) return
                        exportPanel.apply()

                        psiFile.putUserData(FlexibleSearchConstants.Transform.INCLUDE_TYPE_SYSTEM_UNIQUE, includeTypeSystemUnique)
                        psiFile.putUserData(FlexibleSearchConstants.Transform.INCLUDE_DATA, includeData)
                        psiFile.putUserData(FlexibleSearchExecConstants.Transform.EXEC_RESULTS, execResult)

                        applicableTransformers[selectedTransformerIndex].transform(psiFile) { result ->
                            notifyExportDone(project, result)
                        }
                    }
                })
                popup.showUnderneathOf(inputEvent.component)
            }
    }

    private fun notifyExportDone(project: Project, result: TransformationResult) = Notifications.create(
        NotificationType.INFORMATION,
        i18n("hybris.fxs.actions.export.notification.title"),
        result.description
    )
        .addAction(i18n("hybris.fxs.actions.export.notification.copy_to_clipboard")) { _, _ ->
            CopyPasteManager.getInstance().setContents(StringSelection(result.content))
        }
        .addAction(i18n("hybris.fxs.actions.export.notification.open_scratch")) { _, _ ->
            createScratchFile(project, result.content, HybrisConstants.Languages.ImpEx.EXTENSION)
        }
        .system(true)
        .notify(project)
}
