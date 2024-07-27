/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

import com.intellij.execution.console.ConsoleHistoryController
import com.intellij.execution.console.ConsoleRootType
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.notifications.Notifications
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionType
import com.intellij.idea.plugin.hybris.tools.remote.console.HybrisConsole
import com.intellij.idea.plugin.hybris.tools.remote.http.HybrisHacHttpClient
import com.intellij.idea.plugin.hybris.tools.remote.http.solr.SolrCoreData
import com.intellij.idea.plugin.hybris.tools.remote.http.solr.SolrQueryObject
import com.intellij.idea.plugin.hybris.tools.remote.http.solr.impl.SolrHttpClient
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.invokeLater
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
import java.awt.BorderLayout
import java.io.Serial
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel

class HybrisSolrSearchConsole(project: Project) : HybrisConsole(project, HybrisConstants.CONSOLE_TITLE_SOLR_SEARCH, PlainTextLanguage.INSTANCE) {

    private object MyConsoleRootType : ConsoleRootType("hybris.solr.search.shell", null)

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

        ConsoleHistoryController(MyConsoleRootType, "hybris.solr.search.shell", this).install()
    }

    override fun connectionType() = RemoteConnectionType.SOLR

    override fun printDefaultText() {
        this.setInputText("*:*")
    }

    override fun onSelection() {
        val selectedCore = coresComboBox.selectedItem.asSafely<SolrCoreData>()
        reloadCores(selectedCore)
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
        SolrHttpClient.getInstance(project).coresData(project).toList()
    } catch (e: Exception) {
        Notifications.create(
            NotificationType.WARNING,
            message("hybris.notification.toolwindow.hac.test.connection.title"),
            message("hybris.notification.toolwindow.solr.test.connection.fail.content", e.localizedMessage)
        )
            .notify(project)
        emptyList()
    }

    override fun execute(query: String) = HybrisHacHttpClient.getInstance(project).executeSolrSearch(project, buildSolrQueryObject(query))

    override fun title() = "Solr Search"
    override fun tip() = "Solr Search Console"
    override fun icon() = HybrisIcons.Console.SOLR

    private fun buildSolrQueryObject(query: String) = coresComboBox.selectedItem
        ?.asSafely<SolrCoreData>()
        ?.core
        ?.let { SolrQueryObject(query, it, maxRowsSpinner.value as Int) }

    companion object {
        @Serial
        private val serialVersionUID: Long = -2047695844446905788L
    }
}