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

package sap.commerce.toolset.flexibleSearch.actionSystem

import com.intellij.codeInsight.hint.HintManager
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.ex.CustomComponentAction
import com.intellij.openapi.actionSystem.impl.ActionButton
import com.intellij.openapi.application.EDT
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.Editor
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.psi.util.lastLeaf
import com.intellij.ui.GotItTooltip
import com.intellij.util.asSafely
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import sap.commerce.toolset.*
import sap.commerce.toolset.flexibleSearch.editor.flexibleSearchExecutionContextSettings
import sap.commerce.toolset.flexibleSearch.exec.context.FlexibleSearchExecContext
import sap.commerce.toolset.flexibleSearch.psi.FlexibleSearchDefinedTableName
import sap.commerce.toolset.flexibleSearch.psi.FlexibleSearchPsiFile
import sap.commerce.toolset.flexibleSearch.psi.FlexibleSearchTypes
import sap.commerce.toolset.flexibleSearch.restrictions.FlexibleSearchCheckRestriction
import sap.commerce.toolset.flexibleSearch.restrictions.FlexibleSearchRestriction
import sap.commerce.toolset.flexibleSearch.ui.FlexibleSearchRestrictionsDialog
import sap.commerce.toolset.groovy.exec.GroovyExecClient
import sap.commerce.toolset.groovy.exec.context.GroovyExecContext
import sap.commerce.toolset.hac.exec.HacExecConnectionService
import sap.commerce.toolset.settings.state.TransactionMode
import sap.commerce.toolset.typeSystem.meta.TSMetaModelStateService
import java.awt.Dimension

class FlexibleSearchShowRestrictionsAction : AnAction(
    "Show Search Restrictions",
    "Fetch and display user-specific search restrictions to be applied to a given FlexibleSearch query.",
    HybrisIcons.FlexibleSearch.RESTRICTIONS
), CustomComponentAction {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        e.presentation.isVisible = ActionPlaces.ACTION_SEARCH != e.place
        if (!e.presentation.isVisible) return
        val project = e.project ?: return

        e.presentation.isEnabled = TSMetaModelStateService.getInstance(project).initialized()
    }

    override fun actionPerformed(e: AnActionEvent) {
        val component = e.inputEvent?.component ?: return
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val psiFile = e.getData(CommonDataKeys.PSI_FILE)
            ?.asSafely<FlexibleSearchPsiFile>()
            ?: return
        val connectionService = HacExecConnectionService.getInstance(project)
        val server = connectionService.activeConnection
        val userUid = e.flexibleSearchExecutionContextSettings { FlexibleSearchExecContext.defaultSettings(server) }
            .user
            ?: connectionService.getCredentials(server).userName
            ?: "admin"

        val table2scope = PsiTreeUtil.collectElementsOfType(psiFile, FlexibleSearchDefinedTableName::class.java)
            .distinctBy { it.text }
            .map { table ->
                val marker = table.lastLeaf()
                    .elementType
                    .takeIf { it != FlexibleSearchTypes.IDENTIFIER }
                table to marker
            }

        val type2scope = buildList {
            table2scope.filter { it.second == null }
                .forEach { add(FlexibleSearchCheckRestriction(it.first.tableName, false)) }
            table2scope.filter { it.second == FlexibleSearchTypes.EXCLAMATION_MARK }
                .forEach { add(FlexibleSearchCheckRestriction(it.first.tableName, true)) }
        }.joinToString(",", "[", "]") {
            "[\"${it.typeCode}\", ${it.excludeSubTypes}]"
        }

        if (type2scope == "[]") {
            showNoRestrictions(editor, userUid)
            return
        }

        val groovyScript = readResource("scripts/flexibleSearch-user-search-restrictions.groovy")
            .replace("placeholder_userUid", userUid)
            .replace("placeholder_types", type2scope)
        val context = GroovyExecContext(
            connection = server,
            executionTitle = "Fetching '$userUid' FlexibleSearch restrictions from SAP Commerce [${server.shortenConnectionName}]...",
            content = groovyScript,
            transactionMode = TransactionMode.ROLLBACK,
            timeout = server.timeout,
        )

        GroovyExecClient.getInstance(project).execute(
            context = context,
            onError = { _, e ->
                thisLogger().warn("Unable to get search restrictions: ${e.message}", e)
                Notifications.warning("Unable to get search restrictions", "Something went wrong: ${e.message}")
                    .hideAfter(5)
                    .notify(project)
            }
        ) { coroutineScope, execResult ->
            coroutineScope.launch {
                if (execResult.hasError) {
                    thisLogger().warn("Unable to get search restrictions: ${execResult.errorMessage}")

                    Notifications.warning("Unable to get search restrictions", execResult.errorMessage ?: "Unknown error")
                        .hideAfter(5)
                        .notify(project)

                    return@launch
                }

                val result = execResult.result ?: return@launch
                val restrictions = Json.decodeFromString<Array<FlexibleSearchRestriction>>(result)

                withContext(Dispatchers.EDT) {
                    if (restrictions.isEmpty()) {
                        showNoRestrictions(editor, userUid)
                    } else {
                        FlexibleSearchRestrictionsDialog(project, userUid, restrictions.toList()).show()
                    }
                }
            }
        }
    }

    override fun createCustomComponent(presentation: Presentation, place: String) = ActionButton(this, presentation, place, Dimension())
        .also {
            GotItTooltip(
                id = GotItTooltips.FlexibleSearch.SEARCH_RESTRICTIONS,
                textSupplier = {
                    """
                    Retrieve all ${icon(HybrisIcons.FlexibleSearch.RESTRICTIONS)} ${code("search restrictions")} applied to the query for the user specified in the execution context.
                    <br><br>You can copy the detected restrictions as an ${code("ImpEx")} file or open them directly in a new Scratch File.
                    <br><br>A running SAP Commerce server and valid ${
                        link("connection configuration") {
                            DataManager.getInstance().getDataContext(it).getData(CommonDataKeys.PROJECT)
                                ?.triggerAction("sap.commerce.toolset.hac.openSettings")
                        }
                    } are required.
                """.trimIndent()
                },
                parentDisposable = null
            )
                .withHeader("Search restrictions for FlexibleSearch!")
                .show(it, GotItTooltip.BOTTOM_MIDDLE)
        }

    private fun showNoRestrictions(editor: Editor, userUid: String) {
        HintManager.getInstance().showSuccessHint(editor, "No search restrictions apply to user <strong>$userUid</strong> for the provided FlexibleSearch query.")
    }

}