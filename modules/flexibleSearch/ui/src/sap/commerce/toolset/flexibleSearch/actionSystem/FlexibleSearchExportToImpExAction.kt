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

        val impexContent = buildImpEx(queryInfo, rows)

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

    private fun buildImpEx(queryInfo: FxSQueryInfo, rows: List<List<String>>): String {
        // Filter out PK-only columns and build the visible column list
        val visibleColumns = queryInfo.columns.filterNot { it.isPk }

        return buildString {
            // Header line: INSERT_UPDATE TypeName; col1[unique=true]; col2; ...
            append("INSERT_UPDATE ${queryInfo.primaryType}")
            visibleColumns.forEach { col ->
                val modifiers = buildColumnModifiers(col, queryInfo.uniqueAttributeNames)
                append("; ${col.attributeName}$modifiers")
            }
            appendLine()

            // Value rows
            val visibleIndices = queryInfo.columns
                .mapIndexedNotNull { idx, col -> if (!col.isPk) idx else null }

            rows.forEach { row ->
                append("")
                visibleIndices.forEach { idx ->
                    val cell = row.getOrNull(idx) ?: ""
                    val value = if (cell == "null") "" else cell
                    append("; $value")
                }
                appendLine()
            }
        }
    }

    private fun buildColumnModifiers(col: FxSColumn, uniqueAttributeNames: Set<String>): String {
        val modifiers = buildList {
            if (col.attributeName in uniqueAttributeNames) add("unique=true")
            if (col.isLocalized && col.langCode != null) add("lang=${col.langCode}")
        }
        return if (modifiers.isEmpty()) "" else "[${modifiers.joinToString(",")}]"
    }
}
