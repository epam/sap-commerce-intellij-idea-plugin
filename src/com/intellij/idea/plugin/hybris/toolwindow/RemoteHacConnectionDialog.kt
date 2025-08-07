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

package com.intellij.idea.plugin.hybris.toolwindow

import com.intellij.credentialStore.Credentials
import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.settings.RemoteConnectionListener
import com.intellij.idea.plugin.hybris.settings.RemoteConnectionSettings
import com.intellij.idea.plugin.hybris.tools.remote.RemoteConnectionScope
import com.intellij.idea.plugin.hybris.tools.remote.http.HybrisHacHttpClient
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.EnumComboBoxModel
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.layout.selected
import java.awt.Component

class RemoteHacConnectionDialog(
    project: Project,
    parentComponent: Component,
    settings: RemoteConnectionSettings
) : AbstractRemoteConnectionDialog(project, parentComponent, settings, "Remote SAP Commerce Instance") {

    private lateinit var sslProtocolComboBox: ComboBox<String>
    private lateinit var sessionCookieNameTextField: JBTextField

    override fun applyFields() {
        super.applyFields()

        project.messageBus.syncPublisher(RemoteConnectionListener.TOPIC).onHybrisConnectionModified(settings)
    }

    override fun createTestSettings() = with(RemoteConnectionSettings()) {
        type = settings.type
        hostIP = hostTextField.text
        port = portTextField.text
        isSsl = sslProtocolCheckBox.isSelected
        isWsl = isWslCheckBox?.isSelected ?: false
        sslProtocol = sslProtocolComboBox.selectedItem?.toString() ?: ""
        hacWebroot = webrootTextField.text
        sessionCookieName = sessionCookieNameTextField.text.takeIf { !it.isNullOrBlank() } ?: HybrisConstants.DEFAULT_SESSION_COOKIE_NAME
        credentials = Credentials(usernameTextField.text, String(passwordTextField.password))
        this
    }

    override fun testConnection(testSettings: RemoteConnectionSettings): String = HybrisHacHttpClient.getInstance(project)
        .testConnection(testSettings)

    override fun panel() = panel {
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
            row {
                testConnectionLabel = label("")
                    .visible(false)
            }
            row {
                testConnectionComment = comment("")
                    .visible(false)
            }
        }

        group("Host Settings") {
            row {
                label("Address:")
                hostTextField = textField()
                    .comment("Host name or IP address")
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
                sslProtocolCheckBox = checkBox("SSL:")
                    .bindSelected(settings::isSsl)
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
                    .bindItem(settings::sslProtocol.toNullableProperty())
                    .align(AlignX.FILL)
                    .component
            }.layout(RowLayout.PARENT_GRID)

            row {
                label("Webroot:")
                webrootTextField = textField()
                    .align(AlignX.FILL)
                    .bindText(settings::hacWebroot.toNonNullableProperty(""))
                    .onChanged { urlPreviewLabel.text = generateUrl() }
                    .component
            }.layout(RowLayout.PARENT_GRID)

            row {
                label("Session Cookie name:")
                sessionCookieNameTextField = textField()
                    .comment("Optional: override the session cookie name. Default is JSESSIONID.")
                    .align(AlignX.FILL)
                    .bindText(settings::sessionCookieName.toNonNullableProperty(HybrisConstants.DEFAULT_SESSION_COOKIE_NAME))
                    .apply { component.text = "" }
                    .component
            }.layout(RowLayout.PARENT_GRID)

            if (isWindows()) {
                wslHostConfiguration()
            }
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
}