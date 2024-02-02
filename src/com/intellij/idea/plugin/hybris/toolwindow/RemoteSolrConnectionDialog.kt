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

package com.intellij.idea.plugin.hybris.toolwindow

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils
import com.intellij.idea.plugin.hybris.notifications.Notifications
import com.intellij.idea.plugin.hybris.settings.HybrisRemoteConnectionSettings
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionScope
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionUtil
import com.intellij.idea.plugin.hybris.tools.remote.http.solr.impl.SolrHttpClient
import com.intellij.notification.NotificationType
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.EnumComboBoxModel
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.util.ui.JBUI
import java.awt.Component
import java.awt.event.ActionEvent
import java.io.Serial
import javax.swing.Action
import javax.swing.JLabel

class RemoteSolrConnectionDialog(
    private val project: Project,
    parentComponent: Component,
    private val settings: HybrisRemoteConnectionSettings
) : DialogWrapper(project, parentComponent, false, IdeModalityType.IDE) {


    private val originalScope = settings.scope
    private val testConnectionButton: Action = object : DialogWrapperAction("Test Connection") {

        @Serial
        private val serialVersionUID: Long = 7851071514284300449L

        override fun doAction(e: ActionEvent?) {
            val testSettings = with(HybrisRemoteConnectionSettings()) {
                type = settings.type
                hostIP = hostTextField.text
                port = portTextField.text
                isSsl = sslProtocolCheckBox.isSelected
                solrWebroot = webrootTextField.text
                credentials = Credentials(usernameTextField.text, String(passwordTextField.password))
                this
            }

            var type: NotificationType
            var message: String

            try {
                SolrHttpClient.getInstance(project).listOfCores(testSettings)
                message = HybrisI18NBundleUtils.message("hybris.toolwindow.hac.test.connection.success", "SOLR", testSettings.generatedURL)
                type = NotificationType.INFORMATION
            } catch (e: Exception) {
                type = NotificationType.WARNING
                message = HybrisI18NBundleUtils.message("hybris.toolwindow.hac.test.connection.fail", testSettings.generatedURL, e.message ?: "")
            }

            Notifications.create(type, HybrisI18NBundleUtils.message("hybris.notification.toolwindow.hac.test.connection.title"), message)
                .notify(project)
        }
    }

    private lateinit var connectionNameTextField: JBTextField
    private lateinit var hostTextField: JBTextField
    private lateinit var portTextField: JBTextField
    private lateinit var sslProtocolCheckBox: JBCheckBox
    private lateinit var webrootTextField: JBTextField
    private lateinit var usernameTextField: JBTextField
    private lateinit var passwordTextField: JBPasswordField
    private lateinit var urlPreviewLabel: JLabel
    private val panel = panel {
        row {
            label("Connection name:")
                .bold()
            connectionNameTextField = textField()
                .align(AlignX.FILL)
                .bindText(settings::displayName.toNonNullableProperty(""))
                .component
        }.layout(RowLayout.PARENT_GRID)

        row {
            label("Scope:")
                .comment("Non-personal settings will be stored in the <strong>hybrisProjectSettings.xml</strong> and can be shared via VCS.")
            comboBox(
                EnumComboBoxModel(RemoteConnectionScope::class.java),
                renderer = SimpleListCellRenderer.create("?") { it.title }
            )
                .bindItem(settings::scope.toNullableProperty(RemoteConnectionScope.PROJECT_PERSONAL))
        }.layout(RowLayout.PARENT_GRID)

        group("Full URL Preview", false) {
            row {
                urlPreviewLabel = label(settings.generatedURL)
                    .bold()
                    .align(AlignX.FILL)
                    .component
            }
        }

        group("Host Settings") {
            row {
                label("Address:")
                    .comment("Host name or IP address")
                hostTextField = textField()
                    .align(AlignX.FILL)
                    .bindText(settings::hostIP.toNonNullableProperty(HybrisConstants.DEFAULT_HOST_URL))
                    .onChanged { urlPreviewLabel.text = generateUrl() }
                    .addValidationRule("Address cannot be blank.") { it.text.isNullOrBlank() }
                    .component
            }.layout(RowLayout.PARENT_GRID)

            row {
                label("Port:")
                portTextField = textField()
                    .align(AlignX.FILL)
                    .bindText(settings::port.toNonNullableProperty(""))
                    .onChanged { urlPreviewLabel.text = generateUrl() }
                    .addValidationRule("Port should be blank or in a range of 1..65535.") {
                        if (it.text.isNullOrBlank()) return@addValidationRule false

                        val intValue = it.text.toIntOrNull() ?: return@addValidationRule true
                        return@addValidationRule intValue !in 1..65535
                    }
                    .component
            }.layout(RowLayout.PARENT_GRID)

            row {
                sslProtocolCheckBox = checkBox("SSL")
                    .selected(settings.isSsl)
                    .onChanged { urlPreviewLabel.text = generateUrl() }
                    .component
            }.layout(RowLayout.PARENT_GRID)

            row {
                label("Webroot:")
                webrootTextField = textField()
                    .align(AlignX.FILL)
                    .bindText(settings::solrWebroot.toNonNullableProperty(""))
                    .onChanged { urlPreviewLabel.text = generateUrl() }
                    .component
            }.layout(RowLayout.PARENT_GRID)
        }

        group("Credentials") {
            row {
                label("Username:")
                usernameTextField = textField()
                    .align(AlignX.FILL)
                    .enabled(false)
                    .addValidationRule("Username cannot be blank.") { it.text.isNullOrBlank() }
                    .component
            }.layout(RowLayout.PARENT_GRID)

            row {
                label("Password:")
                passwordTextField = passwordField()
                    .align(AlignX.FILL)
                    .enabled(false)
                    .addValidationRule("Password cannot be blank.") { it.password.isEmpty() }
                    .component
            }.layout(RowLayout.PARENT_GRID)
        }
    }

    init {
        title = "Remote SOLR Instance"
        super.init()
    }

    override fun applyFields() {
        super.applyFields()

        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Persisting credentials", false) {
            override fun run(indicator: ProgressIndicator) {
                val credentialAttributes = CredentialAttributes("SAP CX - ${settings.uuid}")
                PasswordSafe.instance.set(credentialAttributes, Credentials(usernameTextField.text, String(passwordTextField.password)))
            }
        })

        // change of the scope
        if (settings.uuid != null && originalScope != settings.scope) {
            RemoteConnectionUtil.changeRemoteConnectionScope(project, settings, originalScope)
        }
    }

    override fun createCenterPanel() = with(panel) {
        border = JBUI.Borders.empty(16)
        loadCredentials()
        this
    }

    private fun loadCredentials() {
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Retrieving credentials", false) {
            override fun run(indicator: ProgressIndicator) {
                passwordTextField.text = settings.password
                passwordTextField.isEnabled = true

                usernameTextField.text = settings.username
                usernameTextField.isEnabled = true
            }
        })
    }

    override fun createLeftSideActions() = arrayOf(testConnectionButton)
    override fun getStyle() = DialogStyle.COMPACT
    override fun getPreferredFocusedComponent() = connectionNameTextField

    private fun generateUrl() = RemoteConnectionUtil.generateUrl(
        sslProtocolCheckBox.isSelected,
        hostTextField.text,
        portTextField.text,
        webrootTextField.text
    )
}