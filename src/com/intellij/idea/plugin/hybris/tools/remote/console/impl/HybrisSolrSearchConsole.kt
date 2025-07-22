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

package com.intellij.idea.plugin.hybris.tools.remote.console.impl

import com.intellij.execution.impl.ConsoleViewUtil
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.notifications.Notifications
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionType
import com.intellij.idea.plugin.hybris.tools.remote.console.HybrisConsole
import com.intellij.idea.plugin.hybris.tools.remote.execution.DefaultExecutionResult
import com.intellij.idea.plugin.hybris.tools.remote.execution.solr.SolrCoreData
import com.intellij.idea.plugin.hybris.tools.remote.execution.solr.SolrExecutionClient
import com.intellij.idea.plugin.hybris.tools.remote.execution.solr.SolrQueryExecutionContext
import com.intellij.json.JsonFileType
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.fileTypes.PlainTextLanguage
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.CollectionComboBoxModel
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.components.JBLabel
import com.intellij.util.asSafely
import com.intellij.vcs.log.ui.frame.WrappedFlowLayout
import com.jetbrains.rd.swing.selectedItemProperty
import com.jetbrains.rd.util.reactive.adviseEternal
import kotlinx.coroutines.CoroutineScope
import java.awt.BorderLayout
import java.io.Serial
import javax.swing.*

@Service(Service.Level.PROJECT)
class HybrisSolrSearchConsole(project: Project, coroutineScope: CoroutineScope) : HybrisConsole<SolrQueryExecutionContext>(
    project,
    HybrisConstants.CONSOLE_TITLE_SOLR_SEARCH,
    PlainTextLanguage.INSTANCE,
    coroutineScope
) {

    val docs = "Docs: "
    val coresComboBoxModel = CollectionComboBoxModel(ArrayList<SolrCoreData>())

    private val docsLabel = JBLabel(docs)
        .also { it.border = bordersLabel }
    private val coresComboBox = ComboBox(coresComboBoxModel, 270)
        .also {
            it.border = borders5
            it.renderer = SimpleListCellRenderer.create("...") { cell -> cell.core }
            it.selectedItemProperty().adviseEternal { data -> setDocsLabelCount(data) }
        }
    private val reloadCoresButton = JButton("Reload")
        .also {
            it.icon = HybrisIcons.Actions.FORCE_REFRESH
            it.isOpaque = true
            it.toolTipText = message("hybris.solr.search.console.reload.cores.button.tooltip")
            it.addActionListener { reloadCores() }
        }
    private val maxRowsSpinner = JSpinner(SpinnerNumberModel(10, 1, 500, 1))
        .also {
            it.border = borders5
        }

    init {
        isEditable = true
        prompt = "q="

        val panel = JPanel(WrappedFlowLayout(0, 0))
        panel.add(JBLabel("Select core: ").also { it.border = bordersLabel })
        panel.add(coresComboBox)
        panel.add(reloadCoresButton)
        panel.add(docsLabel)
        panel.add(JBLabel("Rows (max 500):").also { it.border = bordersLabel })
        panel.add(maxRowsSpinner)

        add(panel, BorderLayout.NORTH)
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

    override fun printResult(result: DefaultExecutionResult) {
        clear()

        printHost(RemoteConnectionType.SOLR, result.replicaContext)

        when {
            result.hasError && result.errorMessage != null -> ConsoleViewUtil.printAsFileType(this, result.errorMessage, PlainTextFileType.INSTANCE)
            result.output != null -> ConsoleViewUtil.printAsFileType(this, result.output, JsonFileType.INSTANCE)
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
        SolrExecutionClient.getInstance(project).coresData().toList()
    } catch (e: Exception) {
        Notifications.create(
            NotificationType.WARNING,
            message("hybris.notification.toolwindow.hac.test.connection.title"),
            message("hybris.notification.toolwindow.solr.test.connection.fail.content", e.localizedMessage)
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
        private val serialVersionUID: Long = -2047695844446905788L

        fun getInstance(project: Project): HybrisSolrSearchConsole = project.service()
    }
}