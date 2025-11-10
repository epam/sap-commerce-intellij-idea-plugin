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

package sap.commerce.toolset.solr.ui

import com.intellij.openapi.project.Project
import com.intellij.ui.EnumComboBoxModel
import com.intellij.ui.JBIntSpinner
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import sap.commerce.toolset.exec.settings.state.ExecConnectionScope
import sap.commerce.toolset.exec.ui.ConnectionSettingsDialog
import sap.commerce.toolset.solr.exec.SolrExecClient
import sap.commerce.toolset.solr.exec.SolrExecConnectionService
import sap.commerce.toolset.solr.exec.settings.state.SolrConnectionSettingsState
import sap.commerce.toolset.ui.nullableIntTextField
import java.awt.Component
import javax.swing.JLabel

class SolrConnectionSettingsDialog(
    project: Project,
    parentComponent: Component,
    settings: SolrConnectionSettingsState.Mutable,
    dialogTitle: String,
) : ConnectionSettingsDialog<SolrConnectionSettingsState.Mutable>(project, parentComponent, settings, dialogTitle) {

    private lateinit var urlPreviewLabel: JLabel
    private lateinit var timeoutIntSpinner: JBIntSpinner
    private lateinit var usernameTextField: JBTextField
    private lateinit var passwordTextField: JBPasswordField
    private lateinit var socketTimeoutIntSpinner: JBIntSpinner

    override fun retrieveCredentials(mutable: SolrConnectionSettingsState.Mutable) = SolrExecConnectionService.getInstance(project)
        .getCredentials(mutable.immutable().first)

    override suspend fun testConnection(): String? = try {
        val testSettings = SolrConnectionSettingsState(
            host = hostTextField.text,
            port = portTextField.text,
            ssl = sslProtocolCheckBox.isSelected,
            timeout = timeoutIntSpinner.number,
            socketTimeout = timeoutIntSpinner.number,
            webroot = webrootTextField.text,
        )

        SolrExecClient.getInstance(project).testConnection(
            testSettings,
            mutable.username.get(),
            mutable.password.get(),
        )

        null
    } catch (e: Exception) {
        e.message ?: ""
    }

    override fun panel() = panel {
        row {
            label("Connection name:")
                .bold()
            connectionNameTextField = textField()
                .align(AlignX.FILL)
                .bindText(mutable.name)
                .component
        }.layout(RowLayout.PARENT_GRID)

        row {
            label("Scope:")
                .comment("Non-personal settings will be stored in the <strong>hybrisProjectSettings.xml</strong> and can be shared via VCS.")
            comboBox(
                EnumComboBoxModel(ExecConnectionScope::class.java),
                renderer = SimpleListCellRenderer.create("?") { it.title }
            )
                .bindItem(mutable::scope.toNullableProperty(ExecConnectionScope.PROJECT_PERSONAL))
        }.layout(RowLayout.PARENT_GRID)

        row {
            timeoutIntSpinner = spinner(1000..Int.MAX_VALUE, 1000)
                .label("Connection timeout:")
                .bindIntValue(mutable::timeout)
                .gap(RightGap.SMALL)
                .component
            label("(ms)")
        }.layout(RowLayout.PARENT_GRID)

        row {
            socketTimeoutIntSpinner = spinner(1000..Int.MAX_VALUE, 1000)
                .label("Socket timeout:")
                .bindIntValue(mutable::socketTimeout)
                .gap(RightGap.SMALL)
                .component
            label("(ms)")
        }.layout(RowLayout.PARENT_GRID)

        group("Full URL Preview", false) {
            row {
                urlPreviewLabel = label(mutable.generatedURL)
                    .bold()
                    .align(AlignX.FILL)
                    .component
            }
            row {
                testConnectionLabel = label("")
                    .visible(false)
            }
            row {
                testConnectionComment = comment("")
                    .visible(false)
            }
        }

        collapsibleGroup("Host Settings") {
            row {
                hostTextField = textField()
                    .label("Host / IP:")
                    .align(AlignX.FILL)
                    .bindText(mutable.host)
                    .onChanged { urlPreviewLabel.text = generateUrl() }
                    .addValidationRule("Address cannot be blank.") { it.text.isNullOrBlank() }
                    .component

                portTextField = nullableIntTextField(1..65535)
                    .label("Port:")
                    .bindText(mutable.port)
                    .onChanged { urlPreviewLabel.text = generateUrl() }
                    .component
            }.layout(RowLayout.PARENT_GRID)

            row {
                webrootTextField = textField()
                    .label("Webroot:")
                    .bindText(mutable.webroot)
                    .onChanged { urlPreviewLabel.text = generateUrl() }
                    .component

                sslProtocolCheckBox = checkBox("SSL")
                    .bindSelected(mutable.ssl)
                    .onChanged { urlPreviewLabel.text = generateUrl() }
                    .component
            }.layout(RowLayout.PARENT_GRID)
        }.expanded = true

        collapsibleGroup("Credentials") {
            row {
                label("Username:")
                usernameTextField = textField()
                    .align(AlignX.FILL)
                    .bindText(mutable.username)
                    .enabledIf(editableCredentials)
                    .addValidationRule("Username cannot be blank.") { it.text.isNullOrBlank() }
                    .component
            }.layout(RowLayout.PARENT_GRID)

            row {
                label("Password:")
                passwordTextField = passwordField()
                    .align(AlignX.FILL)
                    .bindText(mutable.password)
                    .enabledIf(editableCredentials)
                    .addValidationRule("Password cannot be blank.") { it.password.isEmpty() }
                    .component
            }.layout(RowLayout.PARENT_GRID)
        }.expanded = true
    }
}