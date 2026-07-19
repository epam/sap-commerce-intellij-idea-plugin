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
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.*
import com.intellij.ui.AnimatedIcon
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.asSafely
import com.intellij.util.ui.JBUI
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.Notifications
import sap.commerce.toolset.flexibleSearch.editor.flexibleSearchSplitEditor
import sap.commerce.toolset.flexibleSearch.exec.FxSImpExExecService
import sap.commerce.toolset.flexibleSearch.impex.FxSImpExHeaderBuilder
import sap.commerce.toolset.flexibleSearch.impex.FxSQueryAnalyzer
import sap.commerce.toolset.flexibleSearch.impex.FxSQueryInfo
import sap.commerce.toolset.flexibleSearch.psi.FlexibleSearchPsiFile
import sap.commerce.toolset.hac.exec.HacExecConnectionService
import sap.commerce.toolset.hac.exec.settings.state.HacConnectionSettingsState
import sap.commerce.toolset.i18n
import sap.commerce.toolset.ifNotFromSearchPopup
import sap.commerce.toolset.scratch.createScratchFile
import java.awt.datatransfer.StringSelection
import java.awt.event.KeyEvent

class FlexibleSearchExportToImpExAction : AnAction() {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) = e.ifNotFromSearchPopup {
        e.presentation.text = i18n("hybris.fxs.actions.export_to_impex")
        e.presentation.description = i18n("hybris.fxs.actions.export_to_impex.description")
        val hasPsiFile = e.getData(CommonDataKeys.PSI_FILE) is FlexibleSearchPsiFile
        val exporting = e.project?.let { FxSImpExExecService.getInstance(it).isExporting } ?: false
        e.presentation.icon = if (exporting) AnimatedIcon.Default.INSTANCE else HybrisIcons.ImpEx.FILE
        e.presentation.isEnabled = hasPsiFile && !exporting
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        if (FxSImpExExecService.getInstance(project).isExporting) return

        val result = e.flexibleSearchSplitEditor()?.lastExecResult
        val hasData = result?.hasDataRows == true
        val headers = result?.headers ?: emptyList()
        val rows = result?.rows ?: emptyList()

        val psiFile = e.getData(CommonDataKeys.PSI_FILE)?.asSafely<FlexibleSearchPsiFile>() ?: return
        val baseQueryInfo = FxSQueryAnalyzer.analyze(psiFile, headers)
        val connection = HacExecConnectionService.getInstance(project).activeConnection
        val inputEvent = e.inputEvent ?: return

        var includeTypeSystemUnique = true
        var includeData = hasData
        lateinit var myPopup: JBPopup

        val exportPanel = panel {
            row {
                checkBox(i18n("hybris.fxs.actions.export_to_impex.dialog.include_type_unique"))
                    .bindSelected({ includeTypeSystemUnique }, { includeTypeSystemUnique = it })
            }
            row {
                checkBox(i18n("hybris.fxs.actions.export_to_impex.dialog.include_data"))
                    .bindSelected({ includeData }, { includeData = it })
                    .enabled(hasData)
                    .comment(
                        if (hasData) i18n("hybris.fxs.actions.export_to_impex.dialog.include_data.has_result")
                        else i18n("hybris.fxs.actions.export_to_impex.dialog.include_data.no_result")
                    )
            }
            row {
                button(i18n("hybris.fxs.actions.export_to_impex.dialog.generate")) {
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
            .setTitle(i18n("hybris.fxs.actions.export_to_impex.dialog.title"))
            .setTitleIcon(ActiveIcon(HybrisIcons.ImpEx.FILE))
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
                        doExport(project, baseQueryInfo, rows, connection, includeData, includeTypeSystemUnique)
                    }
                })
                popup.showUnderneathOf(inputEvent.component)
            }
    }

    private fun doExport(
        project: Project,
        baseQueryInfo: FxSQueryInfo,
        rows: List<List<String>>,
        connection: HacConnectionSettingsState,
        includeData: Boolean,
        includeTypeSystemUnique: Boolean,
    ) {
        val queryInfo = if (includeTypeSystemUnique) {
            val tsUniqueAttrs = FxSImpExHeaderBuilder.typeSystemUniqueAttributeNames(baseQueryInfo.primaryType, project)
            baseQueryInfo.copy(uniqueAttributeNames = baseQueryInfo.uniqueAttributeNames + tsUniqueAttrs)
        } else {
            baseQueryInfo
        }

        val params = FxSImpExHeaderBuilder.buildParams(queryInfo, project)
        val joinUniqueParams = FxSImpExHeaderBuilder.buildJoinUniqueParams(queryInfo, project)
        val exportRows = if (includeData) rows else emptyList()

        FxSImpExExecService.getInstance(project).exportToImpEx(
            queryInfo = queryInfo,
            params = params,
            joinUniqueParams = joinUniqueParams,
            rows = exportRows,
            connection = connection,
        ) { impexContent ->
            notifyExportDone(project, queryInfo.primaryType, exportRows.size, impexContent)
        }
    }

    private fun notifyExportDone(project: Project, typeName: String, rowCount: Int, impexContent: String) {
        CopyPasteManager.getInstance().setContents(StringSelection(impexContent))

        Notifications.create(
            NotificationType.INFORMATION,
            i18n("hybris.fxs.actions.export_to_impex.notification.title"),
            "$typeName ($rowCount ${i18n("hybris.fxs.actions.export_to_impex.notification.rows")})"
        )
            .hideAfter(10)
            .addAction(i18n("hybris.fxs.actions.export_to_impex.notification.open_scratch")) { _, _ ->
                createScratchFile(project, impexContent, "impex")
            }
            .notify(project)
    }
}
