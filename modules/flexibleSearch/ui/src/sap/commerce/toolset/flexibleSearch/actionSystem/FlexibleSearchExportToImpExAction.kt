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
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.util.asSafely
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.Notifications
import sap.commerce.toolset.flexibleSearch.editor.flexibleSearchSplitEditor
import sap.commerce.toolset.flexibleSearch.impex.FxSColumn
import sap.commerce.toolset.flexibleSearch.impex.FxSImpExHeaderBuilder
import sap.commerce.toolset.flexibleSearch.impex.FxSImpExParam
import sap.commerce.toolset.flexibleSearch.impex.FxSQueryAnalyzer
import sap.commerce.toolset.flexibleSearch.impex.FxSQueryInfo
import sap.commerce.toolset.flexibleSearch.psi.FlexibleSearchPsiFile
import sap.commerce.toolset.i18n
import sap.commerce.toolset.ifNotFromSearchPopup
import sap.commerce.toolset.scratch.createScratchFile
import java.awt.datatransfer.StringSelection

class FlexibleSearchExportToImpExAction : DumbAwareAction() {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) = e.ifNotFromSearchPopup {
        e.presentation.text = i18n("hybris.fxs.actions.export_to_impex")
        e.presentation.description = i18n("hybris.fxs.actions.export_to_impex.description")
        e.presentation.icon = HybrisIcons.ImpEx.FILE
        e.presentation.isEnabled = e.flexibleSearchSplitEditor()?.lastExecResult?.hasDataRows == true
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val result = e.flexibleSearchSplitEditor()?.lastExecResult ?: return
        val headers = result.headers ?: return
        val rows = result.rows ?: return

        val psiFile = e.getData(CommonDataKeys.PSI_FILE)?.asSafely<FlexibleSearchPsiFile>()
        val queryInfo = psiFile?.let { FxSQueryAnalyzer.analyze(it, headers) }
            ?: FxSQueryInfo(
                primaryType = "UnknownType",
                columns = headers.map { h -> FxSColumn(resultHeaderName = h, attributeName = h, isPk = h.equals("pk", ignoreCase = true)) },
                uniqueAttributeNames = emptySet(),
            )

        val params = FxSImpExHeaderBuilder.buildParams(queryInfo, project)
        val impexContent = buildImpEx(queryInfo.primaryType, params, queryInfo, rows)

        CopyPasteManager.getInstance().setContents(StringSelection(impexContent))

        Notifications.create(
            NotificationType.INFORMATION,
            i18n("hybris.fxs.actions.export_to_impex.notification.title"),
            "${queryInfo.primaryType} (${rows.size} ${i18n("hybris.fxs.actions.export_to_impex.notification.rows")})"
        )
            .hideAfter(10)
            .addAction(i18n("hybris.fxs.actions.export_to_impex.notification.open_scratch")) { _, _ ->
                createScratchFile(project, impexContent, "impex")
            }
            .notify(project)
    }

    private fun buildImpEx(
        typeName: String,
        params: List<FxSImpExParam>,
        queryInfo: FxSQueryInfo,
        rows: List<List<String>>,
    ): String {
        // Map result column indices (excluding PK) to their param indices
        val columnIndexMap = queryInfo.columns
            .mapIndexedNotNull { idx, col -> if (!col.isPk) idx else null }

        return buildString {
            // Header line
            append("INSERT_UPDATE $typeName")
            params.forEach { param -> append("; ${param.render()}") }
            appendLine()

            // Value rows — only emit values for visible (non-PK) columns
            rows.forEach { row ->
                append("")
                columnIndexMap.forEach { srcIdx ->
                    val cell = row.getOrNull(srcIdx) ?: ""
                    val value = if (cell == "null") "" else cell
                    append("; $value")
                }
                appendLine()
            }
        }
    }
}
