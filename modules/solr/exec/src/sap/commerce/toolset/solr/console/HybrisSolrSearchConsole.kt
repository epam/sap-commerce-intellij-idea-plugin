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

package sap.commerce.toolset.solr.console

import com.intellij.execution.impl.ConsoleViewUtil
import com.intellij.json.JsonFileType
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.fileTypes.PlainTextLanguage
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.JBIntSpinner
import com.intellij.ui.MutableCollectionComboBoxModel
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.whenItemSelectedFromUi
import com.intellij.util.asSafely
import kotlinx.coroutines.CoroutineScope
import sap.commerce.toolset.HybrisI18NBundleUtils
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.Notifications
import sap.commerce.toolset.console.HybrisConsole
import sap.commerce.toolset.exec.context.ConsoleAwareExecutionResult
import sap.commerce.toolset.exec.settings.state.RemoteConnectionType
import sap.commerce.toolset.solr.exec.SolrExecutionClient
import sap.commerce.toolset.solr.exec.context.SolrCoreData
import sap.commerce.toolset.solr.exec.context.SolrQueryExecutionContext
import java.awt.BorderLayout
import java.io.Serial
import javax.swing.Icon
import javax.swing.JLabel

class HybrisSolrSearchConsole(
    project: Project,
    coroutineScope: CoroutineScope
) : HybrisConsole<SolrQueryExecutionContext>(project, "[y] Solr search", PlainTextLanguage.INSTANCE, coroutineScope) {

    val docs = "Docs: "
    val coresComboBoxModel = MutableCollectionComboBoxModel<SolrCoreData>()

    private lateinit var docsLabel: JLabel
    private lateinit var coresComboBox: ComboBox<SolrCoreData>
    private lateinit var maxRowsSpinner: JBIntSpinner

    init {
        isEditable = true
        prompt = "q="

        val myPanel = panel {
            row {
                coresComboBox = comboBox(
                    model = coresComboBoxModel,
                    renderer = SimpleListCellRenderer.create("...") { cell -> cell.core }
                )
                    .label("Select core: ")
                    .whenItemSelectedFromUi(this@HybrisSolrSearchConsole) { setDocsLabelCount(it) }
                    .component

                button("Reload") { reloadCores() }

                docsLabel = label("Docs: ")
                    .component

                spinner(1..500)
                    .label("Rows (max 500):")
                    .component
                    .apply { value = 10 }
            }
        }

        add(myPanel, BorderLayout.NORTH)
    }

    override fun icon() = HybrisIcons.Console.SOLR
    override fun disabledIcon(): Icon? = null

    override fun printDefaultText() {
        this.setInputText("*:*")
    }

    override fun onSelection() {
        val selectedCore = coresComboBox.selectedItem.asSafely<SolrCoreData>()
        reloadCores(selectedCore)
    }

    override fun printResult(result: ConsoleAwareExecutionResult) {
        clear()

        printHost(RemoteConnectionType.SOLR, result.replicaContext)

        when {
            result.hasError -> ConsoleViewUtil.printAsFileType(this, result.errorMessage!!, PlainTextFileType.INSTANCE)
            result.output != null -> ConsoleViewUtil.printAsFileType(this, result.output!!, JsonFileType.INSTANCE)
            else -> ConsoleViewUtil.printAsFileType(this, "No Data", PlainTextFileType.INSTANCE)
        }
    }

    private fun reloadCores(selectedCore: SolrCoreData? = null) {
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Retrieving SOLR Cores", false) {
            override fun run(indicator: ProgressIndicator) {
                val cores = retrieveListOfCores()

                invokeLater {
                    coresComboBoxModel.removeAll()

                    if (cores.isNotEmpty()) {
                        coresComboBoxModel.removeAll()
                        coresComboBoxModel.addAll(0, cores)
                    }

                    if (selectedCore != null) {
                        setDocsLabelCount(selectedCore)
                    } else {
                        coresComboBoxModel.selectedItem = cores.firstOrNull()
                        setDocsLabelCount(cores.firstOrNull())
                    }
                }
            }
        })
    }

    private fun setDocsLabelCount(data: SolrCoreData?) {
        docsLabel.text = docs + (data?.docs ?: "...")
    }

    private fun retrieveListOfCores() = try {
        SolrExecutionClient.Companion.getInstance(project).coresData().toList()
    } catch (e: Exception) {
        Notifications.Companion.create(
            NotificationType.WARNING,
            HybrisI18NBundleUtils.message("hybris.notification.toolwindow.hac.test.connection.title"),
            HybrisI18NBundleUtils.message("hybris.notification.toolwindow.solr.test.connection.fail.content", e.localizedMessage)
        )
            .notify(project)
        emptyList()
    }

    override fun canExecute() = super.canExecute()
        && coresComboBox.selectedItem.asSafely<SolrCoreData>() != null

    override fun currentExecutionContext(content: String) = SolrQueryExecutionContext(
        content = content,
        core = (coresComboBox.selectedItem as SolrCoreData).core,
        rows = maxRowsSpinner.value as Int
    )

    override fun title() = "Solr Search"
    override fun tip() = "Solr Search Console"

    companion object {
        @Serial
        private const val serialVersionUID: Long = -2047695844446905788L
    }
}