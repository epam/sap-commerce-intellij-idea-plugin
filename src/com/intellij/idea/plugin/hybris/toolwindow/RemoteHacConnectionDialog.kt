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

import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.notifications.Notifications
import com.intellij.idea.plugin.hybris.settings.HybrisRemoteConnectionSettings
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionUtil
import com.intellij.idea.plugin.hybris.tools.remote.http.HybrisHacHttpClient
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.layout.selected
import com.intellij.util.ui.JBUI
import java.awt.Component
import java.awt.event.ActionEvent
import java.io.Serial
import javax.swing.Action
import javax.swing.JLabel

class RemoteHacConnectionDialog(
    private val project: Project,
    parentComponent: Component,
    private val settings: HybrisRemoteConnectionSettings
) : DialogWrapper(project, parentComponent, false, IdeModalityType.IDE) {

    private val testConnectionButton: Action = object : DialogWrapperAction("Test Connection") {

        @Serial
        private val serialVersionUID: Long = 7851071514284300449L

        override fun doAction(e: ActionEvent?) {
            val testSettings = with(HybrisRemoteConnectionSettings()) {
                hostIP = instanceHostTextField.text
                port = instancePortTextField.text
                isSsl = sslProtocolCheckBox.isSelected
                sslProtocol = sslProtocolComboBox.selectedItem?.toString()
                hacWebroot = hacWebrootTextField.text
                hacLogin = hacUsernameTextField.text
                hacPassword = String(hacPasswordTextField.password)
                this
            }

            val httpClient = HybrisHacHttpClient.getInstance(project)
            val errorMessage = httpClient.login(project, testSettings)

            val type: NotificationType
            val message: String
            if (errorMessage.isEmpty()) {
                message = message("hybris.toolwindow.hac.test.connection.success", "hac", testSettings.generatedURL)
                type = NotificationType.INFORMATION
            } else {
                type = NotificationType.WARNING
                message = message("hybris.toolwindow.hac.test.connection.fail", testSettings.generatedURL, errorMessage)
            }

            Notifications.create(type, message("hybris.notification.toolwindow.hac.test.connection.title"), message)
                .notify(project)
        }
    }

    private lateinit var connectionNameTextField: JBTextField
    private lateinit var instanceHostTextField: JBTextField
    private lateinit var instancePortTextField: JBTextField
    private lateinit var sslProtocolCheckBox: JBCheckBox
    private lateinit var sslProtocolComboBox: ComboBox<String>
    private lateinit var hacWebrootTextField: JBTextField
    private lateinit var hacUsernameTextField: JBTextField
    private lateinit var hacPasswordTextField: JBPasswordField
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

//        row {
//            checkBox("Shared settings")
//                .comment("Shared settings will be stored in the <strong>hybrisProjectSettings.xml</strong>.")
//                .bindSelected(settings::shared)
//        }.layout(RowLayout.PARENT_GRID)

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
                instanceHostTextField = textField()
                    .align(AlignX.FILL)
                    .bindText(settings::hostIP)
                    .onChanged { urlPreviewLabel.text = generateUrl() }
                    .addValidationRule("Address cannot be blank.") { it.text.isNullOrBlank() }
                    .component
            }.layout(RowLayout.PARENT_GRID)

            row {
                label("Port:")
                instancePortTextField = intTextField(1..65535)
                    .align(AlignX.FILL)
                    .bindText(settings::port.toNonNullableProperty(""))
                    .onChanged { urlPreviewLabel.text = generateUrl() }
                    .component
            }.layout(RowLayout.PARENT_GRID)

            row {
                sslProtocolCheckBox = checkBox("SSL:")
                    .selected(settings.isSsl)
                    .onChanged { urlPreviewLabel.text = generateUrl() }
                    .component
                sslProtocolComboBox = comboBox(
                    listOf(
                        "TLSv1",
                        "TLSv1.1",
                        "TLSv1.2"
                    ),
                    renderer = SimpleListCellRenderer.create("?") { it }
                )
                    .enabledIf(sslProtocolCheckBox.selected)
                    .bindItem(settings::sslProtocol)
                    .align(AlignX.FILL)
                    .component
            }.layout(RowLayout.PARENT_GRID)
        }

        group("HAC Settings") {
            row {
                label("Webroot:")
                hacWebrootTextField = textField()
                    .align(AlignX.FILL)
                    .bindText(settings::hacWebroot)
                    .onChanged { urlPreviewLabel.text = generateUrl() }
                    .component
            }.layout(RowLayout.PARENT_GRID)

            row {
                label("Username:")
                hacUsernameTextField = textField()
                    .align(AlignX.FILL)
                    .bindText(settings::hacLogin)
                    .addValidationRule("Username cannot be blank.") { it.text.isNullOrBlank() }
                    .component
            }.layout(RowLayout.PARENT_GRID)

            row {
                label("Password:")
                hacPasswordTextField = passwordField()
                    .align(AlignX.FILL)
                    .bindText(settings::hacPassword)
                    .addValidationRule("Password cannot be blank.") { it.password.isEmpty() }
                    .component
            }.layout(RowLayout.PARENT_GRID)
        }
    }

    init {
        title = "Remote SAP Commerce Instance"
        super.init()
    }

    override fun createCenterPanel() = with(panel) {
        border = JBUI.Borders.empty(16)
        this
    }

    override fun createLeftSideActions() = arrayOf(testConnectionButton)
    override fun getStyle() = DialogStyle.COMPACT

    private fun generateUrl() = RemoteConnectionUtil.generateUrl(
        sslProtocolCheckBox.isSelected,
        instanceHostTextField.text,
        instancePortTextField.text,
        hacWebrootTextField.text
    )

}